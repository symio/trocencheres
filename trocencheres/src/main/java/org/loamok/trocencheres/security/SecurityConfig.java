package org.loamok.trocencheres.security;

import java.util.List;
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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

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
                // SpringDoc OpenAPI / Swagger UI endpoints - Documentation API accessible publiquement
                .requestMatchers("/v3/api-docs/**").permitAll() // Spécification OpenAPI 3.0 en JSON/YAML
                .requestMatchers("/swagger-ui/**").permitAll() // Interface utilisateur Swagger (HTML, CSS, JS)
                .requestMatchers("/swagger-ui.html").permitAll() // Page principale de l'interface Swagger
                .requestMatchers("/swagger-resources/**").permitAll() // Métadonnées et configuration Swagger
                .requestMatchers("/webjars/**").permitAll() // Librairies JavaScript/CSS (Bootstrap, jQuery, etc.)

                // APIs basées sur les rôles uniquement
                // Adresses - Gestion des addresses de retrait et personnelles
                .requestMatchers(HttpMethod.GET, "/addresses", "/addresses/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/addresses").hasAnyRole("USER", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/addresses/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/addresses/**").hasRole("ADMIN")
                // Articles à vendre - Gestion des articles mis aux enchères
                .requestMatchers(HttpMethod.GET, "/articlesAVendres", "/articlesAVendres/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/articlesAVendres").hasAnyRole("USER", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/articlesAVendres/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/articlesAVendres/**").hasRole("ADMIN")
                // Catégories - Classification des articles
                .requestMatchers(HttpMethod.GET, "/categories", "/categories/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/categories").hasAnyRole("USER", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/categories/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/categories/**").hasRole("ADMIN")
                // Enchères - Gestion des enchères sur les articles
                .requestMatchers(HttpMethod.GET, "/encheres", "/encheres/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/encheres").hasAnyRole("USER", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/encheres/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/encheres/**").hasRole("ADMIN")
                // Rôles - Administration des rôles (Admin uniquement)
                .requestMatchers(HttpMethod.GET, "/roles", "/roles/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/roles").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/roles/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/roles/**").hasRole("ADMIN")
                // Profils - Consultation des profils (Admin seulement pour la gestion globale)
                .requestMatchers(HttpMethod.GET, "/profile").hasRole("ADMIN")
                // Utilisateurs - Gestion des comptes utilisateurs
                .requestMatchers(HttpMethod.GET, "/utilisateurs", "/utilisateurs/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/utilisateurs").hasAnyRole("USER", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/utilisateurs/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/utilisateurs/**").hasRole("ADMIN")
                // Tout le reste nécessite authentification + scope
                .anyRequest().access(this::hasAccessScopeAndAuthenticated)
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()));
        return http.build();
    }

    /**
     * Vérifie que l'utilisateur a le scope "access" ET est authentifié Cette
     * méthode ajoute une couche de sécurité supplémentaire
     */
    private AuthorizationDecision hasAccessScopeAndAuthenticated(Supplier<Authentication> authentication, RequestAuthorizationContext context) {
        Authentication auth = authentication.get();
        if (auth == null || !auth.isAuthenticated()) {
            return new AuthorizationDecision(false);
        }

        // Vérifier que l'utilisateur a le scope "access"
        boolean hasAccessScope = auth.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("SCOPE_access"));

        return new AuthorizationDecision(hasAccessScope);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:4200"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }
}
