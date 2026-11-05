package com.websites.coffeeshop.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  /** Signs remember-me cookies. The default is for local development only; the prod profile requires a real key. */
  @Value("${app.security.remember-me-key:dev-only-remember-me-key}")
  private String rememberMeKey;

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/h2-console/**")
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))
            .headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.sameOrigin()))
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers(HttpMethod.DELETE, "/user").hasAnyRole("USER", "VIP", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/user").hasAnyRole("USER", "VIP", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/admin/users/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE).hasRole("ADMIN")
                .requestMatchers("/admin/**").hasAnyRole("ADMIN")
                .requestMatchers("/user").hasAnyRole("USER", "ADMIN", "VIP")
                .requestMatchers("/").permitAll()
                .requestMatchers("/error").permitAll()
                .requestMatchers("/actuator/health/**").permitAll()
                .requestMatchers("/login/**").permitAll()
                .requestMatchers("/register/**").permitAll()
                .requestMatchers("/home").permitAll()
                .requestMatchers("/products/**").permitAll()
                .requestMatchers("/cart/**").permitAll()
                .requestMatchers("/images/**").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                .anyRequest().authenticated())
            .rememberMe(rememberMe -> rememberMe
                .tokenValiditySeconds(86400)  // 1 Day
                .key(rememberMeKey))
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/user", true)
                .permitAll())
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/home")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID"));
        return http.build();
    }
}
