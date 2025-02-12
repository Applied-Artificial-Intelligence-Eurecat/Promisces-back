package org.eurecat.promisces.tools;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfiguration {

    @Autowired
    private ObjectMapper objectMapper;

    @PostConstruct
    public void configureObjectMapper() {
        objectMapper.configure(MapperFeature.DEFAULT_VIEW_INCLUSION, true);
    }
}
