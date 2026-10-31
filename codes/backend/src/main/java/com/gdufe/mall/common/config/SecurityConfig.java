package com.gdufe.mall.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gdufe.mall.common.result.Result;
import com.gdufe.mall.common.result.ResultCode;
import com.gdufe.mall.common.security.JwtAuthFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.util.List;

/**
 * Spring Security 6 无状态配置。
 * 路径规则顺序敏感：精确的放行规则必须写在 anyRequest 之前。
 * 注意：context-path=/api 会被剥离，这里的路径不含 /api。
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final ObjectMapper objectMapper;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 只放行三个登录入口；/auth/me、/auth/logout 必须登录（契约 AUTH-04/05）
                        .requestMatchers(HttpMethod.POST,
                                "/auth/login", "/auth/register", "/auth/admin/login").permitAll()
                        .requestMatchers("/health").permitAll()
                        .requestMatchers(HttpMethod.GET, "/files/**",
                                "/front/products/**", "/front/categories/**").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((req, res, ex) -> writeUnauthorized(req, res))
                        .accessDeniedHandler((req, res, ex) ->
                                writeJson(res, HttpStatus.FORBIDDEN, ResultCode.FORBIDDEN)))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // 开发期前端走 Vite 代理，此配置为兜底；生产用 Nginx 同源
        config.setAllowedOriginPatterns(List.of("http://localhost:*", "http://127.0.0.1:*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    /**
     * 未通过认证：带 token 但校验失败 -> 2002（令牌无效/过期）；压根没带 -> 2001。
     * 前端对两者处理一致（清 token 跳登录），但错误语义不能混（契约边界 #5）。
     */
    private void writeUnauthorized(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Object jwtError = request.getAttribute(JwtAuthFilter.ATTR_JWT_ERROR);
        writeJson(response, HttpStatus.UNAUTHORIZED,
                jwtError == null ? ResultCode.UNAUTHORIZED : ResultCode.TOKEN_INVALID);
    }

    private void writeJson(HttpServletResponse response, HttpStatus status, ResultCode code) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(Result.fail(code)));
    }
}
