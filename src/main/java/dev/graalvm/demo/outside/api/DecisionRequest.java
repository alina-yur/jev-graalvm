package dev.graalvm.demo.outside.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DecisionRequest(
        @NotBlank @Size(max = 80) String location,
        @NotNull TimeSlot time,
        @NotNull Mood mood) {

    public enum TimeSlot { NOW, LUNCH, EVENING }

    public enum Mood { RELAX, EXERCISE, SOCIALIZE, EXPLORE }
}
