package org.loamok.trocencheres.repository;

import java.util.List;
import org.loamok.trocencheres.entity.Adresse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

/**
 *
 * @author Huby Franck
 */
@RepositoryRestResource(collectionResourceRel = "addresses", path = "addresses")
public interface AdresseRepository extends JpaRepository<Adresse, Integer> {
    @Query
    List<Adresse> findByVille(@Param("ville") String ville);
}
