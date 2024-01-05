package com.walnutek.fermentationtank;

import com.walnutek.fermentationtank.config.Const;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableMongoAuditing
@OpenAPIDefinition(
	info = @Info(title = "smart-fermentation-tank-api", version = "dev")
)
@SecurityScheme(
		name = Const.BEARER_JWT,
		scheme = "Bearer",
		bearerFormat = "JWT",
		type = SecuritySchemeType.APIKEY,
		in = SecuritySchemeIn.HEADER,
		description = "JWT Authorization"
)
public class SmartFermentationTankApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(SmartFermentationTankApiApplication.class, args);
	}

}
