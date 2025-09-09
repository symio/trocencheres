package org.loamok.trocencheres.repository;

import org.loamok.trocencheres.entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * @author Huby Franck
 */
public interface UtilisateurRepository extends JpaRepository<Utilisateur, String> {
    
}
