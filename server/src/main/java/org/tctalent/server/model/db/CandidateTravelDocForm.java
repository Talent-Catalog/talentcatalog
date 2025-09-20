/*
 * Copyright (c) 2025 Talent Catalog.
 * [License boilerplate as in other files]
 */

package org.tctalent.server.model.db;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;
import org.tctalent.server.api.dto.CountryDto;

/**
 * Form for personal travel document data entry + upload verification.
 * Fields map to Ministry requirements; uploads stored as CandidateDocument.
 */
@Getter
@Setter
@Entity
@Table(name = "candidate_form_instance")
public class CandidateTravelDocForm extends CandidateFormInstance {

  // Constants for property names
  private static final String DOC_TYPE_PROP = "travelDocType";
  private static final String DOC_NUMBER_PROP = "travelDocNumber";
  private static final String DOC_ISSUED_BY_PROP = "travelDocIssuedBy";
  private static final String DOC_ISSUE_DATE_PROP = "travelDocIssueDate";
  private static final String DOC_EXPIRY_DATE_PROP = "travelDocExpiryDate";

  @Override
  public String getFormName() {
    return "ItalyCandidateTravelDocumentForm";
  }

  // Existing TC fields (direct access - update candidate if needed)
  public String getFirstName() {
    return getWorkingCandidate().getUser().getFirstName();
  }

  public void setFirstName(String firstName) {
    getWorkingCandidate().getUser().setFirstName(firstName);
  }

  public String getLastName() {
    return getWorkingCandidate().getUser().getLastName();
  }

  public void setLastName(String lastName) {
    getWorkingCandidate().getUser().setLastName(lastName);
  }

  public LocalDate getDateOfBirth() {
    return getWorkingCandidate().getDob();
  }

  public void setDateOfBirth(LocalDate dob) {
    getWorkingCandidate().setDob(dob);
  }

  public Gender getGender() {
    return getWorkingCandidate().getGender();
  }

  public void setGender(Gender gender) {
    getWorkingCandidate().setGender(gender);
  }

  @Transient
  public CountryDto getBirthCountry() {
       Country country = getWorkingCandidate().getBirthCountry();
       if (country != null) {
          CountryDto dto = new CountryDto();
          dto.setIsoCode(country.getIsoCode());
          dto.setName(country.getName());
          dto.setStatus(country.getStatus());
          return dto;
       }
       return null;
  }


  public void setBirthCountry(Country country) {
    //TODO JC This needs to effectively just look up country from a possible new id.
    //TODO JC The problem is that we need a service to look up a country and we don't have
    //todo jc access to that from within an entity.

    //todo Look into having partial controller
//    getWorkingCandidate().setBirthCountry(country);
  }

  public String getPlaceOfBirth() {
    return getProperty("placeOfBirth");
  }

  public void setPlaceOfBirth(String place) {
    setProperty("placeOfBirth", place);
  }

  // New properties
  public TravelDocType getTravelDocType() {
    String value = getProperty(DOC_TYPE_PROP);
    return value != null ? TravelDocType.valueOf(value) : null;
  }

  public void setTravelDocType(TravelDocType type) {
    setProperty(DOC_TYPE_PROP, type != null ? type.name() : null);
  }

  public String getTravelDocNumber() {
    return getProperty(DOC_NUMBER_PROP);
  }

  public void setTravelDocNumber(String number) {
    setProperty(DOC_NUMBER_PROP, number);
  }

  public String getTravelDocIssuedBy() {
    return getProperty(DOC_ISSUED_BY_PROP);
  }

  public void setTravelDocIssuedBy(String issuedBy) {
    setProperty(DOC_ISSUED_BY_PROP, issuedBy);
  }

  public LocalDate getTravelDocIssueDate() {
    String dateStr = getProperty(DOC_ISSUE_DATE_PROP);
    return dateStr != null ? LocalDate.parse(dateStr) : null;
  }

  public void setTravelDocIssueDate(LocalDate date) {
    setProperty(DOC_ISSUE_DATE_PROP, date != null ? date.toString() : null);
  }

  public LocalDate getTravelDocExpiryDate() {
    String dateStr = getProperty(DOC_EXPIRY_DATE_PROP);
    return dateStr != null ? LocalDate.parse(dateStr) : null;
  }

  public void setTravelDocExpiryDate(LocalDate date) {
    setProperty(DOC_EXPIRY_DATE_PROP, date != null ? date.toString() : null);
  }
}
