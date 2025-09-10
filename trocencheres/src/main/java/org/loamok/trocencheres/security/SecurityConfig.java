package org.loamok.trocencheres.security;

import org.apache.commons.logging.*;
import org.loamok.trocencheres.security.jwt.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    protected final Log logger = LogFactory.getLog(getClass());

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private AuthenticationProvider authenticationProvider;

    /**
     * Configuration pour les APIs OAuth2 - priorité haute
     */
    @Bean
    public SecurityFilterChain oauth2ApiFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/**")
//                .authorizeHttpRequests(auth -> auth
//                    .anyRequest().permitAll()
//                )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/authorize/token").permitAll()
                // APIs OAuth2 protégées par scopes
                // adresses
                .requestMatchers(HttpMethod.GET, "/adresses").hasAuthority("SCOPE_read")
                .requestMatchers(HttpMethod.POST, "/adresses").hasAuthority("SCOPE_write")
                .requestMatchers(HttpMethod.PUT, "/adresses").hasAuthority("SCOPE_write")
                .requestMatchers(HttpMethod.DELETE, "/adresses").hasAuthority("SCOPE_admin")
                    
                // articlesAVendres
                .requestMatchers(HttpMethod.GET, "/articlesAVendres").hasAuthority("SCOPE_read")
                .requestMatchers(HttpMethod.POST, "/articlesAVendres").hasAuthority("SCOPE_write")
                .requestMatchers(HttpMethod.PUT, "/articlesAVendres").hasAuthority("SCOPE_write")
                .requestMatchers(HttpMethod.DELETE, "/articlesAVendres").hasAuthority("SCOPE_admin")
                    
                // categories
                .requestMatchers(HttpMethod.GET, "/categories").hasAuthority("SCOPE_read")
                .requestMatchers(HttpMethod.POST, "/categories").hasAuthority("SCOPE_write")
                .requestMatchers(HttpMethod.PUT, "/categories").hasAuthority("SCOPE_write")
                .requestMatchers(HttpMethod.DELETE, "/categories").hasAuthority("SCOPE_admin")
                    
                // encheres
                .requestMatchers(HttpMethod.GET, "/encheres").hasAuthority("SCOPE_read")
                .requestMatchers(HttpMethod.POST, "/encheres").hasAuthority("SCOPE_write")
                .requestMatchers(HttpMethod.PUT, "/encheres").hasAuthority("SCOPE_write")
                .requestMatchers(HttpMethod.DELETE, "/encheres").hasAuthority("SCOPE_admin")
                    
                // profiles
                .requestMatchers(HttpMethod.GET, "/profile").hasAuthority("SCOPE_admin")
                    
                // utilisateurs
                .requestMatchers(HttpMethod.GET, "/utilisateurs").hasAuthority("SCOPE_read")
                .requestMatchers(HttpMethod.POST, "/utilisateurs").hasAuthority("SCOPE_write")
                .requestMatchers(HttpMethod.PUT, "/utilisateurs").hasAuthority("SCOPE_write")
                .requestMatchers(HttpMethod.DELETE, "/utilisateurs").hasAuthority("SCOPE_admin")

                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.disable());
            
        return http.build();
    }

}
