package org.tbbtalent.server.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.tbbtalent.server.model.db.CandidateSkill;
import org.tbbtalent.server.repository.db.CandidateSkillRepository;
import org.tbbtalent.server.request.skill.SearchCandidateSkillRequest;
import org.tbbtalent.server.service.CandidateSkillService;

@Service
public class CandidateSkillServiceImpl implements CandidateSkillService {

    private final CandidateSkillRepository candidateSkillRepository;

    @Autowired
    public CandidateSkillServiceImpl(CandidateSkillRepository candidateSkillRepository) {
        this.candidateSkillRepository = candidateSkillRepository;
    }

    @Override
    public Page<CandidateSkill> searchCandidateSkills(SearchCandidateSkillRequest request) {
        return candidateSkillRepository.findByCandidateId(request.getCandidateId(), request.getPageRequest());
    }

  
}
