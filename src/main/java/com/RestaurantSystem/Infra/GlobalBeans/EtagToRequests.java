//package com.RestaurantSystem.Infra.GlobalBeans;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.web.servlet.FilterRegistrationBean;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.filter.ShallowEtagHeaderFilter;
//import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
//
//@Configuration
//public class EtagToRequests {
//
//    @Bean
//    public FilterRegistrationBean<ShallowEtagHeaderFilter> etagFilter(
//            @Value("${app.cache.etag.enabled:false}") boolean enabled) {
//
//        System.out.println("ETAG ENABLED? = " + enabled); // Debug
//
//        FilterRegistrationBean<ShallowEtagHeaderFilter> registration = new FilterRegistrationBean<>();
//        registration.setFilter(new ShallowEtagHeaderFilter());
//        registration.addUrlPatterns("/*");
//        registration.setEnabled(enabled);
//        return registration;
//    }
//
//    @Bean
//    public WebMvcConfigurer cacheConfig() {
//        return new WebMvcConfigurer() {
//            @Override
//            public void addResourceHandlers(ResourceHandlerRegistry registry) {
//                // do nothing (just avoid static)
//            }
//        };
//    }
//}