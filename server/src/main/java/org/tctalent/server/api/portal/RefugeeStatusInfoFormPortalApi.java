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
import org.tctalent.server.model.db.RefugeeStatusInfoForm;
import org.tctalent.server.request.form.RefugeeStatusInfoFormData;
import org.tctalent.server.security.AuthService;
import org.tctalent.server.service.db.CandidatePropertyService;
import org.tctalent.server.service.db.CandidateService;
import org.tctalent.server.util.dto.DtoBuilder;

@RestController
@RequestMapping("/api/portal/form/refugee-status-info-form")
@RequiredArgsConstructor
public class RefugeeStatusInfoFormPortalApi {

  private final AuthService authService;
  private final CandidateService candidateService;
  private final CandidatePropertyService candidatePropertyService;


  @PostMapping
  @NotNull
  public Map<String, Object> createOrUpdate(@Valid @RequestBody RefugeeStatusInfoFormData request) {

    RefugeeStatusInfoForm form = new RefugeeStatusInfoForm(
        "RefugeeStatusInfoForm", authService, candidateService, candidatePropertyService);

    form.setRefugeeStatus(request.getRefugeeStatus());
    form.setDocumentType(request.getDocumentType());
    form.setDocumentNumber(request.getDocumentNumber());
    form.save();

    return refugeeStatusInfoFormDto().build(form);
  }

  @GetMapping
  @NotNull
  public Map<String, Object> get() {
    RefugeeStatusInfoForm form = new RefugeeStatusInfoForm(
        "RefugeeStatusInfoForm", authService, candidateService, candidatePropertyService);
    return refugeeStatusInfoFormDto().build(form);
  }


  private DtoBuilder refugeeStatusInfoFormDto() {
    return new DtoBuilder()
        .add("refugeeStatus")
        .add("documentType")
        .add("documentNumber");
  }
}

