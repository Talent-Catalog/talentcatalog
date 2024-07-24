package org.tctalent.server.api.portal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.tctalent.server.model.db.CandidateExam;
import org.tctalent.server.request.candidate.exam.CreateCandidateExamRequest;
import org.tctalent.server.request.candidate.exam.UpdateCandidateExamsRequest;
import org.tctalent.server.service.db.CandidateExamService;
import org.tctalent.server.util.dto.DtoBuilder;

@RestController
@RequestMapping("/api/portal/candidate-exam")
public class CandidateExamPortalApi {

  private final CandidateExamService candidateExamService;

  @Autowired
  public CandidateExamPortalApi(CandidateExamService candidateExamService) {
    this.candidateExamService = candidateExamService;
  }

  @PostMapping("/{candidateId}")
  public Map<String, Object> createCandidateExam(@Valid @RequestBody CreateCandidateExamRequest request, @PathVariable Long candidateId) {
    CandidateExam createdExam = candidateExamService.createExam(candidateId,request);
    return candidateExamDto().build(createdExam);
  }

  @PostMapping("/update")
  public List<Map<String, Object>> createUpdateCandidateExams(@Valid @RequestBody UpdateCandidateExamsRequest request) {
    List<CandidateExam> candidateExams = candidateExamService.updateCandidateExam(request);
    return candidateExamDto().buildList(candidateExams);
  }

  @DeleteMapping
  public Map<String, Object> deleteCandidateExam(@RequestParam Long id) {
    boolean success = candidateExamService.deleteCandidateExam(id);
    return Map.of("success", success);
  }

  private DtoBuilder candidateExamDto() {
    return new DtoBuilder()
        .add("id")
        .add("exam")
        .add("otherExam")
        .add("score")
        .add("year")
        .add("notes");
  }
}
