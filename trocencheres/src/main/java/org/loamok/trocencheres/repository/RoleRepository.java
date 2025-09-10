package org.loamok.trocencheres.repository;

import org.loamok.trocencheres.entity.Role;
import org.loamok.trocencheres.entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 *
 * @author Huby Franck
 */
public interface RoleRepository extends JpaRepository<Role, Integer> {
    @Query
    Role findByRole(@Param("role") String role);
}
