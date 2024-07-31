package org.tctalent.server.api.portal;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.tctalent.server.api.admin.ITableApi;
import org.tctalent.server.exception.EntityExistsException;
import org.tctalent.server.exception.InvalidRequestException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.CandidateExam;
import org.tctalent.server.request.candidate.exam.CreateCandidateExamRequest;
import org.tctalent.server.request.candidate.exam.UpdateCandidateExamsRequest;
import org.tctalent.server.service.db.CandidateExamService;
import org.tctalent.server.util.dto.DtoBuilder;

@RestController
@RequestMapping("/api/portal/candidate-exam")
public class CandidateExamPortalApi implements ITableApi<CreateCandidateExamRequest,UpdateCandidateExamsRequest,UpdateCandidateExamsRequest> {

  private final CandidateExamService candidateExamService;

  @Autowired
  public CandidateExamPortalApi(CandidateExamService candidateExamService) {
    this.candidateExamService = candidateExamService;
  }

  @PostMapping("/{candidateId}")
  public @NotNull Map<String, Object> createCandidateExam(@Valid @RequestBody CreateCandidateExamRequest request, @PathVariable Long candidateId)
      throws InvalidRequestException, NoSuchObjectException {
    CandidateExam createdExam = candidateExamService.createExam(candidateId,request);
    return candidateExamDto().build(createdExam);
  }

  @PutMapping("/update")
  public @NotNull List<Map<String, Object>> createUpdateCandidateExams(@Valid @RequestBody UpdateCandidateExamsRequest request)
      throws EntityExistsException, InvalidRequestException, NoSuchObjectException {
    List<CandidateExam> candidateExams = candidateExamService.updateCandidateExam(request);
    return candidateExamDto().buildList(candidateExams);
  }

  @DeleteMapping
  public @NotNull Map<String, Object> deleteCandidateExam(@RequestParam Long id) {
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
