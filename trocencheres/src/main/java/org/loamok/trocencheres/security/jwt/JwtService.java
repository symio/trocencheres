package org.loamok.trocencheres.security.jwt;

import java.security.Key;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import javax.crypto.SecretKey;
import org.springframework.security.core.userdetails.UserDetails;

@Service
public class JwtService {

    @Value("${app.jwt.secret}")
    private String SECRET_KEY;

    // Signature transmise pour la création du jeton.
    // Et chiffrer/déchiffrer les données du jeton
    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // Extraire « claims » du jeton
    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith((SecretKey) getSignInKey()).build()
                .parseSignedClaims(token).getPayload();
    }

    // Extraire 1 « claims » du jeton
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Extraire le pseudo du jeton
    public String extractUserName(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Valider le token
    public boolean validateToken(String token, String username) {
        final String tokenUsername = extractUserName(token);
        return (tokenUsername.equals(username) && !isTokenExpired(token));
    }

    // Validation du jeton
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUserName(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // Générer le jeton JWT
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        JwtBuilder builder = Jwts.builder();

        // D'abord définir le subject et les dates
        builder.subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()));
        builder.expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 24));

        // Puis ajouter les claims supplémentaires si ils existent
        if (extraClaims != null && !extraClaims.isEmpty()) {
            builder.claims(extraClaims);
        }

        return builder.signWith(getSignInKey()).compact();
    }

    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public String generateClientCredentialsToken(Map<String, Object> claims, String clientId) {
        JwtBuilder builder = Jwts.builder();

        builder.subject(clientId)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60)); // 1 heure

        if (claims != null && !claims.isEmpty()) {
            builder.claims(claims);
        }

        return builder.signWith(getSignInKey()).compact();
    }

    // Vérifier si c'est un token OAuth2 Client Credentials
    public boolean isClientCredentialsToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return "client_credentials".equals(claims.get("token_type"));
        } catch (Exception e) {
            return false;
        }
    }

    // Valider un token client credentials
    public boolean isClientCredentialsTokenValid(String token, String clientId) {
        try {
            final String tokenClientId = extractUserName(token);
            return (tokenClientId.equals(clientId) && !isTokenExpired(token) && isClientCredentialsToken(token));
        } catch (Exception e) {
            return false;
        }
    }

    // Extraire les scopes OAuth2
    public String extractScopes(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return (String) claims.get("scope");
        } catch (Exception e) {
            return null;
        }
    }

    // Extraire l'authority original du token OAuth2
    public String extractAuthority(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return (String) claims.get("authority");
        } catch (Exception e) {
            return null;
        }
    }

}
