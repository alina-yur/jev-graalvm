package dev.graalvm.demo.outside.api;

import dev.graalvm.demo.outside.weather.WeatherSnapshot;

public record DecisionResponse(
        String mode,
        boolean goOutside,
        int probability,
        int confidence,
        String activity,
        String activityLabel,
        String headline,
        String accent,
        String message,
        String duration,
        String note,
        WeatherSnapshot weather) {
}
