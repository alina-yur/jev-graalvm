package dev.graalvm.demo.outside.weather;

public class LocationNotFoundException extends RuntimeException {
    public LocationNotFoundException(String location) {
        super("We couldn't find “" + location + "”. Try a nearby city.");
    }
}
