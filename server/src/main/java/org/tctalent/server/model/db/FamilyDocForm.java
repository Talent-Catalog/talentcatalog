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

/*
 * Copyright ...
 */
package org.tctalent.server.model.db;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Task 2 form: stores relocating family members (as JSON) and the "no eligible" toggle
 * using candidate properties, consistent with the MySecondForm approach.
 */
@Entity
@Table(name = "candidate_form_instance")
public class FamilyDocForm extends CandidateFormInstance {

  private static final String FAMILY_MEMBERS_JSON = "familyMembersJson";
  private static final String NO_ELIGIBLE = "noEligibleFamilyMembers";
  private static final String NO_ELIGIBLE_NOTES = "noEligibleNotes";

  @Override
  public String getFormName() {
    return "FamilyDocForm";
  }

  @JsonProperty("familyMembersJson")
  public String getFamilyMembersJson() {
    return getProperty(FAMILY_MEMBERS_JSON);
  }

  @JsonProperty("familyMembersJson")
  public void setFamilyMembersJson(String json) {
    setProperty(FAMILY_MEMBERS_JSON, json);
  }

  @JsonProperty("noEligibleFamilyMembers")
  public Boolean getNoEligibleFamilyMembers() {
    String v = getProperty(NO_ELIGIBLE);
    return v == null ? null : Boolean.valueOf(v);
  }

  @JsonProperty("noEligibleFamilyMembers")
  public void setNoEligibleFamilyMembers(Boolean value) {
    setProperty(NO_ELIGIBLE, value == null ? null : value.toString());
  }

  @JsonProperty("noEligibleNotes")
  public String getNoEligibleNotes() {
    return getProperty(NO_ELIGIBLE_NOTES);
  }

  @JsonProperty("noEligibleNotes")
  public void setNoEligibleNotes(String notes) {
    setProperty(NO_ELIGIBLE_NOTES, notes);
  }
}
