/*
 * Copyright (c) 2024 Talent Catalog.
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

package org.tctalent.server.model.db;

import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

/**
 * Contains all relevant details and requirements of a visa pathway. Used for JOI's and visa eligbility checks.
 * Associated with a country, and fetched by country id for display on the front end.
 * todo create table on database
 *
 * @author Caroline Cameron
 */
//@Entity
//@Table(name = "visaPathway")
//@SequenceGenerator(name = "seq_gen", sequenceName = "visa_pathway_id_seq", allocationSize = 1)
@Getter
@Setter
public class VisaPathway {
  private String name;
  private String description;
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "country_id")
  private Country country;
  private Long age;
  private String language;
  private String empCommitment;
  private String inclusions;
  private String other;
  private String workExperience;
  private String education;
  private String educationCredential;
}
