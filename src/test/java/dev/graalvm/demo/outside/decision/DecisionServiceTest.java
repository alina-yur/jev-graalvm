package dev.graalvm.demo.outside.decision;

import dev.graalvm.demo.outside.api.DecisionRequest;
import dev.graalvm.demo.outside.weather.WeatherSnapshot;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DecisionServiceTest {

    private final DecisionService decisions = new DecisionService(null);

    @Test
    void recommendsMovementWhenWeatherIsGood() {
        var request = new DecisionRequest("Zurich", DecisionRequest.TimeSlot.NOW, DecisionRequest.Mood.EXERCISE);
        var weather = new WeatherSnapshot("Zurich", "CH", "Mon · 18:00", 20, 5, 8, 1, "Cloudy", true);

        var result = decisions.decide(request, weather);

        assertThat(result.mode()).isEqualTo("DEMO");
        assertThat(result.goOutside()).isTrue();
        assertThat(result.activity()).isEqualTo("run");
    }

    @Test
    void recommendsAnIndoorPlanInHostileWeather() {
        var request = new DecisionRequest("Zurich", DecisionRequest.TimeSlot.NOW, DecisionRequest.Mood.RELAX);
        var weather = new WeatherSnapshot("Zurich", "CH", "Mon · 18:00", 2, 95, 48, 95, "Thunderstorms", false);

        var result = decisions.decide(request, weather);

        assertThat(result.goOutside()).isFalse();
        assertThat(result.activity()).isEqualTo("stay_inside");
    }
}
