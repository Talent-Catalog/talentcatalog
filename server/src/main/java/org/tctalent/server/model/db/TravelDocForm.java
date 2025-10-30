/*
 * Copyright (c) 2025 Talent Catalog.
 * [License boilerplate as in other files]
 */

package org.tctalent.server.model.db;

import jakarta.persistence.Transient;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;
import org.tctalent.server.security.AuthService;
import org.tctalent.server.service.db.CandidatePropertyService;
import org.tctalent.server.service.db.CandidateService;

/**
 * Form for personal travel document data entry + upload verification. Fields map to Ministry
 * requirements; uploads stored as CandidateDocument.
 */
@Getter
@Setter
public class TravelDocForm extends CandidateFormInstanceHelper {

    public TravelDocForm(String formName, AuthService authService,
        CandidateService candidateService,
        CandidatePropertyService propertyService) {
        super(formName, authService, candidateService, propertyService);
    }

    // Constants for property names
    private static final String DOC_TYPE_PROP = "travelDocType";
    private static final String DOC_NUMBER_PROP = "travelDocNumber";
    private static final String DOC_ISSUED_BY_PROP = "travelDocIssuedBy";
    private static final String DOC_ISSUE_DATE_PROP = "travelDocIssueDate";
    private static final String DOC_EXPIRY_DATE_PROP = "travelDocExpiryDate";

    public String getFormName() {
        return "TravelDocForm";
    }

    // Existing TC fields (direct access - update candidate if needed)
    public String getFirstName() {
        return getCandidate().getUser().getFirstName();
    }

    public void setFirstName(String firstName) {
        getCandidate().getUser().setFirstName(firstName);
    }

    public String getLastName() {
        return getCandidate().getUser().getLastName();
    }

    public void setLastName(String lastName) {
        getCandidate().getUser().setLastName(lastName);
    }

    public LocalDate getDateOfBirth() {
        return getCandidate().getDob();
    }

    public void setDateOfBirth(LocalDate dob) {
        getCandidate().setDob(dob);
    }

    public Gender getGender() {
        return getCandidate().getGender();
    }

    public void setGender(Gender gender) {
        getCandidate().setGender(gender);
    }

    @Transient
    public Country getBirthCountry() {
        return getCandidate().getBirthCountry();
    }


    public void setBirthCountry(Country country) {
        getCandidate().setBirthCountry(country);
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
