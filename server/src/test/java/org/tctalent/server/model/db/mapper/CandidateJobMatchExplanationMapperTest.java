package org.tctalent.server.model.db.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

    @Test
    @DisplayName("should map persisted data to the REST response, including summary, experiences and limitations")
    void toResponse_shouldMapAllFields() {
        CandidateJobMatchExplanationData data = CandidateJobMatchExplanationData.builder()
            .summary("Strong match")
            .experienceExplanations(List.of(
                ExperienceMatchExplanationData.builder()
                    .experienceId(501L)
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

        assertEquals("Strong match", response.getSummary());
        assertEquals(2, response.getExperienceExplanations().size());
        assertEquals(501L, response.getExperienceExplanations().get(0).getExperienceId());
        assertEquals("Relevant Java experience", response.getExperienceExplanations().get(0).getExplanation());
        assertEquals(502L, response.getExperienceExplanations().get(1).getExperienceId());
        assertEquals("Relevant leadership experience", response.getExperienceExplanations().get(1).getExplanation());
        assertEquals(List.of("Limited detail in job description"), response.getLimitations());
    }

    @Test
    @DisplayName("should map the REST response back to persisted data, including summary, experiences and limitations")
    void toData_shouldMapAllFields() {
        CandidateMatchExplanation response = CandidateMatchExplanation.builder()
            .summary("Strong match")
            .experienceExplanations(List.of(
                ExperienceMatchExplanation.builder()
                    .experienceId(501L)
                    .explanation("Relevant Java experience")
                    .build()
            ))
            .limitations(List.of("Limited detail"))
            .build();

        CandidateJobMatchExplanationData data = mapper.toData(response);

        assertEquals("Strong match", data.getSummary());
        assertEquals(1, data.getExperienceExplanations().size());
        assertEquals(501L, data.getExperienceExplanations().get(0).getExperienceId());
        assertEquals("Relevant Java experience", data.getExperienceExplanations().get(0).getExplanation());
        assertEquals(List.of("Limited detail"), data.getLimitations());
    }
}
