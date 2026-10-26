/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.integration.repo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jakarta.persistence.EntityManagerFactory;
import java.util.StringJoiner;
import java.util.TreeMap;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.id.enhanced.SequenceStyleGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.tctalent.server.integration.helper.BaseDBIntegrationTest;
import org.tctalent.server.repository.db.read.cache.CandidateRedisCache;

/**
 * Checks the id generator (database sequence) used by every entity.
 * <p>
 * The expected values are what Hibernate 6.6 used before the Spring Boot 4 upgrade. Hibernate 7
 * resolves generator names differently, so this protects against entities silently switching to a
 * different (or missing) sequence - TC-1189.
 * </p>
 */
@SpringBootTest
class EntityIdGeneratorIntegrationTest extends BaseDBIntegrationTest {

    @MockitoBean
    private CandidateRedisCache candidateRedisCache;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    private static final String EXPECTED = """
        org.tctalent.server.casi.domain.persistence.ServiceAssignmentEntity -> service_assignment_id_seq
        org.tctalent.server.casi.domain.persistence.ServiceListEntity -> service_list_id_seq
        org.tctalent.server.casi.domain.persistence.ServiceResourceEntity -> service_resource_id_seq
        org.tctalent.server.model.db.Agreement -> agreement_id_seq
        org.tctalent.server.model.db.AuditLog -> audit_log_id_seq
        org.tctalent.server.model.db.Candidate -> candidate_id_seq
        org.tctalent.server.model.db.CandidateAttachment -> candidate_attachment_id_seq
        org.tctalent.server.model.db.CandidateCertification -> candidate_certification_id_seq
        org.tctalent.server.model.db.CandidateCitizenship -> candidate_citizenship_id_seq
        org.tctalent.server.model.db.CandidateCouponCode -> candidate_coupon_code_id_seq
        org.tctalent.server.model.db.CandidateDependant -> candidate_dependant_id_seq
        org.tctalent.server.model.db.CandidateDestination -> candidate_destination_id_seq
        org.tctalent.server.model.db.CandidateEducation -> candidate_education_id_seq
        org.tctalent.server.model.db.CandidateExam -> candidate_exam_id_seq
        org.tctalent.server.model.db.CandidateForm -> candidate_form_id_seq
        org.tctalent.server.model.db.CandidateJobExperience -> candidate_job_experience_id_seq
        org.tctalent.server.model.db.CandidateLanguage -> candidate_language_id_seq
        org.tctalent.server.model.db.CandidateNote -> candidate_note_id_seq
        org.tctalent.server.model.db.CandidateOccupation -> candidate_occupation_id_seq
        org.tctalent.server.model.db.CandidateOpportunity -> candidate_opportunity_id_seq
        org.tctalent.server.model.db.CandidateProperty -> CompositeNestedGeneratedValueGenerator
        org.tctalent.server.model.db.CandidatePropertyDefinition -> candidate_property_definition_id_seq
        org.tctalent.server.model.db.CandidateReviewStatusItem -> candidate_review_item_id_seq
        org.tctalent.server.model.db.CandidateSavedList -> CompositeNestedGeneratedValueGenerator
        org.tctalent.server.model.db.CandidateSkill -> candidate_skill_id_seq
        org.tctalent.server.model.db.CandidateVisaCheck -> candidate_visa_check_id_seq
        org.tctalent.server.model.db.CandidateVisaJobCheck -> candidate_visa_job_check_id_seq
        org.tctalent.server.model.db.ChatPost -> chat_post_id_seq
        org.tctalent.server.model.db.Counterparty -> counterparty_id_seq
        org.tctalent.server.model.db.Country -> country_id_seq
        org.tctalent.server.model.db.DuolingoCoupon -> duolingo_coupon_id_seq
        org.tctalent.server.model.db.DuolingoExtraFields -> duolingo_extra_fields_id_seq
        org.tctalent.server.model.db.EducationLevel -> education_level_id_seq
        org.tctalent.server.model.db.EducationMajor -> education_major_id_seq
        org.tctalent.server.model.db.Employer -> employer_id_seq
        org.tctalent.server.model.db.ExportColumn -> export_column_id_seq
        org.tctalent.server.model.db.FormTaskImpl -> task_id_seq
        org.tctalent.server.model.db.HelpLink -> help_link_id_seq
        org.tctalent.server.model.db.Industry -> industry_id_seq
        org.tctalent.server.model.db.JobChat -> job_chat_id_seq
        org.tctalent.server.model.db.JobChatUser -> CompositeNestedGeneratedValueGenerator
        org.tctalent.server.model.db.JobOppIntake -> job_opp_intake_id_seq
        org.tctalent.server.model.db.Language -> language_id_seq
        org.tctalent.server.model.db.LanguageLevel -> language_id_seq
        org.tctalent.server.model.db.LinkPreview -> link_preview_id_seq
        org.tctalent.server.model.db.Matching -> matching_id_seq
        org.tctalent.server.model.db.Occupation -> occupation_id_seq
        org.tctalent.server.model.db.OfferToAssist -> offer_to_assist_id_seq
        org.tctalent.server.model.db.PartnerImpl -> partner_id_seq
        org.tctalent.server.model.db.PartnerJobRelation -> CompositeNestedGeneratedValueGenerator
        org.tctalent.server.model.db.QuestionTaskAssignmentImpl -> task_assignment_id_seq
        org.tctalent.server.model.db.QuestionTaskImpl -> task_id_seq
        org.tctalent.server.model.db.Reaction -> reaction_id_seq
        org.tctalent.server.model.db.RootRequest -> root_request_id_seq
        org.tctalent.server.model.db.SalesforceJobOpp -> salesforce_job_opp_tc_job_id_seq
        org.tctalent.server.model.db.SavedList -> saved_list_id_seq
        org.tctalent.server.model.db.SavedListLink -> saved_list_id_seq
        org.tctalent.server.model.db.SavedSearch -> saved_search_id_seq
        org.tctalent.server.model.db.SearchJoin -> search_join_id_seq
        org.tctalent.server.model.db.SkillsEscoEn -> Assigned
        org.tctalent.server.model.db.SkillsTcEn -> skills_tc_en_id_seq
        org.tctalent.server.model.db.SkillsTechOnetEn -> Assigned
        org.tctalent.server.model.db.SurveyType -> survey_type_id_seq
        org.tctalent.server.model.db.SystemLanguage -> system_language_id_seq
        org.tctalent.server.model.db.TaskAssignmentImpl -> task_assignment_id_seq
        org.tctalent.server.model.db.TaskImpl -> task_id_seq
        org.tctalent.server.model.db.Translation -> translation_id_seq
        org.tctalent.server.model.db.UploadTaskAssignmentImpl -> task_assignment_id_seq
        org.tctalent.server.model.db.UploadTaskImpl -> task_id_seq
        org.tctalent.server.model.db.User -> users_id_seq
        org.tctalent.server.model.db.chatbot.ChatbotMessage -> Assigned
        org.tctalent.server.model.db.embedding.EmbeddingModel -> embedding_model_id_seq
        org.tctalent.server.model.db.explanation.CandidateMatchingExplanation -> CompositeNestedGeneratedValueGenerator
        """;

    @Test
    @DisplayName("every entity uses the same id generator as before the Spring Boot 4 upgrade")
    void entityIdGenerators() {
        TreeMap<String, String> generators = new TreeMap<>();
        entityManagerFactory.unwrap(SessionFactoryImplementor.class).getMappingMetamodel()
            .forEachEntityDescriptor(persister -> {
                Object generator = persister.getGenerator();
                generators.put(persister.getEntityName(),
                    generator instanceof SequenceStyleGenerator sequenceGenerator
                        ? sequenceGenerator.getDatabaseStructure().getPhysicalName().render()
                        : generator.getClass().getSimpleName());
            });

        StringJoiner actual = new StringJoiner("\n", "", "\n");
        generators.forEach((entity, generator) -> actual.add(entity + " -> " + generator));
        assertEquals(EXPECTED, actual.toString());
    }
}
