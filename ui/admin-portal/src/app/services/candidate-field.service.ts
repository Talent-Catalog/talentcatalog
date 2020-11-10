import {Injectable} from '@angular/core';
import {DatePipe, TitleCasePipe} from "@angular/common";
import {CandidateFieldInfo} from "../model/candidate-field-info";
import {AuthService} from "./auth.service";

@Injectable({
  providedIn: 'root'
})
export class CandidateFieldService {

  //Note - if you want to use any other pipes for formatting, you also need to
  //add them to providers array in app.module.ts.
  //See https://stackoverflow.com/a/48785621/929968
  private dateFormatter = (value) => this.datePipe.transform(value, "yyyy-MM-dd");
  private titleCaseFormatter = (value) => this.titleCasePipe.transform(value);

  private allDisplayableFields = [
    new CandidateFieldInfo("First Name", "user.firstName",
      null, this.isCandidateNameViewable),
    new CandidateFieldInfo("Gender", "gender",
      this.titleCaseFormatter, null),
    new CandidateFieldInfo("Last Name", "user.lastName",
      null, this.isCandidateNameViewable),
    new CandidateFieldInfo("Location", "country.name",
      null, this.isCountryViewable),
    new CandidateFieldInfo("Married?", "maritalStatus",
      null, null),
    new CandidateFieldInfo("Nationality", "nationality.name",
      null, this.isCountryViewable),
    new CandidateFieldInfo("Phone", "phone",
      null, null),
    new CandidateFieldInfo("Status", "status",
      null, null),
    new CandidateFieldInfo("UNHCR Number", "unhcrNumber",
      null, null),
    new CandidateFieldInfo("UNHCR Registered", "unhcrRegistered",
      null, null),
    new CandidateFieldInfo("UNHCR Status", "unhcrStatus",
      null, null),
    new CandidateFieldInfo("Updated", "updatedDate",
      this.dateFormatter, null),
  ];

  private allDisplayableFieldsMap = new Map<string, CandidateFieldInfo>();

  private allDefaultDisplayedFieldPaths: string [] = [
    "user.firstName",
    "user.lastName",
    "status",
    "updatedDate",
    "nationality.name",
    "country.name",
    "gender"
  ];

  constructor(
    private authService: AuthService,
    private datePipe: DatePipe,
    private titleCasePipe: TitleCasePipe
  ) {

    for (const field of this.allDisplayableFields) {
      this.allDisplayableFieldsMap.set(field.fieldPath, field);
    }
  }

  get defaultDisplayableFields(): CandidateFieldInfo[] {
    const fields: CandidateFieldInfo[] = [];

    for (const fieldPath of this.allDefaultDisplayedFieldPaths) {
      const field = this.allDisplayableFieldsMap.get(fieldPath);
      if (field == null) {
        //todo error
      } else {
        if (field.fieldSelector == null || field.fieldSelector()) {
          fields.push(field);
        }
      }
    }
    return fields;
  }

  get displayableFieldsMap(): Map<string, CandidateFieldInfo> {
    const fields = new Map<string, CandidateFieldInfo>();
    //Filter based on field selectors
    for (const field of this.allDisplayableFields) {
      if (field.fieldSelector == null || field.fieldSelector()) {
        fields.set(field.fieldPath, field);
      }
    }
    return fields;
  }

  isCandidateNameViewable(): boolean {
    const loggedInUser =
      this.authService ? this.authService.getLoggedInUser() : null;
    const role = loggedInUser ? loggedInUser.role : null;
    return role !== 'semilimited' && role !== 'limited';
  }

  isCountryViewable(): boolean {
    const loggedInUser =
      this.authService ? this.authService.getLoggedInUser() : null;
    const role = loggedInUser ? loggedInUser.role : null;
    return role !== 'limited';
  }

}
