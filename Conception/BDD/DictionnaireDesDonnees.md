# Dictionnaire des Données - ENCHERES_DB

## Liste des entités - ENCHERES_DB

1. **ADRESSES**  
   Stocke les informations d’adresses postales (utilisateurs, lieux de retrait).  

2. **CATEGORIES**  
   Référence les catégories d’articles (ex : Informatique, Meubles, etc.).  

3. **UTILISATEURS**  
   Contient les informations des utilisateurs inscrits (identité, email, mot de passe, rôle administrateur éventuel).  

4. **ARTICLES_A_VENDRE**  
   Regroupe les articles mis en vente avec leurs détails, leur vendeur, leur catégorie et leur adresse de retrait.  

5. **ENCHERES**  
   Trace les enchères réalisées sur les articles avec un identifiant unique, le montant et la date.  

6. **ROLES**  
   Définit les rôles disponibles dans l’application (user, admin), associés à un identifiant unique.  

---

## 1. Entité ADRESSES

| Champ        | Type      | Taille | Contraintes                  | Description                          |
|--------------|-----------|--------|------------------------------|--------------------------------------|
| no_adresse   | INTEGER   | -      | PK, IDENTITY, NOT NULL       | Identifiant unique de l'adresse      |
| rue          | VARCHAR   | 100    | NOT NULL                     | Rue                                  |
| code_postal  | VARCHAR   | 10     | NOT NULL                     | Code postal                          |
| ville        | VARCHAR   | 50     | NOT NULL                     | Ville                                |
| adresse_eni  | BIT       | -      | NOT NULL, DEFAULT 0          | Indique si c'est une adresse ENI     |

---

## 2. Entité CATEGORIES

| Champ        | Type    | Taille | Contraintes                  | Description               |
|--------------|---------|--------|------------------------------|---------------------------|
| no_categorie | INTEGER | -      | PK, IDENTITY, NOT NULL       | Identifiant catégorie     |
| libelle      | VARCHAR | 30     | NOT NULL, UNIQUE             | Nom de la catégorie       |

---

## 3. Entité UTILISATEURS

| Champ         | Type    | Taille | Contraintes                            | Description                       |
|---------------|---------|--------|----------------------------------------|-----------------------------------|
| pseudo        | VARCHAR | 30     | PK, NOT NULL, UNIQUE (avec email)      | Identifiant utilisateur           |
| nom           | VARCHAR | 40     | NOT NULL                               | Nom de famille                    |
| prenom        | VARCHAR | 50     | NOT NULL                               | Prénom                            |
| email         | VARCHAR | 100    | NOT NULL, UNIQUE (avec pseudo)         | Adresse email                     |
| telephone     | VARCHAR | 15     | NULL                                   | Numéro de téléphone               |
| mot_de_passe  | VARCHAR | 68     | NOT NULL                               | Mot de passe chiffré              |
| credit        | INTEGER | -      | NOT NULL, DEFAULT 10                   | Crédits disponibles               |
| no_adresse    | INTEGER | -      | NOT NULL, FK → ADRESSES(no_adresse)    | Lien vers l’adresse de l’utilisateur |
| id_role       | INTEGER | -      | NOT NULL, FK → ROLES(id_role)          | Lien vers le rôle de l’utilisateur |

---

---

## 4. Entité ARTICLES_A_VENDRE

| Champ              | Type    | Taille | Contraintes                                    | Description                          |
|--------------------|---------|--------|------------------------------------------------|--------------------------------------|
| no_article         | INTEGER | -      | PK, IDENTITY, NOT NULL                         | Identifiant de l’article             |
| nom_article        | VARCHAR | 30     | NOT NULL                                       | Nom de l’article                      |
| description        | VARCHAR | 300    | NOT NULL                                       | Description détaillée                 |
| photo              | INTEGER | -      | NULL                                           | Identifiant photo (optionnel)         |
| date_debut_encheres| DATE    | -      | NOT NULL                                       | Date de début de l’enchère           |
| date_fin_encheres  | DATE    | -      | NOT NULL                                       | Date de fin de l’enchère             |
| statut_enchere     | INTEGER | -      | NOT NULL, DEFAULT 0                            | Statut (0=pas commencée,1=active,2=clôturée,3=livrée,100=annulée) |
| prix_initial       | INTEGER | -      | NOT NULL                                       | Prix de départ                        |
| prix_vente         | INTEGER | -      | NULL                                           | Prix final (si vendu)                 |
| id_utilisateur     | VARCHAR | 30     | NOT NULL, FK → UTILISATEURS(pseudo)            | Utilisateur vendeur                   |
| no_categorie       | INTEGER | -      | NOT NULL, FK → CATEGORIES(no_categorie)        | Catégorie de l’article                |
| no_adresse_retrait | INTEGER | -      | NOT NULL, FK → ADRESSES(no_adresse)            | Adresse de retrait de l’article       |

## 5. Entité ENCHERES

| Champ           | Type     | Taille | Contraintes                                                                          | Description                     |
|-----------------|----------|--------|--------------------------------------------------------------------------------------|---------------------------------|
| id_enchere      | INTEGER  | -      | PK, IDENTITY, NOT NULL                                                               | Identifiant unique de l’enchère |
| id_utilisateur  | VARCHAR  | 30     | NOT NULL, FK → UTILISATEURS(pseudo), UNIQUE (+ montant_enchere, id_utilisateur)      | Utilisateur enchérisseur        |
| no_article      | INTEGER  | -      | NOT NULL, FK → ARTICLES_A_VENDRE(no_article), UNIQUE (+ montant_enchere, no_article) | Article concerné                |
| montant_enchere | INTEGER  | -      | NOT NULL, UNIQUE (+ id_utilisateur, no_article)                                      | Montant de l’enchère            |
| date_enchere    | DATETIME | -      | NOT NULL                                                                             | Date de l’enchère               |

---

## 6. Entité ROLES

| Champ    | Type     | Taille | Contraintes                  | Description                 |
|----------|----------|--------|------------------------------|-----------------------------|
| id_role  | INTEGER  | -      | PK, IDENTITY, NOT NULL       | Identifiant unique du rôle  |
| ROLE     | NVARCHAR | 50     | NOT NULL, UNIQUE (+IS_ADMIN) | Nom du rôle (ex: USER/ADMIN)|
| IS_ADMIN | INT      | -      | NOT NULL, UNIQUE (+ROLE)     | Indique si admin (0/1)      |


---

# Relations entre entités - ENCHERES_DB

| Table source        | Colonne FK          | Table cible      | Colonne PK      | Type de relation       |
|---------------------|---------------------|------------------|-----------------|------------------------|
| UTILISATEURS        | no_adresse          | ADRESSES         | no_adresse      | Plusieurs utilisateurs → une adresse |
| UTILISATEURS        | id_role             | ROLES            | id_role         | Plusieurs utilisateurs → une role    |
| ARTICLES_A_VENDRE   | id_utilisateur      | UTILISATEURS     | pseudo          | Plusieurs articles → un utilisateur  |
| ARTICLES_A_VENDRE   | no_categorie        | CATEGORIES       | no_categorie    | Plusieurs articles → une catégorie   |
| ARTICLES_A_VENDRE   | no_adresse_retrait  | ADRESSES         | no_adresse      | Plusieurs articles → une adresse de retrait |
| ENCHERES            | id_utilisateur      | UTILISATEURS     | pseudo          | Plusieurs enchères → un utilisateur  |
| ENCHERES            | no_article          | ARTICLES_A_VENDRE| no_article      | Plusieurs enchères → un article      |
