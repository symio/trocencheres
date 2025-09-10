package org.loamok.trocencheres.repository;

import org.loamok.trocencheres.entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 *
 * @author Huby Franck
 */
public interface UtilisateurRepository extends JpaRepository<Utilisateur, String> {
    @Query("SELECT u FROM Utilisateur u WHERE u.email = :email")
    Utilisateur findByEmail(@Param("email") String email);
    @Query("SELECT u FROM Utilisateur u WHERE u.pseudo = :pseudo")
    Utilisateur findByPseudo(@Param("pseudo") String pseudo);
    Utilisateur findByPseudoAndPassword(@Param("pseudo") String pseudo, @Param("password") String password);
}
