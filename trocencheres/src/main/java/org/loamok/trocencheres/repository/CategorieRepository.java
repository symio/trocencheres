package org.loamok.trocencheres.repository;

import org.loamok.trocencheres.entity.Categorie;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * @author Huby Franck
 */
public interface CategorieRepository extends JpaRepository<Categorie, Integer> {
    
}
