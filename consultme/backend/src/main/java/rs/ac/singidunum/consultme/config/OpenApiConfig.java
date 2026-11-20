package rs.ac.singidunum.consultme.config;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

// Podešavanje Swagger-a da zna za JWT.
// U Swagger-u se pojavi dugme "Authorize" gde nalepite token dobijen sa /api/auth/login,
// i od tada Swagger šalje token uz svaki zahtev (kao što radi Angular).
@Configuration
@OpenAPIDefinition(
		info = @Info(title = "ConsultMe API", version = "0.1", description = "Portal za zakazivanje konsultacija"),
		security = @SecurityRequirement(name = "bearerAuth"))
@SecurityScheme(
		name = "bearerAuth",
		type = SecuritySchemeType.HTTP,
		scheme = "bearer",
		bearerFormat = "JWT")
public class OpenApiConfig {

}
