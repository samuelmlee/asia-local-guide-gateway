package com.asialocalguide.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Entry point for the Asia Local Guide Gateway Spring Boot application.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
@EnableJpaAuditing
public class AsiaLocalGuideGatewayApplication {

	/**
	 * Starts the application.
	 *
	 * @param args command-line arguments passed to the JVM
	 */
	public static void main(String[] args) {
		SpringApplication.run(AsiaLocalGuideGatewayApplication.class, args);
	}
}
