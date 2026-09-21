package dev.graalvm.demo.outside;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportRuntimeHints;

@SpringBootApplication
@ImportRuntimeHints(OutsideRuntimeHints.class)
public class OutsideApplication {

    public static void main(String[] args) {
        SpringApplication.run(OutsideApplication.class, args);
    }
}
