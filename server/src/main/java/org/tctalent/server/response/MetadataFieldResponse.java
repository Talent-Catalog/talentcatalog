package org.tctalent.server.response;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO representing a metadata field returned in API responses.
 * <p>
 * A metadata field can represent different kinds of information such as
 * text, number, or selectable options. The {@code label} can be used for
 * display purposes (typically internationalized), while {@code options}
 * provides a list of valid selectable values for fields such as dropdowns.
 * </p>
 *
 * <ul>
 *   <li>{@code name} – the unique identifier of the field</li>
 *   <li>{@code type} – the field type (e.g., "string", "number", "select")</li>
 *   <li>{@code label} – a human-readable or localized label for display</li>
 *   <li>{@code options} – a list of possible values, when applicable</li>
 * </ul>
 */
@Getter
@Setter
public class MetadataFieldResponse {
  private String name;
  private String type;
  private String label;
  private List<String> options;
}
