/*
 * [License]
 */

package org.tctalent.server.request.form;

import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;
import org.tctalent.server.model.db.Country;
import org.tctalent.server.model.db.Gender;
import org.tctalent.server.model.db.TravelDocType;

@Getter
@Setter
public class TravelInfoFormData {
  private String firstName;
  private String lastName;
  private LocalDate dateOfBirth;
  private Gender gender;
  private Country birthCountry;
  private String placeOfBirth;
  private TravelDocType travelDocType;
  private String travelDocNumber;
  private String travelDocIssuedBy;
  private LocalDate travelDocIssueDate;
  private LocalDate travelDocExpiryDate;
  private String travelInfoComment;
}
