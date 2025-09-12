package org.loamok.trocencheres.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
@Table(name = "ENCHERES", uniqueConstraints = { @UniqueConstraint(columnNames = { "id_utilisateur", "no_article", "montant_enchere" }) })
public class Enchere {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_enchere")
    private Integer id;
    @Column(name = "montant_enchere", nullable = false)
    private Integer montantEnchere;
    @Column(name = "date_enchere", nullable = false)
    private LocalDateTime dateEnchere;
    // -- relations
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_utilisateur")
    private Utilisateur utilisateur;
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "no_article")
    private ArticlesAVendre article;
}
