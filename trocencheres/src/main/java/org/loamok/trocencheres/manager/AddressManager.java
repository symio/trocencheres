package org.loamok.trocencheres.manager;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.loamok.trocencheres.entity.Adresse;
import org.springframework.stereotype.Service;

/**
 *
 * @author Huby Franck
 */
@Service
public class AddressManager implements AdresseService {

    @PersistenceContext
    private EntityManager entityManager;
    
    @Override
    @Transactional
    public Adresse registerAdresse(Adresse a) {
        Adresse adresse = Adresse.builder()
            .rue(a.getRue())
            .codePostal(a.getCodePostal())
            .ville(a.getVille())
            .build();
        
        if(!doCheckAdresseRegistering(adresse)) 
            throw new RuntimeException("address is not filled, all parameters are mandatory. : " + adresse.toString());
        
        try {
            entityManager.persist(adresse);
            entityManager.flush();

            return adresse;
        } catch (RuntimeException e) {
            throw new RuntimeException("Error registering address. : " + adresse.toString());
        }
    }

    @Override
    public Boolean doCheckAdresseRegistering(Adresse a) {
        Boolean isRueValid = checkRueFilled(a.getRue());
        Boolean isCodePostalValid = checkCodePostalFilled(a.getCodePostal());
        Boolean isVilleValid = checkVilleFilled(a.getVille());
        
        return isRueValid && isCodePostalValid && isVilleValid;
    }

    @Override
    public Boolean checkRueFilled(String rue) {
        if(rue == null || rue.isBlank())
            return false;
        
        return true;
    }

    @Override
    public Boolean checkCodePostalFilled(String codePostal) {
        if(codePostal == null || codePostal.isBlank())
            return false;
        
        return true;
    }

    @Override
    public Boolean checkVilleFilled(String ville) {
        if(ville == null || ville.isBlank())
            return false;
        
        return true;
    }
    
}
