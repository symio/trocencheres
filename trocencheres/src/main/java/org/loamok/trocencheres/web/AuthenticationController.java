package org.loamok.trocencheres.web;

import org.loamok.trocencheres.security.oauth2.OAuth2TokenResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.loamok.trocencheres.security.oauth2.OAuth2Service;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Huby Franck
 */
@RestController
@RequestMapping("/authorize")
@AllArgsConstructor
public class AuthenticationController {
    
    private final OAuth2Service oauth2Service;
    
    /**
     * Endpoint OAuth2 Client Credentials
     * @param formData
     * @return 
     */
    @PostMapping(value = "/token", 
                consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
                produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> oauth2Token(@RequestBody MultiValueMap<String, String> formData) {
        String grantType = formData.getFirst("grant_type");
        String clientId = formData.getFirst("client_id");
        String clientSecret = formData.getFirst("client_secret");
        String scope = formData.getFirst("scope");
        
        // Validation du grant type
        if (!"client_credentials".equals(grantType)) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "unsupported_grant_type",
                "error_description", "Grant type must be client_credentials"
            ));
        }
        
        // Validation des paramètres
        if (clientId == null || clientSecret == null) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "invalid_request",
                "error_description", "client_id and client_secret are required"
            ));
        }
        
        // Générer le token
        Optional<OAuth2TokenResponse> tokenOpt = oauth2Service.generateClientCredentialsToken(clientId, clientSecret, scope);
        
        if (tokenOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "error", "invalid_client",
                "error_description", "Invalid client credentials or unauthorized scopes"
            ));
        }
        
        // Réponse OAuth2 standard
        OAuth2TokenResponse tokenResponse = tokenOpt.get();
        Map<String, Object> response = new HashMap<>();
        response.put("access_token", tokenResponse.getAccessToken());
        response.put("token_type", tokenResponse.getTokenType());
        response.put("expires_in", tokenResponse.getExpiresIn());
        if (tokenResponse.getScope() != null) {
            response.put("scope", tokenResponse.getScope());
        }
        
        return ResponseEntity.ok(response);
    }
}
