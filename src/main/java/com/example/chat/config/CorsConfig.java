package com.example.chat.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;

/**
 * 用 CorsFilter 而不是 WebMvcConfigurer.addCorsMappings()：
 * 拦截器抛 401 时 MVC 的 CORS 还没写入响应头，浏览器会把 401 当成 CORS 错误。
 * CorsFilter 在 Filter 阶段就写头，保证 401/403 响应也带 CORS 头。
 *
 * 允许来源用 pattern 配置（app.cors.allowed-origin-patterns）：
 * 开发期 Vite 端口不固定（5173 被占时会顺延到 5174/5175…），按端口白名单会误杀。
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter(
            @Value("${app.cors.allowed-origin-patterns}") String allowedOriginPatterns) {
        CorsConfiguration cfg = new CorsConfiguration();
        List<String> patterns = Arrays.stream(allowedOriginPatterns.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
        cfg.setAllowedOriginPatterns(patterns);
        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        cfg.setAllowedHeaders(List.of("*"));       // 必须允许 Authorization
        cfg.setAllowCredentials(true);
        cfg.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return new CorsFilter(source);
    }
}
