package org.tctalent.server.util.validator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.response.MetadataFieldResponse;
import org.tctalent.server.util.validator.MetadataValidator;

/**
 * Default implementation of {@link MetadataValidator} that validates
 * candidate-submitted metadata fields.
 * <p>
 * This validator checks if all required metadata fields are provided
 * and not empty. If any required field is missing or empty, it throws
 * an {@link IllegalArgumentException}.
 * </p>
 *
 * <p>
 * Typical usage:
 * <pre>
 * MetadataValidator validator = new DefaultMetadataValidator();
 * validator.validate(candidate, fieldAnswers, requiredMetadata);
 * </pre>
 * </p>
 *
 * @see MetadataValidator
 * @see Candidate
 * @see MetadataFieldResponse
 */
public class DefaultMetadataValidator implements MetadataValidator {

  /**
   * Validates that all required metadata fields are present and non-empty
   * in the candidate's submitted answers.
   *
   * @param candidate the candidate providing the metadata
   * @param fieldAnswers a map of field names to submitted values
   * @param requiredMetadata the list of metadata fields required for validation
   * @throws IllegalArgumentException if any required field is missing or empty
   */
  @Override
  public void validate(Candidate candidate, Map<String, String> fieldAnswers, List<MetadataFieldResponse> requiredMetadata) {
    List<String> missingFields = new ArrayList<>();
    for (MetadataFieldResponse field : requiredMetadata) {
      String value = fieldAnswers.get(field.getName());
      if (value == null || value.trim().isEmpty()) {
        missingFields.add(field.getName());
      }
    }
    if (!missingFields.isEmpty()) {
      throw new IllegalArgumentException("Missing required metadata fields: " + String.join(", ", missingFields));
    }
  }
}
