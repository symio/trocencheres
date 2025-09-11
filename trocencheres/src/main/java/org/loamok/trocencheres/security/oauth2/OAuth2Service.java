package org.loamok.trocencheres.security.oauth2;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.loamok.trocencheres.entity.Utilisateur;
import org.loamok.trocencheres.repository.UtilisateurRepository;
import org.loamok.trocencheres.security.jwt.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 *
 * @author Huby Franck
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
        if (user == null)
            return Optional.empty();
        
        // Vérifier le password
        if (!pE.matches(clientSecret, user.getPassword()))
            return Optional.empty();
        
        // Scope unique "access" pour tous les utilisateurs authentifiés
        String scope = "access";
        
        // Créer les claims pour le token OAuth2
        Map<String, Object> claims = new HashMap<>();
        claims.put("client_id", clientId);
        claims.put("scope", scope);
        claims.put("token_type", "client_credentials");
        claims.put("authority", user.getAuthority()); // Le rôle de l'utilisateur
        claims.put("isAdmin", user.getRole().getIsAdmin()); // Optionnel pour des vérifications rapides
        
        // Générer le token
        String token = jwtService.generateClientCredentialsToken(claims, clientId);
        
        return Optional.of(new OAuth2TokenResponse(token, "Bearer", 3600, scope));
    }
}
