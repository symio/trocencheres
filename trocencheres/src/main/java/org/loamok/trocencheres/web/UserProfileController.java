package org.loamok.trocencheres.web;

import lombok.AllArgsConstructor;
import org.loamok.trocencheres.entity.Utilisateur;
import org.loamok.trocencheres.manager.userService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Huby Franck
 */
@RestController
@RequestMapping("/profil")
@AllArgsConstructor
public class UserProfileController {
    
    private userService userManager;
    
    @PostMapping("/register")
//    public ResponseEntity<?> register(@Valid @RequestBody Utilisateur u) {
    public ResponseEntity<?> register(@RequestBody Utilisateur u) {
        try {
            final Utilisateur user = userManager.registerUser(u);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(e.getMessage());
        }
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/create")
    public ResponseEntity<?> adminCreate(@RequestBody Utilisateur u) {
        try {
            final Utilisateur user = userManager.registerUser(u, true);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(e.getMessage());
        }
    }
    
}
