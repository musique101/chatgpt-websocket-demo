package com.cqnews.chat.config;

import feign.Logger;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    @Bean
    public RequestInterceptor requestInterceptor(){
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate requestTemplate) {
                requestTemplate.header("Authorization", "Bearer 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");
            }
        };
    }

    /**
     * feign 日志记录
     * @return
     */
    @Bean
    public Logger.Level level() {
        return Logger.Level.FULL;
    }

}
