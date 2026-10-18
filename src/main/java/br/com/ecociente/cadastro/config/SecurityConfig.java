package br.com.ecociente.cadastro.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.client.RestTemplate;

import br.com.ecociente.cadastro.config.security.JwtAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtAuthenticationFilter;

  public SecurityConfig(
      JwtAuthenticationFilter jwtAuthenticationFilter) {
    this.jwtAuthenticationFilter = jwtAuthenticationFilter;
  }

  @Bean
  SecurityFilterChain securityFilterChain(
      HttpSecurity http) throws Exception {
    return http
        .csrf(csrf -> csrf.disable())
        .sessionManagement(session -> session
            .sessionCreationPolicy(
                SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(
                "/api/usuarios/**",
                "/api/condominios/**",
                "/api/coletas",
                "/api/coletas/**")
            .authenticated()
            .anyRequest().permitAll())
        .exceptionHandling(errors -> errors
            .authenticationEntryPoint((request, response, ex) -> response.sendError(401, "Autenticação necessária"))
            .accessDeniedHandler((request, response, ex) -> response.sendError(403, "Acesso não permitido")))
        .addFilterBefore(
            jwtAuthenticationFilter,
            UsernamePasswordAuthenticationFilter.class)
        .build();
  }

  @Bean
  public FilterRegistrationBean<JwtAuthenticationFilter> registroJwtAuthenticationFilter(
      JwtAuthenticationFilter filtro) {

    var registro = new FilterRegistrationBean<JwtAuthenticationFilter>(filtro);

    registro.setEnabled(false);

    return registro;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }
}