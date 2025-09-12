package org.loamok.trocencheres.repository;

import java.util.List;
import org.loamok.trocencheres.entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

/**
 *
 * @author Huby Franck
 */
@RepositoryRestResource(collectionResourceRel = "utilisateurs", path = "utilisateurs")
public interface UtilisateurRepository extends JpaRepository<Utilisateur, String> {
    @Query("SELECT u FROM Utilisateur u WHERE u.email = :email")
    Utilisateur findByEmail(@Param("email") String email);
    @Query("SELECT u FROM Utilisateur u WHERE u.pseudo = :pseudo")
    Utilisateur findByPseudo(@Param("pseudo") String pseudo);
    Utilisateur findByPseudoAndPassword(@Param("pseudo") String pseudo, @Param("password") String password);
    @RestResource(path = "findByEmailContaining", rel = "findByEmailContaining")
    List<Utilisateur> findByEmailContaining(@Param("email") String email);
    @RestResource(path = "findByNomContaining", rel = "findByNomContaining") 
    List<Utilisateur> findByNomContaining(@Param("nom") String nom);
    @RestResource(path = "findByRole", rel = "findByRole")
    List<Utilisateur> findByRole_Role(@Param("role") String role);
}
