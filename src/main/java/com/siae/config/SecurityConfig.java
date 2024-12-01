package com.siae.config;

import static org.springframework.security.config.Customizer.withDefaults;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;

import com.siae.security.CustomPasswordEncoder;

@Configuration
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfig {
	
	@Autowired
    private UserDetailsService userDetailsServiceImpl;

    @Autowired
    private CustomPasswordEncoder customPasswordEncoder;
    
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(requests -> {
                    requests.requestMatchers("/admin/**").hasAuthority("ADMIN");
                    requests.requestMatchers("/public/**").permitAll();
                    requests.anyRequest().authenticated();
                })
                .httpBasic(withDefaults());
        
        return http.build();
		
	}
	
	@Autowired 
	public void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(userDetailsServiceImpl).passwordEncoder(customPasswordEncoder.passwordEnconder());
	}
	
}
