package backend.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Warhammer Companion API",
                version = "1.0",
                description = "API documentation for the Warhammer 40K Companion application. " +
                        "The API is used by the Angular frontend to manage users, game sessions, dice rolls, match history and dice presets.",
                contact = @Contact(
                        name = "Thijn van Westerhuis"
                )
        )
)
@SecurityScheme(
        name = "bearerAuth",
        description = "JWT authentication using a Bearer token",
        scheme = "bearer",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {
}