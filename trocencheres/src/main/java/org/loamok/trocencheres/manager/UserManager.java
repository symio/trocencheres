package org.loamok.trocencheres.manager;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.util.Optional;
import java.util.regex.Pattern;
import org.loamok.trocencheres.entity.Adresse;
import org.loamok.trocencheres.entity.Role;
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
    @Autowired
    private AdresseService aS;
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Utilisateur registerUser(Utilisateur u) {
        return registerUser(u, Boolean.FALSE);
    }
    
    @Override
    @Transactional
    public Utilisateur registerUser(Utilisateur u, Boolean isAdmin) {
        Role roleUser =null;
        
        if(!isAdmin) {
            roleUser = rR.findByRole("ROLE_USER");
        } else {
            if(u.getRole() == null || u.getRole().getRole() == null | u.getRole().getRole().isBlank())
                throw new RuntimeException("user must have a Role but user.role is null.");
            roleUser = rR.findByRole(u.getRole().getRole());
        }
        
        Adresse a = null;
        
        if(u.getAdresse() == null)
            throw new RuntimeException("user must have an address but user.address is null.");
            
        try {
            a = aS.registerAdresse(u.getAdresse());
        } catch (RuntimeException e) {
            throw e;
        }
        
        Utilisateur user = Utilisateur.builder()
                .pseudo(u.getPseudo())
                .password(u.getPassword())
                .email(u.getEmail())
                .nom(u.getNom())
                .prenom(u.getPrenom())
                .telephone(u.getTelephone())
                .role(roleUser)
                .adresse(a)
                .build();
        
        if(!doCheckUserRegistering(user)) 
            throw new RuntimeException("user is not unique or wrong password. : " + user.toString());
        
        user.setPassword("{bcrypt}" + passwordEncoder.encode(u.getPassword()));
        
        try {
            entityManager.persist(user);
            entityManager.flush();
            return user;
        } catch (RuntimeException e) {
//            throw new RuntimeException("Error registering user : " + e.getMessage(), e);
            throw new RuntimeException("Last name and First name are mandatory parameters. : " + user.toString());
        }
    }

    @Override
    public Boolean doCheckUserRegistering(Utilisateur u) {
        Boolean isPseudoUnique = checkPseudoUnique(u.getPseudo());
        
        if(!isPseudoUnique) 
            return false;

        Boolean isEmailUnique = checkEmailUnique(u.getEmail());
        
        if(!isEmailUnique) 
            return false;

        Boolean isPasswordCorrect = checkPasswordCorrect(u.getPassword());
        
        if(!isPasswordCorrect) 
            return false;

        return true;
    }

    @Override
    @Transactional
    public Boolean checkPseudoUnique(String pseudo) {
        if(pseudo == null || pseudo.isBlank()) 
            return false;

        final Utilisateur u = uR.findByPseudo(pseudo);

        Boolean isUnique = false;

        if(u == null) 
            isUnique = true;

        return isUnique;
    }

    @Override
    @Transactional
    public Boolean checkEmailUnique(String email) {
        if(email == null || email.isBlank()) 
            return false;

        final Utilisateur u = uR.findByEmail(email);

        Boolean isUnique = false;

        if(u == null)
            isUnique = true;

        return isUnique;
    }

    @Override
    public Boolean checkPasswordCorrect(String password) {
        if(password == null || password.isBlank()) 
            return false;

        return pattern.matcher(password).matches();
    }

}
