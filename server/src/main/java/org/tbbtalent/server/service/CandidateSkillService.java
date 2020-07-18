package org.tbbtalent.server.service;

import org.springframework.data.domain.Page;
import org.tbbtalent.server.model.db.CandidateSkill;
import org.tbbtalent.server.request.skill.SearchCandidateSkillRequest;

public interface CandidateSkillService {

    Page<CandidateSkill> searchCandidateSkills(SearchCandidateSkillRequest request);



}
