package org.tctalent.server.service.db.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.BreakType;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.UnderlinePatterns;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.tctalent.server.logging.LogBuilder;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateCertification;
import org.tctalent.server.model.db.CandidateEducation;
import org.tctalent.server.model.db.CandidateJobExperience;
import org.tctalent.server.model.db.CandidateLanguage;

/**
 * Helper component for generating candidate CV documents in DOCX format.
 *
 * <p>This class builds a Word document using Apache POI and populates it with candidate
 * profile data such as personal details, work experience, education, certifications,
 * and languages.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DocxHelper {

  /** Prepares candidate data before it is exported to DOCX. */
  private final CvExportDataPreparer cvExportDataPreparer;

  /** Formatter used to convert candidate data into display-ready text for DOCX export. */
  private final CvDocxFormatter cvDocxFormatter;

  /**
   * Generates a DOCX CV for the given candidate.
   *
   * <p>The generated document includes a title, personal details, experience,
   * education, certifications, and languages. Candidate data is first prepared
   * for export before being written into the document.
   *
   * @param candidate the candidate whose CV should be generated
   * @param showName whether the candidate's display name should be shown in the title
   * @param showContact whether the candidate's contact details should be included
   * @return a {@link Resource} containing the generated DOCX file
   * @throws RuntimeException if an I/O error occurs while generating the DOCX document
   */
  public Resource generateDocx(Candidate candidate, Boolean showName, Boolean showContact) {
    try (XWPFDocument document = new XWPFDocument();
        ByteArrayOutputStream out = new ByteArrayOutputStream()) {

      candidate = cvExportDataPreparer.prepare(candidate, showContact);

      addTitle(document, candidate, showName);
      addPersonalBlock(document, candidate, showContact);
      addExperienceSection(document, candidate);
      addEducationSection(document, candidate);
      addCertificationSection(document, candidate);
      addLanguageSection(document, candidate);

      document.write(out);
      return new ByteArrayResource(out.toByteArray());
    } catch (IOException e) {
      LogBuilder.builder(log)
          .action("generateDocx")
          .message("Error generating DOCX CV")
          .logError(e);

      throw new RuntimeException("Error generating DOCX CV", e);
    }
  }

  /**
   * Adds the document title.
   *
   * <p>If {@code showName} is {@code true} and the candidate has a display name,
   * that name is used as the title. Otherwise, a generic title is used.
   *
   * @param document the DOCX document
   * @param candidate the candidate being exported
   * @param showName whether the candidate's display name should be shown
   */
  private void addTitle(XWPFDocument document, Candidate candidate, Boolean showName) {
    XWPFParagraph p = document.createParagraph();
    p.setAlignment(ParagraphAlignment.CENTER);

    XWPFRun run = p.createRun();
    run.setBold(true);
    run.setFontSize(16);

    if (Boolean.TRUE.equals(showName)
        && candidate.getUser() != null
        && candidate.getUser().getDisplayName() != null) {
      run.setText(candidate.getUser().getDisplayName());
    } else {
      run.setText("Candidate CV");
    }

    run.addBreak();
  }

  /**
   * Adds the personal information block.
   *
   * <p>This includes the candidate's country and, if enabled, contact details such
   * as email, phone, and WhatsApp.
   *
   * @param document the DOCX document
   * @param candidate the candidate being exported
   * @param showContact whether contact information should be included
   */
  private void addPersonalBlock(XWPFDocument document, Candidate candidate, Boolean showContact) {
    if (candidate.getCountry() != null && candidate.getCountry().getName() != null) {
      addSimpleLine(document, "Country", candidate.getCountry().getName());
    }

    if (Boolean.TRUE.equals(showContact)) {
      addSectionHeading(document, "Contact Information");

      if (candidate.getUser() != null && candidate.getUser().getEmail() != null) {
        addSimpleLine(document, "Email", candidate.getUser().getEmail());
      }
      if (candidate.getPhone() != null) {
        addSimpleLine(document, "Phone", candidate.getPhone());
      }
      if (candidate.getWhatsapp() != null) {
        addSimpleLine(document, "WhatsApp", candidate.getWhatsapp());
      }
    }
  }

  /**
   * Adds the work experience section.
   *
   * <p>Each experience entry may include the date range, role, company and country,
   * and a plain-text version of the description.
   *
   * @param document the DOCX document
   * @param candidate the candidate being exported
   */
  private void addExperienceSection(XWPFDocument document, Candidate candidate) {
    if (candidate.getCandidateJobExperiences() == null
        || candidate.getCandidateJobExperiences().isEmpty()) {
      return;
    }

    addSectionHeading(document, "Experience");

    for (CandidateJobExperience experience : candidate.getCandidateJobExperiences()) {
      XWPFParagraph p = document.createParagraph();

      String dateRange =
          cvDocxFormatter.formatDateRange(experience.getStartDate(), experience.getEndDate());
      if (!dateRange.isBlank()) {
        XWPFRun dateRun = p.createRun();
        dateRun.setBold(true);
        dateRun.setText(dateRange);
        dateRun.addBreak();
      }

      if (experience.getRole() != null) {
        XWPFRun roleRun = p.createRun();
        roleRun.setBold(true);
        roleRun.setText(experience.getRole());
        roleRun.addBreak();
      }

      String companyAndCountry = cvDocxFormatter.formatCompanyAndCountry(experience);
      if (!companyAndCountry.isBlank()) {
        XWPFRun metaRun = p.createRun();
        metaRun.setItalic(true);
        metaRun.setText(companyAndCountry);
        metaRun.addBreak();
      }

      if (experience.getDescription() != null) {
        String plainText = cvDocxFormatter.toPlainText(experience.getDescription());
        if (!plainText.isBlank()) {
          XWPFRun descRun = p.createRun();
          descRun.setText(plainText);
        }
      }

      p.createRun().addBreak(BreakType.TEXT_WRAPPING);
    }
  }

  /**
   * Adds the education section.
   *
   * @param document the DOCX document
   * @param candidate the candidate being exported
   */
  private void addEducationSection(XWPFDocument document, Candidate candidate) {
    if (candidate.getCandidateEducations() == null
        || candidate.getCandidateEducations().isEmpty()) {
      return;
    }

    addSectionHeading(document, "Education");

    for (CandidateEducation education : candidate.getCandidateEducations()) {
      XWPFParagraph p = document.createParagraph();

      String line = cvDocxFormatter.formatEducationLine(education);
      XWPFRun run = p.createRun();
      run.setText(line);
    }
  }

  /**
   * Adds the certification section.
   *
   * @param document the DOCX document
   * @param candidate the candidate being exported
   */
  private void addCertificationSection(XWPFDocument document, Candidate candidate) {
    if (candidate.getCandidateCertifications() == null
        || candidate.getCandidateCertifications().isEmpty()) {
      return;
    }

    addSectionHeading(document, "Certification");

    for (CandidateCertification certification : candidate.getCandidateCertifications()) {
      XWPFParagraph p = document.createParagraph();

      String line = cvDocxFormatter.formatCertificationLine(certification);
      XWPFRun run = p.createRun();
      run.setText(line);
    }
  }

  /**
   * Adds the languages section.
   *
   * @param document the DOCX document
   * @param candidate the candidate being exported
   */
  private void addLanguageSection(XWPFDocument document, Candidate candidate) {
    if (candidate.getCandidateLanguages() == null
        || candidate.getCandidateLanguages().isEmpty()) {
      return;
    }

    addSectionHeading(document, "Languages");

    for (CandidateLanguage language : candidate.getCandidateLanguages()) {
      if (language.getLanguage() == null || language.getLanguage().getName() == null) {
        continue;
      }

      XWPFParagraph p = document.createParagraph();
      XWPFRun run = p.createRun();
      run.setText(language.getLanguage().getName());
    }
  }

  /**
   * Adds a section heading with bold, underlined styling.
   *
   * @param document the DOCX document
   * @param heading the heading text
   */
  private void addSectionHeading(XWPFDocument document, String heading) {
    XWPFParagraph p = document.createParagraph();
    p.setSpacingBefore(220);

    XWPFRun run = p.createRun();
    run.setBold(true);
    run.setFontSize(13);
    run.setUnderline(UnderlinePatterns.SINGLE);
    run.setText(heading);
  }

  /**
   * Adds a simple label-value line, such as {@code Email: user@example.com}.
   *
   * @param document the DOCX document
   * @param label the label to display
   * @param value the value to display
   */
  private void addSimpleLine(XWPFDocument document, String label, String value) {
    if (value == null || value.isBlank()) {
      return;
    }

    XWPFParagraph p = document.createParagraph();

    XWPFRun labelRun = p.createRun();
    labelRun.setBold(true);
    labelRun.setText(label + ": ");

    XWPFRun valueRun = p.createRun();
    valueRun.setText(value);
  }
}