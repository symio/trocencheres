package org.loamok.trocencheres.manager;

import org.loamok.trocencheres.entity.Adresse;

/**
 *
 * @author symio
 */
public interface AdresseService {
    Adresse registerAdresse(Adresse a);
    Boolean doCheckAdresseRegistering(Adresse a);
    Boolean checkRueFilled(String rue);
    Boolean checkCodePostalFilled(String codePostal);
    Boolean checkVilleFilled(String ville);
}
