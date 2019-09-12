package org.tbbtalent.server.service;

import org.tbbtalent.server.model.Profession;
import org.tbbtalent.server.request.profession.CreateProfessionRequest;

public interface ProfessionService {

    Profession createProfession(CreateProfessionRequest request);

    void deleteProfession(Long id);
}
