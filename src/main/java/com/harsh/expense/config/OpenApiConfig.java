package com.harsh.expense.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI expenseTrackerOpenAPI() {
    final String schemeName = "bearerAuth";
    return new OpenAPI()
        .info(
            new Info()
                .title("Expense Tracker API")
                .description(
                    "REST API for managing personal expenses, categories, reports, and user profiles. "
                        + "Authenticate with /api/auth/register or /api/auth/login to receive a short-lived JWT access token "
                        + "and a rotatable refresh token. Use /api/auth/refresh to rotate tokens and /api/auth/logout to revoke tokens. "
                        + "Click Authorize and provide the access token to call protected endpoints.")
                .version("1.0.0"))
        .components(
            new Components()
                .addSecuritySchemes(
                    schemeName,
                    new SecurityScheme()
                        .name(schemeName)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description(
                            "Enter the JWT access token (without the 'Bearer ' prefix). Obtain it from auth endpoints.")));
  }
}
