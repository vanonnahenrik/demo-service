package de.dsa.prodis.service.registry.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration 
@EnableWebMvc
public class AcceptMediaTypeConfig implements WebMvcConfigurer{   
    @Override   
	public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {  
		// ensures, that always hal+json is returned, event if the client sets the "Accept"-header with a special mediatype
		configurer.favorParameter(false)
		.ignoreAcceptHeader(true)
		.useRegisteredExtensionsOnly(true)
		.defaultContentType(MediaType.APPLICATION_JSON);
		
	}
}
