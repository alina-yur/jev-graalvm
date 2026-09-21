package dev.graalvm.demo.outside.weather;

import dev.graalvm.demo.outside.api.DecisionRequest.TimeSlot;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import tools.jackson.databind.JsonNode;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.IntStream;

@Service
public class WeatherService {

    private static final DateTimeFormatter API_TIME = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final DateTimeFormatter DISPLAY_TIME = DateTimeFormatter.ofPattern("EEE · HH:mm");
    private final RestClient http = RestClient.builder().build();

    public WeatherSnapshot forecast(String query, TimeSlot slot) {
        JsonNode search = http.get()
                .uri("https://geocoding-api.open-meteo.com/v1/search?name={name}&count=1&language=en&format=json", query)
                .retrieve().body(JsonNode.class);

        JsonNode results = search == null ? null : search.path("results");
        if (results == null || !results.isArray() || results.isEmpty()) {
            throw new LocationNotFoundException(query);
        }

        JsonNode place = results.get(0);
        double latitude = place.path("latitude").asDouble();
        double longitude = place.path("longitude").asDouble();

        JsonNode forecast = http.get().uri(
                "https://api.open-meteo.com/v1/forecast?latitude={lat}&longitude={lon}" +
                        "&hourly=temperature_2m,precipitation_probability,weather_code,wind_speed_10m" +
                        "&daily=sunrise,sunset&timezone=auto&forecast_days=2",
                latitude, longitude).retrieve().body(JsonNode.class);

        if (forecast == null) {
            throw new RestClientException("Empty forecast");
        }

        ZoneId zone = ZoneId.of(forecast.path("timezone").asText("UTC"));
        LocalDateTime now = ZonedDateTime.now(zone).toLocalDateTime();
        LocalDateTime target = switch (slot) {
            case NOW -> now;
            case LUNCH -> nextAt(now, 12);
            case EVENING -> nextAt(now, 18);
        };

        JsonNode times = forecast.path("hourly").path("time");
        int index = IntStream.range(0, times.size())
                .boxed()
                .min((a, b) -> Long.compare(distance(times.get(a), target), distance(times.get(b), target)))
                .orElseThrow(() -> new RestClientException("Forecast contained no hours"));

        JsonNode hourly = forecast.path("hourly");
        LocalDateTime forecastTime = LocalDateTime.parse(times.get(index).asText(), API_TIME);
        double temperature = hourly.path("temperature_2m").get(index).asDouble();
        int rain = hourly.path("precipitation_probability").get(index).asInt();
        double wind = hourly.path("wind_speed_10m").get(index).asDouble();
        int code = hourly.path("weather_code").get(index).asInt();

        JsonNode daily = forecast.path("daily");
        int dayIndex = forecastTime.toLocalDate().equals(now.toLocalDate()) ? 0 : 1;
        LocalDateTime sunrise = LocalDateTime.parse(daily.path("sunrise").get(dayIndex).asText(), API_TIME);
        LocalDateTime sunset = LocalDateTime.parse(daily.path("sunset").get(dayIndex).asText(), API_TIME);
        boolean daylight = !forecastTime.isBefore(sunrise) && forecastTime.isBefore(sunset);

        return new WeatherSnapshot(
                place.path("name").asText(query),
                place.path("country_code").asText(""),
                forecastTime.format(DISPLAY_TIME),
                temperature,
                rain,
                wind,
                code,
                describe(code),
                daylight);
    }

    private static LocalDateTime nextAt(LocalDateTime now, int hour) {
        LocalDateTime candidate = now.toLocalDate().atTime(hour, 0);
        return candidate.isBefore(now.minusMinutes(30)) ? candidate.plusDays(1) : candidate;
    }

    private static long distance(JsonNode time, LocalDateTime target) {
        return Math.abs(Duration.between(LocalDateTime.parse(time.asText(), API_TIME), target).toMinutes());
    }

    static String describe(int code) {
        if (code == 0) return "Clear";
        if (code <= 3) return "Cloudy";
        if (code <= 48) return "Foggy";
        if (code <= 57) return "Drizzle";
        if (code <= 67) return "Rain";
        if (code <= 77) return "Snow";
        if (code <= 82) return "Showers";
        if (code <= 86) return "Snow showers";
        return "Thunderstorms";
    }
}
