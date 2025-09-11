package org.loamok.trocencheres.security.jwt;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 *
 * @author Huby Franck
 */
@Component
@AllArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private JwtService jwtService;
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
    throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        
        jwt = authHeader.substring(7);
        handleClientCredentialsToken(jwt, request);
        setAuthentication(jwt);
        
        filterChain.doFilter(request, response);
    }
    
    private void handleClientCredentialsToken(String jwt, HttpServletRequest request) {
        try {
            final String clientId = jwtService.extractUserName(jwt); // subject = clientId
            String scopes = jwtService.extractScopes(jwt);

            if (clientId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                if (jwtService.isClientCredentialsTokenValid(jwt, clientId)) {
                    // Convertir les scopes OAuth2 en authorities Spring Security
                    Collection<GrantedAuthority> authorities = Arrays.stream(scopes.split(" "))
                        .map(scope -> new SimpleGrantedAuthority("SCOPE_" + scope))
                        .collect(Collectors.toList());

                    // Créer l'authentication avec le clientId comme principal
                    UsernamePasswordAuthenticationToken authToken = 
                        new UsernamePasswordAuthenticationToken(clientId, null, authorities);
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                } 
            }
        } catch (Exception e) {
            // Log l'erreur mais continue le filtrage
            logger.error("Erreur lors de l'authentification JWT", e);
        }
    }

    private void setAuthentication(String token) {
        Claims claims = jwtService.extractAllClaims(token);
        String clientId = claims.get("client_id", String.class);
        String scope = claims.get("scope", String.class);
        String authority = claims.get("authority", String.class); // Le rôle
        
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        
        // Ajouter le scope "access" si présent
        if ("access".equals(scope))
            authorities.add(new SimpleGrantedAuthority("SCOPE_access"));
        
        // Ajouter le rôle de l'utilisateur
        if (authority != null)
            authorities.add(new SimpleGrantedAuthority(authority));
        
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
            clientId, null, authorities
        );
        
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }
    
}
