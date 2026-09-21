package dev.graalvm.demo.outside.weather;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WeatherServiceTest {

    @Test
    void translatesOpenMeteoWeatherCodes() {
        assertThat(WeatherService.describe(0)).isEqualTo("Clear");
        assertThat(WeatherService.describe(61)).isEqualTo("Rain");
        assertThat(WeatherService.describe(95)).isEqualTo("Thunderstorms");
    }
}
