package org.loamok.trocencheres.security.oauth2;

import fr.eni.cave.security.oauth2.OAuth2TokenResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.loamok.trocencheres.entity.Utilisateur;
import org.loamok.trocencheres.repository.UtilisateurRepository;
import org.loamok.trocencheres.security.jwt.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 *
 * @author symio
 */
@Service
@AllArgsConstructor
public class OAuth2Service {
    private UtilisateurRepository uR;
    private PasswordEncoder pE;
    private JwtService jwtService;
    
    public Optional<OAuth2TokenResponse> generateClientCredentialsToken(String clientId, String clientSecret, String requestedScopes) {
        // Utiliser les utilisateurs existants comme "clients" OAuth2
        Utilisateur user = uR.findByPseudo(clientId);
        if (user == null) {
            return Optional.empty();
        }
        
        // Vérifier le password (même logique que l'authentification utilisateur)
        if (!pE.matches(clientSecret, user.getPassword())) {
            return Optional.empty();
        }
        
        // Définir les scopes selon le rôle de l'utilisateur
        String role = user.getRole().getRole();
        user.setIsAdmin(user.getRole().getIsAdmin());
        Set<String> allowedScopes = getScopesForAuthority(role);
        Set<String> requestedScopesSet = requestedScopes != null ? 
            Set.of(requestedScopes.split(" ")) : allowedScopes;
            
        // Vérifier que les scopes demandés sont autorisés pour ce rôle
        if (!allowedScopes.containsAll(requestedScopesSet)) {
            return Optional.empty();
        }
        
        // Créer les claims pour le token OAuth2
        Map<String, Object> claims = new HashMap<>();
        claims.put("client_id", clientId);
        claims.put("scope", String.join(" ", requestedScopesSet));
        claims.put("token_type", "client_credentials");
        claims.put("authority", user.getAuthority()); // Garder le rôle original
        
        // Générer le token
        String token = jwtService.generateClientCredentialsToken(claims, clientId);
        
        return Optional.of(new OAuth2TokenResponse(token, "Bearer", 3600, String.join(" ", requestedScopesSet)));
    }
    
    
    /**
     * Définit les scopes autorisés selon le rôle de l'utilisateur
     */
    private Set<String> getScopesForAuthority(String authority) {
        return switch (authority) {
            case "ROLE_ADMIN" -> Set.of("read", "write", "admin"); // Tous les droits
            case "ROLE_USER" -> Set.of("read", "write"); // Lecture + écriture limitée
            default -> Set.of("read"); // Par défaut, lecture seule
        };
    }
}
