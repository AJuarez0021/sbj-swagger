package com.work.swagger.dto;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 *
 * @author linux
 */
@ConfigurationProperties(prefix = "swagger")
@Data
public class ConfigSwaggerDTO {
    /** The title. */
    private String application;
    
    /** The version. */
    private String version;
    
    private String path;
    
}
