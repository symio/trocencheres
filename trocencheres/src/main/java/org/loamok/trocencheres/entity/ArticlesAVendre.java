/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.loamok.trocencheres.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 *
 * @author Huby Franck
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
@Entity
@Table(name = "ARTICLES_A_VENDRE")
public class ArticlesAVendre {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "no_article")
    private Integer id;
    @Column(name = "nom_article", nullable = false, length = 30)
    private String nom;
    @Column(name = "description", nullable = false, length = 300)
    private String description;
    @Column(name = "photo", nullable = false)
    private Integer photo;
    @Column(name = "date_debut_encheres", nullable = false)
    private LocalDateTime dateDebutEncheres;
    @Column(name = "date_fin_encheres", nullable = false)
    private LocalDateTime dateFinEncheres;
    @Column(name = "statut_enchere", nullable = false)
    private Integer statutEnchere;
    @Column(name = "prix_initial", nullable = false)
    private Integer prixInitial;
    @Column(name = "prix_vente", nullable = true)
    private Integer prixVente;
}
