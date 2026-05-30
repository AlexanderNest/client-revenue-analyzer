package ru.nesterov.web.config;

import com.ebay.ejmask.core.BaseFilter;
import com.ebay.ejmask.core.EJMaskInitializer;
import com.ebay.ejmask.extenstion.builder.header.HeaderFieldPatternBuilder;
import com.ebay.ejmask.extenstion.builder.json.JsonFullValuePatternBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class EJMaskingConfig {

    @Bean
    public EJMaskingConfig initializer(BaseFilter headers, BaseFilter body) {
        EJMaskInitializer.addFilter(headers, body);
        return this;
    }

    @Bean
    public BaseFilter headers() {
        return new BaseFilter(HeaderFieldPatternBuilder.class, 0, "x-secret-token", "Authorization", "X-API-Key");
    }

    @Bean
    public BaseFilter body() {
        return new BaseFilter(JsonFullValuePatternBuilder.class, 0, "phone", "password");
    }
}
