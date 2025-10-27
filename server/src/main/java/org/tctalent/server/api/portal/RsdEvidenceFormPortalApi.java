/*
 * Copyright (c) 2025 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */


package org.tctalent.server.api.portal;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tctalent.server.exception.InvalidSessionException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.RsdEvidenceForm;
import org.tctalent.server.request.form.RsdEvidenceFormData;
import org.tctalent.server.security.AuthService;
import org.tctalent.server.service.db.CandidateFormInstanceService;
import org.tctalent.server.service.db.CandidateService;
import org.tctalent.server.util.dto.DtoBuilder;

@RestController
@RequestMapping("/api/portal/form/rsd-evidence-form")
@RequiredArgsConstructor
public class RsdEvidenceFormPortalApi {

  private final AuthService authService;
  private final CandidateFormInstanceService formService;
  private final CandidateService candidateService;

  @PostMapping
  @NotNull
  public Map<String, Object> createOrUpdate(@Valid @RequestBody RsdEvidenceFormData request) {
    Candidate candidate = getLoggedInCandidate();
    RsdEvidenceForm form = formService.createOrUpdateRsdEvidenceForm(candidate, request);
    return rsdEvidenceFormDto().build(form);
  }

  @GetMapping
  @NotNull
  public Map<String, Object> get() {
    Candidate candidate = getLoggedInCandidate();
    RsdEvidenceForm form = formService.getRsdEvidenceForm(candidate)
        .orElseThrow(() -> new NoSuchObjectException("No RsdEvidenceForm found"));
    return rsdEvidenceFormDto().build(form);
  }

  private Candidate getLoggedInCandidate() {
    Long loggedInCandidateId = authService.getLoggedInCandidateId();
    if (loggedInCandidateId == null) {
      throw new InvalidSessionException("Not logged in");
    }
    return candidateService.getCandidate(loggedInCandidateId);
  }

  private DtoBuilder rsdEvidenceFormDto() {
    return new DtoBuilder()
        .add("refugeeStatus")
        .add("documentType")
        .add("documentNumber");
  }
}