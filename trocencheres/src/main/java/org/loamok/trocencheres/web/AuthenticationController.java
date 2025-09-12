package org.loamok.trocencheres.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.loamok.trocencheres.security.oauth2.OAuth2TokenResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.loamok.trocencheres.security.jwt.TokenRequest;
import org.loamok.trocencheres.security.jwt.TokenResponse;
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

    @Operation(
            summary = "Obtenir un token d'accès OAuth2",
            description = "Authentifie un client via le flow Client Credentials et retourne un JWT access_token",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Paramètres OAuth2 au format application/x-www-form-urlencoded",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
                            schema = @Schema(implementation = TokenRequest.class)
                    )
            )
    )
    @ApiResponses(value = {
        @ApiResponse(
                responseCode = "200",
                description = "Token généré avec succès",
                content = @Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = TokenResponse.class)
                )
        ),
        @ApiResponse(
                responseCode = "400",
                description = "Paramètres manquants ou invalides"
        ),
        @ApiResponse(
                responseCode = "401",
                description = "Client ID ou secret invalide"
        )
    })
    @PostMapping(value = "/token",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> oauth2Token(
            @Parameter(description = "Données d'authentification OAuth2", required = true)
            @Valid TokenRequest tokenRequest) {
        String grantType = tokenRequest.getGrant_type();
        String clientId = tokenRequest.getClient_id();
        String clientSecret = tokenRequest.getClient_secret();
        String scope = tokenRequest.getScope();

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
