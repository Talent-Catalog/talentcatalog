package org.tbbtalent.server.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tbbtalent.server.exception.InvalidCredentialsException;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.model.Candidate;
import org.tbbtalent.server.model.Industry;
import org.tbbtalent.server.model.Profession;
import org.tbbtalent.server.repository.IndustryRepository;
import org.tbbtalent.server.repository.ProfessionRepository;
import org.tbbtalent.server.request.profession.CreateProfessionRequest;
import org.tbbtalent.server.security.UserContext;
import org.tbbtalent.server.service.ProfessionService;

@Service
public class ProfessionServiceImpl implements ProfessionService {

    private final ProfessionRepository professionRepository;
    private final IndustryRepository industryRepository;
    private final UserContext userContext;

    @Autowired
    public ProfessionServiceImpl(ProfessionRepository professionRepository,
                                 IndustryRepository industryRepository,
                                 UserContext userContext) {
        this.professionRepository = professionRepository;
        this.industryRepository = industryRepository;
        this.userContext = userContext;
    }


    @Override
    public Profession createProfession(CreateProfessionRequest request) {
        Candidate candidate = userContext.getLoggedInCandidate();

        // Load the industry from the database - throw an exception if not found
        Industry industry = industryRepository.findById(request.getIndustryId())
                .orElseThrow(() -> new NoSuchObjectException(Industry.class, request.getIndustryId()));

        // Create a new profession object to insert into the database
        Profession profession = new Profession();
        profession.setCandidate(candidate);
        profession.setIndustry(industry);
        profession.setYearsExperience(request.getYearsExperience());

        // Save the profession
        return professionRepository.save(profession);
    }

    @Override
    public void deleteProfession(Long id) {
        Candidate candidate = userContext.getLoggedInCandidate();
        Profession profession = professionRepository.findByIdLoadCandidate(id)
                .orElseThrow(() -> new NoSuchObjectException(Profession.class, id));

        // Check that the user is deleting their own profession
        if (!candidate.getId().equals(profession.getCandidate().getId())) {
            throw new InvalidCredentialsException("You do not have permission to perform that action");
        }

        professionRepository.delete(profession);
    }
}
