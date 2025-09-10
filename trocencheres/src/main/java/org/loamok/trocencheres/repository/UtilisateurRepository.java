package org.loamok.trocencheres.repository;

import org.loamok.trocencheres.entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

/**
 *
 * @author Huby Franck
 */
public interface UtilisateurRepository extends JpaRepository<Utilisateur, String> {
    Utilisateur findByPseudo(@Param("pseudo") String pseudo);
    Utilisateur findByPseudoAndPassword(@Param("pseudo") String pseudo, @Param("password") String password);
}
