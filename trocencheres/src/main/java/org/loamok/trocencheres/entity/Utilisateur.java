package org.loamok.trocencheres.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 *
 * @author Huby Franck
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"pseudo"})
@ToString(of = {"pseudo", "nom", "prenom", "email"})
@Inheritance(strategy = InheritanceType.JOINED)
@SuperBuilder
@Entity
@Table(name = "UTILISATEURS", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"pseudo", "email"})})
public class Utilisateur implements UserDetails {
    @Id
    @Column(name = "pseudo", length = 30)
    private String pseudo;
    @Column(name = "nom", nullable = false, length = 40)
    private String nom;
    @Column(name = "prenom", nullable = false, length = 50)
    private String prenom;
    @Column(name = "email", nullable = false, length = 100)
    private String email;
    @Column(name = "telephone", nullable = true, length = 15)
    private String telephone;
    @Column(name = "credit", nullable = false)
    private @Builder.Default
    Integer credit = 10;
    // -- relations
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "no_adresse")
    private Adresse adresse;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_role", nullable = false)
    private Role role;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "mot_de_passe", nullable = false, length = 69)
    private String password;

//    @JsonIgnore
    private String authority;
    @JsonIgnore
    private Boolean isAdmin;

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        authority = role.getRole();
        isAdmin = role.getIsAdmin();
        return Arrays.asList(new SimpleGrantedAuthority(authority));
    }

    public String getAuthority() {
        return role.getRole();
    }
    
    @Override
    public String getUsername() {
        return pseudo;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
