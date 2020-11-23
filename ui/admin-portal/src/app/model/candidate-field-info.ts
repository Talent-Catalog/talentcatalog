/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
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
      }
      val = val[field];
    }
    return val;
  }
}

