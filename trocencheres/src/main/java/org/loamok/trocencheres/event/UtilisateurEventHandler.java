package org.loamok.trocencheres.event;

import org.loamok.trocencheres.entity.Utilisateur;
import org.loamok.trocencheres.manager.AddressManager;
import org.loamok.trocencheres.manager.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 *
 * @author Huby Franck
 */
@Component
@RepositoryEventHandler(Utilisateur.class)
public class UtilisateurEventHandler {

    @Autowired
    private UserManager userManager;
    @Autowired
    private AddressManager addressManager;

    @HandleBeforeCreate
    public void handleUtilisateurCreate(Utilisateur utilisateur) {
        if (!userManager.doCheckUserRegistering(utilisateur))
            throw new RuntimeException("Utilisateur invalide");
        
        if (!addressManager.doCheckAdresseRegistering(utilisateur.getAdresse()))
            throw new RuntimeException("Adresse invalide");
        
        if (utilisateur.getPassword() != null && !utilisateur.getPassword().startsWith("{bcrypt}")) {
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            utilisateur.setPassword("{bcrypt}" + encoder.encode(utilisateur.getPassword()));
        }
    }
}
