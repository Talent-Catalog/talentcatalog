/*
 * Copyright (c) 2024 Talent Catalog.
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
import {CandidateSource} from "./base";

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
   * @param tooltipSupplier Optional function which returns a tooltip for the given value. If null
   * no tooltip is available.
   * @param fieldFormatter Optional function which takes the field's value and returns the field's
   * displayed value. If null, the field displays in the default way.
   * Useful for formatting dates, for example.
   * @param fieldSelector If null, field is always available for display. If not null, field is
   * only available for display if this function returns true. For example, candidate names are
   * only displayable to user admins.
   * @param sortable Designates whether a field is intended to be sortable, meaning we can include
   * non-sortable (e.g. @Transient) fields and disable the frontend sort-on-click behaviour that
   * would otherwise cause an error (using *ngIf).
   */
  constructor(displayName: string, fieldPath: string,
              private tooltipSupplier: (value: any, value2: any) => string,
              private fieldFormatter: (value: any, value2: any) => string,
              public fieldSelector: (value: any) => boolean,
              public sortable: boolean) {
    this.displayName = displayName;
    this.fieldPath = fieldPath;
  }

  getTooltip(candidate: Candidate, source: CandidateSource): string {
    let tooltip: string = '';
    if (this.tooltipSupplier != null) {
      const value = this.getValue(candidate, source);
      tooltip = this.tooltipSupplier(candidate, source);
    }
    return tooltip;
  }

  getValue(candidate: Candidate, source: CandidateSource): string {
    let value;
    // Need to format field with the candidate object not value
    if (this.fieldPath === "ieltsScore" || this.fieldPath === "frenchAssessmentScoreNclc" ||
      this.fieldPath === "latestIntake" || this.fieldPath === "latestIntakeDate" ||
      this.fieldPath === "nextStep" || this.fieldPath === "addedBy") {
      value = candidate;
    } else {
      value = this.getUnformattedValue(candidate);
    }
    const ret = value == null ? null :
      this.fieldFormatter == null ? value : this.fieldFormatter(value, source);
    return ret;
  }

  getUnformattedValue(candidate: Candidate): any {
    const fields: string[] = this.fieldPath.split('.');
    let val = candidate;
    for (const field of fields) {
      if (val == null) {
        break;
      } else if (val instanceof Array) {
          if (val.length === 0) {
            val = null;
            break;
          } else {
            val = val[0];
          }
      // If we are sorting by level not alphabetical, break at object and then use levelGetNameFormatter.
      } else if (field === 'level') {
        break
      }
      val = val[field];
    }
    return val;
  }

}

