package dev.graalvm.demo.outside.weather;

public record WeatherSnapshot(
        String location,
        String country,
        String localTime,
        double temperature,
        int rainChance,
        double windSpeed,
        int weatherCode,
        String condition,
        boolean daylight) {
}
