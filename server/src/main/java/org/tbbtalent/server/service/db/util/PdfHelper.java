package org.tbbtalent.server.service.db.util;

import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.tbbtalent.server.exception.PdfGenerationException;
import org.tbbtalent.server.model.db.*;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.extras.java8time.dialect.Java8TimeDialect;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.w3c.tidy.Tidy;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.FileSystems;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.thymeleaf.templatemode.TemplateMode.HTML;

/**
 * Service for generating PDFs using Flying Saucer and Thymeleaf templates. 
 * The PDF will display a letter styled with CSS.
 * <p/> 
 * There is also a main method which will generate a letter. 
 * The letter has two pages and will contain text and images.
 * <p>
 * Run main to generate the PDF. The file is called:
 * <p>
 * /test.pdf
 */
@Service
public class PdfHelper {

    private static final Logger log = LoggerFactory.getLogger(PdfHelper.class);

    @Value("${server.url}")
    private String serverUrl;

    private static final String UTF_8 = "UTF-8";
    private final TemplateEngine pdfTemplateEngine;

    @Autowired
    public PdfHelper(TemplateEngine pdfTemplateEngine) {
        this.pdfTemplateEngine = pdfTemplateEngine;
    }

    public Resource generatePdf(Candidate candidate){
        try {

            Context context = new Context();
            context.setVariable("candidate", candidate);

            String renderedHtmlContent = pdfTemplateEngine.process("template", context);
            String xHtml = convertToXhtml(renderedHtmlContent);

            ITextRenderer renderer = new ITextRenderer();

            String baseUrl = FileSystems
                    .getDefault()
                    .getPath("server","src","main","resources", "pdf")
                    .toUri()
                    .toURL()
                    .toString();
            log.info(baseUrl);
            renderer.setDocumentFromString(xHtml, baseUrl);
            renderer.layout();

            // And finally, we create the PDF:
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            renderer.createPDF(outputStream);
            outputStream.close();
            return new ByteArrayResource(outputStream.toByteArray());

        } catch (Exception e){
           throw new PdfGenerationException(e.getMessage());
        }

    }

    private static String convertToXhtml(String html) throws UnsupportedEncodingException {
        Tidy tidy = new Tidy();
        tidy.setInputEncoding(UTF_8);
        tidy.setOutputEncoding(UTF_8);
        tidy.setXHTML(true);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(html.getBytes(UTF_8));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        tidy.parseDOM(inputStream, outputStream);
        return outputStream.toString(UTF_8);
    }

    public static void main (String[] args){
        User user = new User();
        user.setFirstName("Jo");
        user.setLastName("Thatcher");
        user.setEmail("test@test.com");
        Candidate candidate = new Candidate(user, "045345345", null, user );
        candidate.setCandidateNumber("4556456");
        candidate.setWhatsapp("45654656");
        candidate.setPhone("44444444");
        candidate.setDob(LocalDate.now());
        Country country = new Country("Test Country", Status.active);
        candidate.setCountry(country);
        candidate.setNationality(new Nationality("Test Nationality", Status.active));

        Occupation occupation = new Occupation("Accountant", Status.active);
        CandidateOccupation candidateOccupation = new CandidateOccupation(candidate, occupation, 7l);
        List<CandidateJobExperience> jobs = new ArrayList<>();
        jobs.add(new CandidateJobExperience(candidate, country, candidateOccupation, "Test company 1", "Accountant", LocalDate.now().minusMonths(6), LocalDate.now(), "long description"));
        jobs.add(new CandidateJobExperience(candidate, country, candidateOccupation, "Test company 2", "Bookeeper", LocalDate.now().minusYears(3), null, "long description"));
        jobs.add(new CandidateJobExperience(candidate, country, candidateOccupation, "Test company 2", "Bookeeper", LocalDate.now().minusYears(3), null, "long description"));
        jobs.add(new CandidateJobExperience( candidate, country, candidateOccupation, "Test company 2", "Bookeeper", LocalDate.now().minusYears(3), null, "long description"));
        jobs.add(new CandidateJobExperience( candidate, country, candidateOccupation, "Test company 2", "Bookeeper", LocalDate.now().minusYears(3), null, "long description"));
        jobs.add(new CandidateJobExperience(candidate, country, candidateOccupation, "Test company 2", "Bookeeper", LocalDate.now().minusYears(3), null, "long description"));
        candidate.setCandidateJobExperiences(jobs);

        EducationMajor major = new EducationMajor("Accounting", Status.active);
        List<CandidateEducation> educations = new ArrayList<>();
        educations.add(new CandidateEducation( candidate, EducationType.Associate, country, major, 3, "Bath Uni", "MSC Computer Science", 2012, true));
        educations.add(new CandidateEducation( candidate, EducationType.Masters, country, major, 3, "Bath Uni", "MSC Computer Science", 2012, true));
        educations.add(new CandidateEducation( candidate, EducationType.Doctoral, country, major, 3, "Bath Uni", "MSC Computer Science", 2012, true));
        educations.add(new CandidateEducation( candidate, EducationType.Vocational, country, major, 3, "Bath Uni", "MSC Computer Science", 2012, true));
        candidate.setCandidateEducations(educations);

        List<CandidateCertification> certifications = new ArrayList<>();
        certifications.add(new CandidateCertification( candidate, "Cert1", "Institution 1", LocalDate.now().minusMonths(8)));
        certifications.add(new CandidateCertification( candidate, "Cert2", "Institution 2", LocalDate.now().minusMonths(35)));
        candidate.setCandidateCertifications(certifications);

        LanguageLevel languageLevel = new LanguageLevel("Fluent", Status.active, 3);
        Language french = new Language("French", Status.active);
        Language english = new Language("English", Status.active);
        List<CandidateLanguage> languages = new ArrayList<>();
        languages.add(new CandidateLanguage(candidate, french, languageLevel, languageLevel));
        languages.add(new CandidateLanguage( candidate, english, languageLevel, languageLevel));
        candidate.setCandidateLanguages(languages);

        try {

            ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
            templateResolver.setPrefix("/");
            templateResolver.setSuffix(".html");
            templateResolver.setTemplateMode(HTML);
            templateResolver.setCharacterEncoding(UTF_8);

            TemplateEngine templateEngine = new TemplateEngine();
            templateEngine.addDialect(new Java8TimeDialect());
            templateEngine.setTemplateResolver(templateResolver);

            Context context = new Context();
            context.setVariable("candidate", candidate);

            String renderedHtmlContent = templateEngine.process("template", context);
            String xHtml = convertToXhtml(renderedHtmlContent);

            ITextRenderer renderer = new ITextRenderer();

            // FlyingSaucer has a working directory. If you run this test, the working directory
            // will be the root folder of your project. However, all files (HTML, CSS, etc.) are
            // located under "/src/test/resources". So we want to use this folder as the working
            // directory.
            String baseUrl = FileSystems
                    .getDefault()
                    .getPath("server","src","main","resources", "pdf")
                    .toUri()
                    .toURL()
                    .toString();
            renderer.setDocumentFromString(xHtml, baseUrl);
            renderer.layout();

            // And finally, we create the PDF:
            FileOutputStream outputStream = new FileOutputStream(RandomStringUtils.random(3, true, false)+".pdf");
            renderer.createPDF(outputStream);
            outputStream.close();

        } catch (Exception e){
            throw new PdfGenerationException(e.getMessage());
        }
    }

}
