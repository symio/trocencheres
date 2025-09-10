package org.loamok.trocencheres.security.jwt;

import org.loamok.trocencheres.repository.UtilisateurRepository;
import org.springframework.context.annotation.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.core.*;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.*;

@Configuration
public class JwtAppConfig {

    private final UtilisateurRepository uRepository;

    public JwtAppConfig(UtilisateurRepository uRepository) {
        this.uRepository = uRepository;
    }

    /**
     * Fournit le service qui charge les utilisateurs depuis la base de données
     */
    @Bean
    UserDetailsService userDetailsService() {
        return username -> uRepository.findById(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    /**
     * Fournisseur d'authentification personnalisé (remplace
     * DaoAuthenticationProvider)
     */
    @Bean
    AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        return new AuthenticationProvider() {
            @Override
            public Authentication authenticate(Authentication authentication) throws AuthenticationException {
                String username = authentication.getName();
                String password = authentication.getCredentials().toString();
                UserDetails user = userDetailsService.loadUserByUsername(username);
                if (!passwordEncoder.matches(password, user.getPassword())) {
                    throw new BadCredentialsException("Bad credentials");
                }
                return new UsernamePasswordAuthenticationToken(user, password, user.getAuthorities());
            }

            @Override
            public boolean supports(Class<?> authentication) {
                return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
            }
        };
    }

    /**
     * Gestionnaire d'authentification basé sur le fournisseur personnalisé
     */
    @Bean
    AuthenticationManager authenticationManager(AuthenticationProvider authenticationProvider) {
        return new ProviderManager(authenticationProvider);
    }

    /**
     * Encodeur de mot de passe
     */
    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
