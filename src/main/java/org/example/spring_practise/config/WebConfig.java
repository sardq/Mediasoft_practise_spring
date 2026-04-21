package org.example.spring_practise.config;

import org.example.spring_practise.Enums.SightCategory;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.core.convert.converter.Converter;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new Converter<String, SightCategory>() {
            @Override
            public SightCategory convert(String source) {
                SightCategory cat = SightCategory.fromRussianName(source);
                if (cat == null) {
                    try {
                        return SightCategory.valueOf(source.toUpperCase());
                    } catch (Exception e) {
                        return null;
                    }
                }
                return cat;
            }
        });
    }
}
