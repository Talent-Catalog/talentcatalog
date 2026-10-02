package org.tctalent.server.model.db.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.tctalent.server.model.db.explanation.CandidateJobMatchExplanationData;
import org.tctalent.server.model.db.explanation.ExperienceMatchExplanationData;
import org.tctalent.server.response.CandidateMatchExplanation;
import org.tctalent.server.response.ExperienceMatchExplanation;

class CandidateJobMatchExplanationMapperTest {

    private final CandidateJobMatchExplanationMapper mapper =
        Mappers.getMapper(CandidateJobMatchExplanationMapper.class);

    private static final OffsetDateTime GENERATED_AT = OffsetDateTime.parse("2026-10-02T03:30:00Z");
    private static final String MODEL_NAME = "qwen.qwen3-235b-a22b-2507-v1:0";

    @Test
    @DisplayName("should map persisted data to the REST response, including summary, experiences and limitations")
    void toResponse_shouldMapAllFields() {
        CandidateJobMatchExplanationData data = CandidateJobMatchExplanationData.builder()
            .generatedAt(GENERATED_AT)
            .modelName(MODEL_NAME)
            .summary("Strong match")
            .experienceExplanations(List.of(
                ExperienceMatchExplanationData.builder()
                    .experienceId(501L)
                    .jobTitle("Senior Software Engineer")
                    .explanation("Relevant Java experience")
                    .build(),
                ExperienceMatchExplanationData.builder()
                    .experienceId(502L)
                    .explanation("Relevant leadership experience")
                    .build()
            ))
            .limitations(List.of("Limited detail in job description"))
            .build();

        CandidateMatchExplanation response = mapper.toResponse(data);

        assertEquals(GENERATED_AT, response.getGeneratedAt());
        assertEquals(MODEL_NAME, response.getModelName());
        assertEquals("Strong match", response.getSummary());
        assertEquals(2, response.getExperienceExplanations().size());
        assertEquals(501L, response.getExperienceExplanations().get(0).getExperienceId());
        assertEquals("Senior Software Engineer", response.getExperienceExplanations().get(0).getJobTitle());
        assertNull(response.getExperienceExplanations().get(1).getJobTitle());
        assertEquals("Relevant Java experience", response.getExperienceExplanations().get(0).getExplanation());
        assertEquals(502L, response.getExperienceExplanations().get(1).getExperienceId());
        assertEquals("Relevant leadership experience", response.getExperienceExplanations().get(1).getExplanation());
        assertEquals(List.of("Limited detail in job description"), response.getLimitations());
    }

    @Test
    @DisplayName("should map the REST response back to persisted data, including summary, experiences and limitations")
    void toData_shouldMapAllFields() {
        CandidateMatchExplanation response = CandidateMatchExplanation.builder()
            .generatedAt(GENERATED_AT)
            .modelName(MODEL_NAME)
            .summary("Strong match")
            .experienceExplanations(List.of(
                ExperienceMatchExplanation.builder()
                    .experienceId(501L)
                    .jobTitle("Senior Software Engineer")
                    .explanation("Relevant Java experience")
                    .build()
            ))
            .limitations(List.of("Limited detail"))
            .build();

        CandidateJobMatchExplanationData data = mapper.toData(response);

        assertEquals(GENERATED_AT, data.getGeneratedAt());
        assertEquals(MODEL_NAME, data.getModelName());
        assertEquals("Strong match", data.getSummary());
        assertEquals(1, data.getExperienceExplanations().size());
        assertEquals(501L, data.getExperienceExplanations().get(0).getExperienceId());
        assertEquals("Senior Software Engineer", data.getExperienceExplanations().get(0).getJobTitle());
        assertEquals("Relevant Java experience", data.getExperienceExplanations().get(0).getExplanation());
        assertEquals(List.of("Limited detail"), data.getLimitations());
    }

    @Test
    @DisplayName("should map legacy persisted data without metadata or job titles, leaving them null")
    void toResponse_shouldLeaveMissingMetadataNull() {
        CandidateJobMatchExplanationData data = CandidateJobMatchExplanationData.builder()
            .summary("Strong match")
            .experienceExplanations(List.of(
                ExperienceMatchExplanationData.builder()
                    .experienceId(501L)
                    .explanation("Relevant Java experience")
                    .build()
            ))
            .limitations(List.of())
            .build();

        CandidateMatchExplanation response = mapper.toResponse(data);

        assertNull(response.getGeneratedAt());
        assertNull(response.getModelName());
        assertNull(response.getExperienceExplanations().get(0).getJobTitle());
        assertEquals("Strong match", response.getSummary());
    }
}
