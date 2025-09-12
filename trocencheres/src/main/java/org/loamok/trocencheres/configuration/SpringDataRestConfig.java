package org.loamok.trocencheres.configuration;

import org.loamok.trocencheres.entity.Adresse;
import org.loamok.trocencheres.entity.Role;
import org.loamok.trocencheres.entity.Utilisateur;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

/**
 *
 * @author Huby Franck
 */
@Configuration
public class SpringDataRestConfig implements RepositoryRestConfigurer {

    @Override
    public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config, CorsRegistry cors) {
        // Exposer les IDs dans les réponses JSON
        config.exposeIdsFor(Utilisateur.class, Role.class, Adresse.class);
        
        // Activer CORS pour votre frontend
        cors.addMapping("/**")
            .allowedOrigins("http://localhost:4200")
            .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true);
    }
}
