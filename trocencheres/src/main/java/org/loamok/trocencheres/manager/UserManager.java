package org.loamok.trocencheres.manager;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.util.Optional;
import java.util.regex.Pattern;
import org.loamok.trocencheres.entity.Utilisateur;
import org.loamok.trocencheres.repository.RoleRepository;
import org.loamok.trocencheres.repository.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 *
 * @author Huby Franck
 */
@Service
public class UserManager implements userService {

    // Regex pour valider le mot de passe
    private static final String PASSWORD_PATTERN
            = "^(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).{8,20}$";
    private static final Pattern pattern = Pattern.compile(PASSWORD_PATTERN);
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Autowired
    private UtilisateurRepository uR;
    @Autowired
    private RoleRepository rR;
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public Utilisateur registerUser(Utilisateur u) {
        if (!doCheckUserRegistering(u)) 
            throw new RuntimeException("user is not unique or wrong password. : " + u.toString());

        try {
            Utilisateur user = Utilisateur.builder()
                    .pseudo(u.getPseudo())
                    .password("{bcrypt}" + passwordEncoder.encode(u.getPassword()))
                    .email(u.getEmail())
                    .nom(u.getNom())
                    .prenom(u.getPrenom())
                    .role(rR.findByRole("ROLE_USER"))
                    .build();
            uR.saveAndFlush(user);
            return user;
        } catch (RuntimeException e) {
            throw new RuntimeException("Last name and First name are mandatory parameters. : " + u.toString());
        }
    }

    @Override
    public Boolean doCheckUserRegistering(Utilisateur u) {
        System.out.println("=== DEBUG doCheckUserRegistering ===");
        System.out.println("Pseudo: " + u.getPseudo());
        System.out.println("Email: " + u.getEmail());
        System.out.println("Password: " + u.getPassword());

        Boolean isPseudoUnique = checkPseudoUnique(u.getPseudo());
        System.out.println("isPseudoUnique: " + isPseudoUnique);
        if (!isPseudoUnique) {
            return false;
        }

        Boolean isEmailUnique = checkEmailUnique(u.getEmail());
        System.out.println("isEmailUnique: " + isEmailUnique);
        if (!isEmailUnique) {
            return false;
        }

        Boolean isPasswordCorrect = checkPasswordCorrect(u.getPassword());
        System.out.println("isPasswordCorrect: " + isPasswordCorrect);
        if (!isPasswordCorrect) {
            return false;
        }

        System.out.println("Toutes les validations OK !");
        return true;
    }

    @Override
    @Transactional
    public Boolean checkPseudoUnique(String pseudo) {
        if (pseudo == null || pseudo.isBlank()) {
            System.out.println("DEBUG: pseudo null ou vide");
            return false;
        }

        entityManager.clear();
        final Utilisateur u = uR.findByPseudo(pseudo);
        System.out.println("DEBUG: findByPseudo résultat: " + u);

        Boolean isUnique = false;

        if (u == null) {
            isUnique = true;
        }

        return isUnique;
    }

    @Override
    @Transactional
    public Boolean checkEmailUnique(String email) {
        if (email == null || email.isBlank()) {
            System.out.println("DEBUG: email null ou vide");
            return false;
        }

        entityManager.clear();
        final Utilisateur u = uR.findByEmail(email);
        System.out.println("DEBUG: findByEmail résultat: " + u);

        Boolean isUnique = false;

        if (u == null) {
            isUnique = true;
        }

        return isUnique;
    }

    @Override
    public Boolean checkPasswordCorrect(String password) {
        if (password == null || password.isBlank()) {
            return false;
        }

        return pattern.matcher(password).matches();
    }

}
