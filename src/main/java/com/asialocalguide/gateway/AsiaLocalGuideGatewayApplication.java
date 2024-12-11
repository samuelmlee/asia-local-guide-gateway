package com.asialocalguide.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class AsiaLocalGuideGatewayApplication {

  public static void main(String[] args) {
    SpringApplication.run(AsiaLocalGuideGatewayApplication.class, args);
  }
}
