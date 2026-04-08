/**
 * CsTrackerApplication.java
 *
 * Entry point for the CS2 Investment Tracker Spring Boot application.
 *
 * @author Ville Laaksoaho
 */
package com.cstracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CsTrackerApplication {

    /**
     * Bootstraps and launches the Spring Boot application.
     *
     * @param args command-line arguments passed to the application
     */
    public static void main(String[] args) {
        SpringApplication.run(CsTrackerApplication.class, args);
    }
}
