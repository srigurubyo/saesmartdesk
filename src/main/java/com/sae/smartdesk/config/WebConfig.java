package com.sae.smartdesk.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebConfig {

    @Bean
    public FilterRegistrationBean<RequestCorrelationFilter> requestCorrelationFilterRegistration(
        RequestCorrelationFilter filter) {
        FilterRegistrationBean<RequestCorrelationFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(filter);
        registrationBean.setOrder(1);
        return registrationBean;
    }
}
