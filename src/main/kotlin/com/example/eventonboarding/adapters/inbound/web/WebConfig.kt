package com.example.eventonboarding.adapters.inbound.web

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

/** Allow the Vite dev server to call the API directly during development. */
@Configuration
class WebConfig : WebMvcConfigurer {
    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/api/**")
            .allowedOrigins("http://localhost:5173", "http://127.0.0.1:5173", "http://127.0.0.1:15173")
            .allowedMethods("GET", "POST")
    }
}
