package com.usta.serviexpress.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * WebConfig
 *
 * Purpose:
 * - Configures Spring MVC resource handling for serving static files from a local folder.
 *
 * Key Points:
 * - Maps HTTP requests starting with "/uploads/**" to the local file system folder "uploads" 
 *   located at the root of the project directory.
 * - Useful for serving user-uploaded files like images, documents, etc.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Adds a resource handler for serving uploaded files.
     *
     * Behavior:
     * - Requests to "/uploads/**" will be mapped to files in the local folder [project_root]/uploads/
     * - Example:
     *     HTTP GET /uploads/myimage.png → served from [project_root]/uploads/myimage.png
     *
     * @param registry ResourceHandlerRegistry to configure resource handling.
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Absolute path to the "uploads" folder in the project directory
        String rutaAbsoluta = System.getProperty("user.dir") + "/uploads/";

        // Map "/uploads/**" requests to the physical location on disk
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + rutaAbsoluta);
    }
}
