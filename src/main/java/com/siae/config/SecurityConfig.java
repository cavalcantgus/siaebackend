package com.siae.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.security.config.Customizer.withDefaults;
import com.siae.security.CustomPasswordEncoder;
import com.siae.security.JwtSecurityFilter;

@Configuration
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfig {
	
	@Autowired
    private UserDetailsService userDetailsServiceImpl;
	

	private final JwtSecurityFilter securityFilter;
	
	public SecurityConfig(JwtSecurityFilter jwtAuthenticationFilter) {
        this.securityFilter = jwtAuthenticationFilter;
    }

    @Autowired
    private CustomPasswordEncoder customPasswordEncoder;
    
    @SuppressWarnings("removal")
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(requests -> {
                    requests.requestMatchers("/admin/**").hasAuthority("ADMIN");
                    requests.requestMatchers("/login/auth").permitAll();
                    requests.requestMatchers("/public/**").authenticated();
                    try {
                        requests.anyRequest().permitAll().and().cors(withDefaults());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                })
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationEntryPoint()) // Adiciona tratamento de erro 401
                )
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (HttpServletRequest request, HttpServletResponse response, org.springframework.security.core.AuthenticationException authException) -> {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token inválido ou ausente");
        };
    }
    
	@Autowired 
	public void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(userDetailsServiceImpl).passwordEncoder(customPasswordEncoder.passwordEnconder());
	}
	
	@Bean
	public AuthenticationManager authenticationManager(HttpSecurity http, CustomPasswordEncoder customPasswordEncoder, UserDetailsService userDetailsService) throws Exception {
	    return http.getSharedObject(AuthenticationManagerBuilder.class)
	            .userDetailsService(userDetailsService)
	            .passwordEncoder(customPasswordEncoder.passwordEnconder())
	            .and()
	            .build();
	}

	
}
