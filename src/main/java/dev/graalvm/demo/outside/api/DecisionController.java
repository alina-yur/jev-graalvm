package dev.graalvm.demo.outside.api;

import dev.graalvm.demo.outside.decision.DecisionService;
import dev.graalvm.demo.outside.weather.WeatherService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class DecisionController {

    private final WeatherService weatherService;
    private final DecisionService decisionService;

    public DecisionController(WeatherService weatherService, DecisionService decisionService) {
        this.weatherService = weatherService;
        this.decisionService = decisionService;
    }

    @GetMapping("/status")
    Map<String, Object> status() {
        return Map.of("jev", decisionService.isLive(), "mode", decisionService.isLive() ? "JEV" : "DEMO");
    }

    @PostMapping("/decision")
    DecisionResponse decide(@Valid @RequestBody DecisionRequest request) {
        var weather = weatherService.forecast(request.location().trim(), request.time());
        return decisionService.decide(request, weather);
    }
}
