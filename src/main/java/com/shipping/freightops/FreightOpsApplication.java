package com.shipping.freightops;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Freight Operations API - Shipping Line POC. */
@SpringBootApplication
@OpenAPIDefinition(
    info =
        @Info(
            title = "Freight Operations API",
            version = "0.1.0",
            description =
                "API for managing freight orders, voyages, containers, vessels, and ports"))
public class FreightOpsApplication {

  public static void main(String[] args) {
    SpringApplication.run(FreightOpsApplication.class, args);
  }
}
