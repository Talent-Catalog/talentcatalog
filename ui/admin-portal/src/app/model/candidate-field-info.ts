/*
 * Copyright (c) 2021 Talent Beyond Boundaries.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */
import {Candidate} from "./candidate";

export class CandidateFieldInfo {
  /**
   * This is how we display the field to the user
   */
  displayName: string;

  /**
   * This identifies field according to its candidate entity name - eg maritalStatus or
   * user.firstName
   */
  fieldPath: string;

  /**
   * Create an info
   * @param displayName see doc above
   * @param fieldPath see doc above
   * @param fieldFormatter Function which returns the field's displayed value. If null, the field
   * displays in the default way. Useful for formatting dates, for example.
   * @param fieldSelector If null, field is always available for display. If not null, field is
   * only available for display if this function returns true. For example, candidate names are
   * only displayable to user admins.
   */
  constructor(displayName: string, fieldPath: string,
              private fieldFormatter: (value: any) => string,
              public fieldSelector: () => boolean) {
    this.displayName = displayName;
    this.fieldPath = fieldPath;
  }

  getValue(candidate: Candidate): string {
    let value;
    // Need to format field with the candidate object not value
    if (this.fieldPath === "ieltsScore") {
      value = candidate;
    } else {
      value = this.getUnformattedValue(candidate);
    }
    const ret = value == null ? null :
      this.fieldFormatter == null ? value : this.fieldFormatter(value);
    return ret;
  }

  getUnformattedValue(candidate: Candidate): any {
    const fields: string[] = this.fieldPath.split('.');
    let val = candidate;
    for (const field of fields) {
      if (val == null) {
        break;
        // If we are sorting by level not alphabetical, break at object and then use levelGetNameFormatter.
      } else if (field === 'level') {
        break
      }
      val = val[field];
    }
    return val;
  }

}

