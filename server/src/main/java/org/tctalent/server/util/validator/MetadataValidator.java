package org.tctalent.server.util.validator;

import java.util.List;
import java.util.Map;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.response.MetadataFieldResponse;

/**
 * Interface for validating metadata fields submitted by a candidate.
 * <p>
 * Implementations of this interface should provide logic to check if the
 * candidate's provided metadata answers are valid according to the
 * required metadata definitions.
 * </p>
 *
 * <p>
 * Typically used in task completion workflows where candidates must
 * provide specific information that may need to be validated before
 * processing the task.
 * </p>
 *
 * <p>
 * Example usage:
 * <pre>
 * MetadataValidator validator = new SomeMetadataValidatorImpl();
 * validator.validate(candidate, fieldAnswers, requiredMetadata);
 * </pre>
 * </p>
 *
 * @see Candidate
 * @see MetadataFieldResponse
 */
public interface MetadataValidator {

  /**
   * Validates the metadata field answers provided by a candidate against
   * the required metadata definitions.
   *
   * @param candidate the candidate providing the metadata
   * @param fieldAnswers a map of field names to submitted values
   * @param requiredMetadata the list of metadata fields that are required for validation
   */
  void validate(Candidate candidate, Map<String, String> fieldAnswers, List<MetadataFieldResponse> requiredMetadata);
}
