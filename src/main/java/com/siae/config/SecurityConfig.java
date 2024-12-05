package com.siae.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.siae.JwtSecurityFilter;

import static org.springframework.security.config.Customizer.withDefaults;
import com.siae.security.CustomPasswordEncoder;

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
                    requests.requestMatchers("/login/auth", "/public/**").permitAll();
                    try {
                        requests.anyRequest().authenticated().and().cors(withDefaults());
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                })
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
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
