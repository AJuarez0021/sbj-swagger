package com.work.swagger.config;

import com.work.swagger.dto.ConfigSwaggerDTO;
import com.work.swagger.util.Constants;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 *
 * @author linux
 */
@Configuration
@PropertySource({"classpath:application.properties"})
@EnableConfigurationProperties(ConfigSwaggerDTO.class)
@Slf4j
public class SwaggerConfig {

    private final ConfigSwaggerDTO config;

    public SwaggerConfig(ConfigSwaggerDTO config) {
        this.config = config;
    }

    @Bean
    OpenAPI openAPI() {
        return new OpenAPI().info(getInfo())
                .addServersItem(new Server().url(config.getPath()));
    }

    private Info getInfo() {
        Info info = new Info();
        info.setTitle(config.getApplication());
        info.setDescription(Constants.SWAGGER_DESCRIPTION);
        info.setVersion(config.getVersion());
        info.setContact(getContact());
        return info;
    }

    private Contact getContact() {
        return new Contact().name(Constants.SWAGGER_NAME)
                .url(Constants.SWAGGER_URL)
                .email(Constants.SWAGGER_EMAIL);
    }

}
