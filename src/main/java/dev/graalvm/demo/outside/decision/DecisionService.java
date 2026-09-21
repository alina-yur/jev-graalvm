package dev.graalvm.demo.outside.decision;

import dev.graalvm.demo.outside.api.DecisionRequest;
import dev.graalvm.demo.outside.api.DecisionResponse;
import dev.graalvm.demo.outside.weather.WeatherSnapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springaicommunity.typesafe.TypeSafeClient;
import org.springaicommunity.typesafe.question.Choice;
import org.springaicommunity.typesafe.question.Noul;
import org.springaicommunity.typesafe.question.Score;
import org.springaicommunity.typesafe.response.SystemOneResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;

@Service
public class DecisionService {

    private static final Logger log = LoggerFactory.getLogger(DecisionService.class);
    private final TypeSafeClient jev;

    public DecisionService() {
        this(clientFromEnvironment());
    }

    DecisionService(TypeSafeClient jev) {
        this.jev = jev;
    }

    public boolean isLive() {
        return jev != null;
    }

    public DecisionResponse decide(DecisionRequest request, WeatherSnapshot weather) {
        if (jev == null) {
            return demoDecision(request, weather, "Local fallback decision.");
        }

        try {
            Map<String, Object> state = Map.of(
                    "location", weather.location(),
                    "local_time", weather.localTime(),
                    "available_time", request.time().name().toLowerCase(),
                    "mood", request.mood().name().toLowerCase(),
                    "temperature_c", weather.temperature(),
                    "rain_probability_percent", weather.rainChance(),
                    "wind_kmh", weather.windSpeed(),
                    "conditions", weather.condition(),
                    "daylight", weather.daylight());

            SystemOneResponse answer = jev.systemOne(state, Map.of(
                    "activity", Choice.builder()
                            .instructions("Which activity best fits this person's mood, available time, and forecast?")
                            .option("walk", "A relaxed walk outdoors")
                            .option("run", "A run or energetic outdoor exercise")
                            .option("outdoor_cafe", "Relax outside at a café")
                            .option("explore", "Explore the city or nature outdoors")
                            .option("social_outdoors", "Meet people somewhere outdoors")
                            .option("stay_inside", "Conditions make an indoor plan the better choice")
                            .build(),
                    "go_outside", Noul.builder()
                            .instructions("Should this person go outside for their chosen mood and time?")
                            .whenTrue("The conditions and preferences support an outdoor activity")
                            .whenFalse("An indoor activity is a meaningfully better fit")
                            .build(),
                    "comfort", Score.of("How comfortable are the outdoor conditions for this person?",
                            "Hostile", "Manageable", "Pleasant", "Excellent")));

            var goOutside = answer.noul("go_outside");
            boolean outside = goOutside.isTrue();
            String activity = answer.choiceValue("activity");
            if (!outside) {
                activity = "stay_inside";
            }
            else if ("stay_inside".equals(activity)) {
                activity = outdoorActivity(request, weather);
            }
            int probability = percent(goOutside.value());
            int confidence = percent(answer.choice("activity").confidence());
            return response("JEV", outside, activity, probability, confidence, weather,
                    "Live typed decision from JEV · comfort: " + answer.score("comfort").nearestLabel());
        }
        catch (RuntimeException exception) {
            log.warn("JEV request failed; returning the demo decision", exception);
            return demoDecision(request, weather, "JEV was unavailable, so this is a local fallback decision.");
        }
    }

    private DecisionResponse demoDecision(DecisionRequest request, WeatherSnapshot weather, String note) {
        int weatherScore = 92;
        weatherScore -= Math.max(0, weather.rainChance() - 15) / 2;
        weatherScore -= Math.max(0, (int) weather.windSpeed() - 20);
        if (weather.temperature() < 5) weatherScore -= 22;
        if (weather.temperature() > 31) weatherScore -= 20;
        if (weather.weatherCode() >= 80) weatherScore -= 28;
        if (!weather.daylight() && request.mood() == DecisionRequest.Mood.EXPLORE) weatherScore -= 12;
        int probability = Math.max(8, Math.min(96, weatherScore));

        String activity;
        if (probability < 45) activity = "stay_inside";
        else activity = outdoorActivity(request, weather);
        return response("DEMO", !"stay_inside".equals(activity), activity, probability, 100, weather, note);
    }

    private static String outdoorActivity(DecisionRequest request, WeatherSnapshot weather) {
        return switch (request.mood()) {
            case RELAX -> weather.rainChance() < 25 ? "outdoor_cafe" : "walk";
            case EXERCISE -> weather.windSpeed() < 30 ? "run" : "walk";
            case SOCIALIZE -> "social_outdoors";
            case EXPLORE -> "explore";
        };
    }

    private static DecisionResponse response(String mode, boolean outside, String activity,
                                             int probability, int confidence,
                                             WeatherSnapshot weather, String note) {
        Activity copy = Activity.from(activity, weather);
        return new DecisionResponse(mode, outside, probability, confidence, activity, copy.label,
                outside ? "YES." : "NOT TODAY.", copy.accent,
                copy.message, copy.duration, note, weather);
    }

    private static int percent(double value) {
        return (int) Math.round(value * 100);
    }

    private static TypeSafeClient clientFromEnvironment() {
        String key = System.getenv("TYPESAFE_API_KEY");
        return StringUtils.hasText(key) ? TypeSafeClient.builder().apiKey(key).build() : null;
    }

    private record Activity(String label, String accent, String message, String duration) {
        static Activity from(String key, WeatherSnapshot weather) {
            return switch (key) {
                case "run" -> new Activity("GO FOR A RUN", "MOVE YOUR BODY.", "Move while the forecast is on your side. Future you will be annoyingly pleased.", "35 MIN");
                case "outdoor_cafe" -> new Activity("FIND A TERRACE", "TAKE IT SLOW.", "Take the slow option. Get a drink, claim a table, and let the day happen around you.", "60 MIN");
                case "explore" -> new Activity("PICK A NEW DIRECTION", "GET CURIOUS.", "No grand itinerary needed. Choose a place you have not seen and wander toward it.", "75 MIN");
                case "social_outdoors" -> new Activity("TEXT THE GROUP", "MAKE PLANS.", "The forecast passes the vibe check. Find a park, terrace, or easy meeting point.", "90 MIN");
                case "stay_inside" -> new Activity("MAKE AN INDOOR PLAN", "STAY COZY.", "The sky has declined your invitation. Stay in without feeling guilty about it.", "NO RUSH");
                default -> new Activity("TAKE A WALK", "CLEAR YOUR HEAD.", weather.daylight()
                        ? "Take a slow walk while the light is good. The conditions are doing you a favor."
                        : "Take a short reset walk. Keep it familiar, easy, and close to home.", "45 MIN");
            };
        }
    }
}
