package org.loamok.trocencheres.manager;

import org.loamok.trocencheres.entity.Utilisateur;

/**
 *
 * @author Huby Franck
 */
public interface userService {
    Utilisateur registerUser(Utilisateur u);
    Utilisateur registerUser(Utilisateur u, Boolean isAdmin);
    Boolean doCheckUserRegistering(Utilisateur u);
    Boolean checkPseudoUnique(String pseudo);
    Boolean checkEmailUnique(String email);
    Boolean checkPasswordCorrect(String password);
}
