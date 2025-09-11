package org.loamok.trocencheres.security;

import java.util.function.Supplier;
import org.apache.commons.logging.*;
import org.loamok.trocencheres.security.jwt.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.*;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    protected final Log logger = LogFactory.getLog(getClass());

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain oauth2ApiFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/**")
            .authorizeHttpRequests(auth -> auth
                // APIs ouvertes au public sans authentification
                .requestMatchers(HttpMethod.POST, "/profil/register").permitAll()
                .requestMatchers(HttpMethod.POST, "/authorize/token").permitAll()
                
                // APIs basées sur les rôles uniquement
                // adresses
                .requestMatchers(HttpMethod.GET, "/adresses").hasAnyRole("USER", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/adresses").hasAnyRole("USER", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/adresses").hasAnyRole("USER", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/adresses").hasRole("ADMIN")
                    
                // articlesAVendres
                .requestMatchers(HttpMethod.GET, "/articlesAVendres").hasAnyRole("USER", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/articlesAVendres").hasAnyRole("USER", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/articlesAVendres").hasAnyRole("USER", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/articlesAVendres").hasRole("ADMIN")
                    
                // categories
                .requestMatchers(HttpMethod.GET, "/categories").hasAnyRole("USER", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/categories").hasAnyRole("USER", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/categories").hasAnyRole("USER", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/categories").hasRole("ADMIN")
                    
                // encheres
                .requestMatchers(HttpMethod.GET, "/encheres").hasAnyRole("USER", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/encheres").hasAnyRole("USER", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/encheres").hasAnyRole("USER", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/encheres").hasRole("ADMIN")
                    
                // profiles - Admin seulement
                .requestMatchers(HttpMethod.GET, "/profile").hasRole("ADMIN")
                    
                // utilisateurs
                .requestMatchers(HttpMethod.GET, "/utilisateurs").hasAnyRole("USER", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/utilisateurs").hasAnyRole("USER", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/utilisateurs").hasAnyRole("USER", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/utilisateurs").hasRole("ADMIN")

                // tout le reste
                .anyRequest().access(this::hasAccessScopeAndAuthenticated)
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.disable());
            
        return http.build();
    }
    
    /**
     * Vérifie que l'utilisateur a le scope "access" ET est authentifié
     * Cette méthode ajoute une couche de sécurité supplémentaire
     */
    private AuthorizationDecision hasAccessScopeAndAuthenticated(Supplier<Authentication> authentication, RequestAuthorizationContext context) {
        Authentication auth = authentication.get();
        if (auth == null || !auth.isAuthenticated())
            return new AuthorizationDecision(false);
        
        // Vérifier que l'utilisateur a le scope "access"
        boolean hasAccessScope = auth.getAuthorities().stream()
            .anyMatch(authority -> authority.getAuthority().equals("SCOPE_access"));
            
        return new AuthorizationDecision(hasAccessScope);
    }
}
