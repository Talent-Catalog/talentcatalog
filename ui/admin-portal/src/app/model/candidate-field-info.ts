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
  displayName: string;
  fieldPath: string;

  constructor(displayName: string, fieldPath: string,
              private fieldFormatter: (value: any) => string,
              public fieldSelector: () => boolean) {
    this.displayName = displayName;
    this.fieldPath = fieldPath;
  }

  getValue(candidate: Candidate): string {
    const value = this.getUnformattedValue(candidate);
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

