package org.loamok.trocencheres.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
@Table(name = "ADDRESSES")
public class Adresse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "no_adresse")
    private Integer id;
    @Column(name = "rue", nullable = false, length = 100)
    private String rue;
    @Column(name = "code_postal", nullable = false, length = 10)
    private String codePostal;
    @Column(name = "city", nullable = false, length = 50)
    private String ville;
    @Column(name = "adresse_eni", nullable = false)
    private @Builder.Default Boolean adresseEni = false;
    
}
