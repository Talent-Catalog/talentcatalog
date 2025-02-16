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

import {Component, Input, OnInit} from '@angular/core';
import {TranslationService} from '../../../../services/translation.service';
import {User} from '../../../../model/user';
import {AuthorizationService} from "../../../../services/authorization.service";
import {LanguageService} from "../../../../services/language.service";
import {SystemLanguage} from "../../../../model/language";
import {CandidateOpportunityStage} from "../../../../model/candidate-opportunity";

/**
 * This admin settings component is used to configure translations for the candidate portal code.
 * <p/>
 * Candidate portal code supports multiple languages which the user can select.
 * In the actual code, wherever text is to be displayed its is done like this:
 * <pre>
 *       {{ 'TASKS.HEADER' | translate }}
 * </pre>
 * instead of actual text like "This is your tasks header".
 * <p/>
 * The "key" - TASKS.HEADER in the example above - is processed to be replaced by different text
 * based on the language that the user has selected.
 * <p/>
 * This component defines those keys - but not the values, which will always be null - in the
 * ALL_FIELDS structure below.
 * <p/>
 * The actual values are retrieved from Amazon's S3 store (there is one store for test and another
 * for production - so feel free to play around in your dev environment).
 * This Settings component allows a TC admin user to view the key values for different languages
 * and also to change the values.
 * <p/>
 * In the code, if there is no value assigned to a key, the text of the key itself is displayed.
 * The English translation is special. If there is no value assigned to a key for a particular
 * language, but there is a value assigned for the English language, then the English text is
 * displayed (better than just displaying the cryptic key).
 */
@Component({
  selector: 'app-general-translations',
  templateUrl: './general-translations.component.html',
  styleUrls: ['./general-translations.component.scss']
})
export class GeneralTranslationsComponent implements OnInit {

  @Input() loggedInUser: User;

  loading: boolean;
  languages: SystemLanguage[];
  systemLanguage: SystemLanguage;
  fields;
  fieldsFiltered;
  keys;

  saving: boolean;
  saveError: any;
  error: any;

  constructor(private translationService: TranslationService,
              private languageService: LanguageService,
              private authService: AuthorizationService) {
  }

  ngOnInit() {
    //Populate the translation keys for each candidate opp stage
    const CASE_STAGE_GROUP = "CASE-STAGE";
    ALL_FIELDS[CASE_STAGE_GROUP] = {};
    for (const key of Object.keys(CandidateOpportunityStage)) {
      ALL_FIELDS[CASE_STAGE_GROUP][key.toUpperCase()] = null;
    }

    this.loading = true;
    this.error = null;
    this.languageService.listSystemLanguages().subscribe(
      (result) => {
        this.languages = result;
        this.loading = false;
        this.setLanguage(this.languages[0]);
      },
      (error) => {
        this.error = error;
        this.loading = false;
      }
    )
    this.keys = Object.keys(ALL_FIELDS);
  }

  setLanguage(language: SystemLanguage) {
    this.error = null;
    this.loading = true;
    this.translationService.loadTranslationsFile(language.language).subscribe(
      (translations) => {
        this.loading = false;
        this.systemLanguage = language;
        this.fields = [];
        this.getFields(this.fields, null, ALL_FIELDS, translations);
        this.fieldsFiltered = this.fields;
      }, (error) => {
        this.loading = false;
        this.error = error;
      }
    );
  }

  getFields(results: {path, value}[], path, fieldsToFill, data) {
    Object.entries(fieldsToFill).map(([key, value]) => {
      let subPath = path ? `${path}.${key}` : key;
      if (value === null) {
        results.push({path: subPath.toLowerCase(), value: data ? data[key] : null});
      } else {
        this.getFields(results, subPath,  value, data ? data[key] : null);
      }
    });
  }

  save() {
    let result = {};
    this.fields.forEach(field => {
      let path = field.path.split('.');
      let target = result;
      for (let i = 0; i < path.length; i++) {
        const next = path[i].toUpperCase();
        if (i == path.length - 1) {
          target[next] = field.value;
        } else {
          target[next] = target[next] ? target[next] : {};
          target = target[next];
        }
      }
    });

    this.saving = true;
    this.saveError = null;
    this.translationService.updateTranslationFile(this.systemLanguage.language, result).subscribe(
      result => {
        this.saving = false;
      },
      error => {
        this.saving = false;
        this.saveError = error;
      }
    );
  }

  isBlank(value) {
    return (!value || /^\s*$/.test(value));
  }

  isAnAdmin(): boolean {
    return this.authService.isAnAdmin();
  }

  filterItems($event) {
    if ($event != null) {
      this.fieldsFiltered = this.fields.filter(f => f.path.split('.')[0] === $event.toLowerCase())
    } else {
      this.fieldsFiltered = this.fields;
    }
  }
}

const ALL_FIELDS = {
    "HEADER": {
      "NAV": {
        "ACCOUNT": null,
        "LOGOUT": null,
        "LOGIN": null,
        "PROFILE": null,
        "UPLOAD": {
          "FILE": null,
          "PHOTO": null
        }
      },
      "LANG": {
        "SELECT": null,
      }
    },
    "JOI": {
      "BENEFITS": {
        "LABEL": null,
        "TOOLTIP": null
      },
      "COST_COMMITMENT": {
        "LABEL": null,
        "TOOLTIP": null
      },
      "EDUCATION_REQUIREMENTS": {
        "LABEL": null,
        "TOOLTIP": null
      },
      "EMPLOYMENT_EXPERIENCE": {
        "LABEL": null,
        "TOOLTIP": null
      },
      "LANGUAGE_REQUIREMENTS": {
        "LABEL": null,
        "TOOLTIP": null
      },
      "LOCATION": {
        "LABEL": null,
        "TOOLTIP": null
      },
      "LOCATION_DETAILS": {
        "LABEL": null,
        "TOOLTIP": null
      },
      "MIN_SALARY": {
        "LABEL": null,
        "TOOLTIP": null
      },
      "OCCUPATION_CODE": {
        "LABEL": null,
        "TOOLTIP": null
      },
      "RECRUITMENT_PROCESS": {
        "LABEL": null,
        "TOOLTIP": null
      },
      "SALARY_RANGE": {
        "LABEL": null,
        "TOOLTIP": null
      },
      "SKILL_REQUIREMENTS": {
        "LABEL": null,
        "TOOLTIP": null
      },
      "VISA_PATHWAYS": {
        "LABEL": null,
        "TOOLTIP": null
      },
    },
    "STAFF_INSTRUCTION": {
       "CANDIDATE_NO_INTEREST": null,
       "CANDIDATE_UNSURE_INTEREST": null
     },
    "LOADING": null,
    "LOGIN": {
      "TITLE": null,
      "LABEL": {
        "USERNAME": null,
        "PASSWORD": null
      },
      "BUTTON": {
        "ALREADY": null,
        "FORGOT": null,
        "LOGIN": null
      }
    },
    "RESETPASSWORD": {
      "TITLE": null,
      "SUCCESS": null,
      "LABEL": {
        "EMAIL": null
      },
      "BUTTON": {
        "RESET": null
      }
    },
    "CHANGEPASSWORD": {
      "TITLE": null,
      "SUCCESS": null,
      "LABEL": {
        "OLDPASSWORD": null,
        "PASSWORD": null,
        "PASSWORDCONFIRMATION": null
      },
      "BUTTON": {
        "UPDATE": null
      }
    },
    "LANDING": {
      "TITLE": null,
      "REGISTER": null,
      "LOGIN": null,
      "PARA1": null,
      "HEADING2": null,
      "PARA2": null,
      "HEADING3": null,
      "PARA3": null,
      "USAFGHAN": {
        "HEADING1": null,
        "HEADING2": null
      }
    },
    "HOME": {
      "TITLE": null,
      "DRAFT": {
        "EXPLANATION": null,
        "BUTTON": null
      },
      "PENDING": {
        "EXPLANATION": null,
        "BUTTON": null
      },
      "ACTIVE": {
        "EXPLANATION": null,
        "BUTTON": null
      },
      "INCOMPLETE": {
        "EXPLANATION": null,
        "BUTTON": null
      },
      "EMPLOYED": {
        "EXPLANATION": null
      },
      "INACTIVE": {
        "EXPLANATION": null
      },
      "INELIGIBLE": {
        "EXPLANATION": null,
        "BUTTON": null
      },
      "EMAIL_VERIFICATION" : {
        "BUTTON": null,
        "IDLE": {
          "TITLE": null,
          "DESCRIPTION": null,
          "UPDATE_EMAIL": null,
          "SEND_BUTTON": null
        },
        "LOADING": {
          "TITLE": null,
          "DESCRIPTION": null
        },
        "EMAIL_SENT" : {
          "TITLE": null,
          "DESCRIPTION": null,
          "SPAM": null,
          "BUTTON": null,
        },
        "ERROR":{
          "BUTTON":null,
        }
      }
    },
    "REGISTRATION": {
      "HEADER": {
        "ACCOUNT": {
          "EXPLANATION": null
        },
        "EXPLANATION": null,
        "STEP": null,
        "TITLE": {
          "ACCOUNT": null,
          "CONTACT": null,
          "CONTACT/ALTERNATE": null,
          "CONTACT/ADDITIONAL": null,
          "PERSONAL": null,
          "OCCUPATION": null,
          "EXPERIENCE": null,
          "EDUCATION": null,
          "EDUCATION/MASTERS": null,
          "EDUCATION/UNIVERSITY": null,
          "EDUCATION/SCHOOL": null,
          "LANGUAGE": null,
          "EXAM": null,
          "CERTIFICATIONS": null,
          "DESTINATIONS": null,
          "ADDITIONAL": null,
          "UPLOAD": null,
          "SUBMIT": null
        }
      },
      "FOOTER": {
        "BACK": null,
        "CANCEL": null,
        "NEXT": null,
        "SUBMIT": null,
        "UPDATE": null
      },
      "CONTACT": {
        "LABEL": {
          "EMAIL": null,
          "PHONE": null,
          "WHATSAPP": null,
          "RELOCATEDFIELDS": null,
          "RELOCATEDADDRESS": null,
          "RELOCATEDCITY": null,
          "RELOCATEDSTATE": null,
          "RELOCATEDCOUNTRY": null,
          "USERNAME": null,
          "PASSWORD": null,
          "PASSWORDCONFIRMATION": null,
          "CONTACTCONSENTREGISTRATION": null,
          "CONTACTCONSENTPARTNERS": null
        }
      },
      "PERSONAL": {
        "LABEL": {
          "FIRSTNAME": null,
          "LASTNAME": null,
          "GENDER": null,
          "DOB": null,
          "COUNTRYID": null,
          "CITY": null,
          "STATE": null,
          "YEAROFARRIVAL": null,
          "NATIONALITY": null,
          "OTHER_NATIONALITY": null,
          "OTHER_NATIONALITIES": null,
          "EXTERNALID": null,
          "REGISTEREDWITHUN": null,
          "REGISTRATIONID": null,
          "UNHCRCONSENT": null
        },
        "NOTE": {
          "STATELESS": null,
          "UNHCRREGISTERED": null,
          "UNHCRCONSENT": null
        }
      },
      "OCCUPATION": {
        "LABEL": {
          "OCCUPATION": null,
          "YEARSEXPERIENCE": null,
          "DISCLAIMER": null,
          "MIGRATED_OCCUPATION": null
        },
        "BUTTON": {
          "ADD": null
        },
        "DELETE": {
          "TITLE": null,
          "CONFIRMATION": null,
          "YES": null,
          "NO": null
        }
      },
      "EXPERIENCE": {
        "BUTTON": {
          "ADD": null
        }
      },
      "EDUCATION": {
        "MESSAGE": null,
        "LABEL": {
          "MAXEDUCATIONLEVELID": null
        },
        "BUTTON": {
          "ADD": null
        }
      },
      "LANGUAGE": {
        "LABEL": {
          "LANGUAGE": null,
          "SPEAK": null,
          "WRITTEN": null
        },
        "BUTTON": {
          "ADD": null
        }
      },
      "EXAM": {
        "LABEL": {
          "EXAM": null,
          "SCORE": null,
          "YEAR": null,
          "NOTES": null,
          "OTHEREXAM":null
        },
        "BUTTON": {
          "ADD": null
        },
        "DELETE": {
          "TITLE": null,
          "CONFIRMATION": null,
          "YES": null,
          "NO": null
        }
      },
      "DESTINATIONS": {
        "LABEL": {
          "INTEREST": null,
          "NOTES": null,
        },
      },
      "CERTIFICATIONS": {
        "LABEL": {
          "NAME": null,
          "INSTITUTION": null,
          "DATECOMPLETED": null
        },
        "BUTTON": {
          "ADD": null
        }
      },
      "SUBMIT": {
        "LABEL": {
          "ADDITIONALINFO": null,
          "SURVEY": null,
          "COMMENT": null,
        },
        "LINKEDIN": {
          "LABEL": null,
          "WARN": null
        },
      },
      "COMPLETE": {
        "TITLE": null,
        "PARA1": null,
        "BUTTON": {
          "LOGOUT": null,
          "PROFILE": null
        }
      },
      "ATTACHMENTS": {
        "TITLE": null,
        "EMPTYSTATE": null,
        "LABEL": {
          "NAME": null,
          "EDIT": null,
          "CREATEDBY": null,
          "CREATEDDATE": null
        },
        "CV": {
          "NAME": null,
          "EXPLANATION": null
        },
        "OTHER": {
          "NAME": null,
          "EXPLANATION": null
        },
        "WARN": {
          "MOBILE": null
        }
      }
    },
    "PROFILE": {
      "TAB": {
        "PROFILE": null,
        "TASKS": null,
        "OPPS": null
      },
      "CONTACT": {
        "TITLE": null,
        "EMAIL": null,
        "PHONE": null,
        "WHATSAPP": null,
        "RELOCATEDADDRESS": null
      },
      "PERSONAL": {
        "TITLE": null,
        "FIRSTNAME": null,
        "LASTNAME": null,
        "GENDER": null,
        "DOB": null,
        "COUNTRY": null,
        "CITY": null,
        "STATE": null,
        "YEAROFARRIVAL": null,
        "NATIONALITY": null
      },
      "OCCUPATIONS": {
        "TITLE": null
      },
      "EXPERIENCE": {
        "TITLE": null
      },
      "EDUCATION": {
        "TITLE": null
      },
      "EXAMS": {
        "TITLE": null
      },
      "CERTIFICATIONS": {
        "TITLE": null
      },
      "LANGUAGES": {
        "TITLE": null
      },
      "DESTINATIONS": {
        "TITLE": null
      },
      "OTHER": {
        "TITLE": null,
        "ADDITIONALINFO": null,
        "SURVEY": null,
        "SURVEYCOMMENT": null,
        "LINKEDIN": null
      },
      "UPLOAD": {
        "TITLE": null
      },
      "BUTTON": {
        "EDIT": null,
        "CV": null
      }
    },
    "EDIT": {
      "TITLE": {
        "EDITING": null,
        "CONTACT": null,
        "OCCUPATIONS": null,
        "PERSONAL": null,
        "OCCUPATION": null,
        "EXPERIENCE": null,
        "EDUCATION": null,
        "LANGUAGES": null,
        "EXAMS": null,
        "CERTIFICATIONS": null,
        "DESTINATIONS": null,
        "ADDITIONAL": null,
        "UPLOAD": null
      }
    },
    "FORM": {
      "LABEL": {
        "OPTIONAL": null,
        "CLEAR": null,
        "CHOOSE": null,
        "SAVE": null,
        "APPROX": null
      },
      "PLACEHOLDER" : {
        "SELECT": null,
        "SELECTORTYPE": null,
        "SELECTOREXAM": null
      },
      "ERROR": {
        "REQUIRED": null,
        "EMAIL": null,
        "MINVALUE": null,
        "MINLENGTH": null,
        "DATE": null,
        "INVALIDDATERANGE": null
      },
      "JOBEXPERIENCE": {
        "LABEL": {
          "CANDIDATEOCCUPATIONID": null,
          "COMPANYNAME": null,
          "COUNTRY": null,
          "STARTDATE": null,
          "ENDDATE": null,
          "ROLE": null,
          "DESCRIPTION": null,
          "CONTRACTTYPE": {
            "TITLE": null,
            "FULLTIME": null,
            "PARTTIME": null
          },
          "EMPLOYMENTTYPE": {
            "TITLE": null,
            "PAID": null,
            "VOLUNTARY": null
          }
        },
        "BUTTON": {
          "ADD": null,
          "CANCEL": null,
          "SAVE": null
        }
      },
      "EDUCATION": {
        "EDUCATIONTYPE": null,
        "EDUCATIONMAJORID": null,
        "COURSENAME": null,
        "COUNTRYID": null,
        "INSTITUTION": null,
        "LENGTHOFCOURSEYEARS": null,
        "DATECOMPLETED": null,
        "INCOMPLETE": null
      },
      "ATTACHMENT": {
        "LABEL": {
          "DROP": null,
          "OR": null,
          "BROWSE": {
            "FILE": null,
            "IMAGE": null,
          },
          "UPLOADING": null,
          "PHOTO": null
        }
      }
    },
    "CARD": {
      "JOBEXPERIENCE": {
        "LABEL": {
          "FULLTIME": null,
          "PARTTIME": null,
          "PAID": null,
          "VOLUNTEER": null
        }
      },
      "EDUCATION": {
        "MAJOR": null
      }
    },
    "GENDER": {
      "MALE": null,
      "FEMALE": null,
      "OTHER": null
    },
    "EDUCATIONTYPE": {
      "ASSOCIATE": null,
      "VOCATIONAL": null,
      "BACHELOR": null,
      "MASTERS": null,
      "DOCTORAL": null
    },
    "ERROR": {
      "EMAIL_TAKEN": null,
      "PHONE_TAKEN": null,
      "WHATSAPP_TAKEN": null,
      "USER_DEACTIVATED": null,
      "INVALID_PASSWORD_MATCH": null,
      "PASSWORD_EXPIRED": null,
      "MISSING_OBJECT": null,
      "MISSING_WORK_EXPERIENCE": null,
      "INVALID_PASSWORD_TOKEN": null,
      "INVALID_PASSWORD_FORMAT": null,
      "FILE_DOWNLOAD_FAILED": null,
      "EXPIRED_PASSWORD_TOKEN": null,
      "ENTITY_REFERENCED": null,
      "UNKNOWN_OCCUPATION": null,
      "ALLOW_POPUPS": null,
      "CRITERIA_INVALID": {
        "HEADING": null,
        "LINK": null,
      },
    },
  "CONFIRMATION": {
    "YES": null,
    "NO": null,
    "UNSURE": null,
  },
  "TASKS": {
    "TAB": null,
    "VIEWHELP": null,
    "ONGOING": {
      "HEADER": null,
      "NOTE": null,
    },
    "COMPLETED": {
      "HEADER": null,
      "NOTE": null,
    },
    "TABLE": {
      "NAME": null,
      "REQUIRED": null,
      "DUEDATE": null,
      "COMPLETED": null,
      "ABANDONED": null,
      "NONE": {
        "ACTIVETASKS": null,
        "COMPLETEDTASKS": null,
      }
    },
    "UPLOAD" : {
      "HEADER": null,
      "LOADING": null,
      "SUCCESS": null,
      "VIEW": null
    },
    "QUESTION": {
      "HEADER": null,
      "LABEL": null,
      "DROPDOWN": {
        "LABEL": null
      }
    },
    "SIMPLE": {
      "HEADER": null,
      "LABEL": null,
      "NOTE": null,
      "DOC": {
        "LABEL": null,
        "NOTE": null,
      },
    },
    "COMMENT": {
      "HEADER": null,
      "LABEL": null,
      "ABANDONED": {
        "LABEL": null,
        "NOTE": null,
      }
    },
    "TYPES": {
      "UPLOAD": null,
      "QUESTION": null,
    },
    "TASK": {
      "REQUIRED": null,
      "OPTIONAL": null,
      "DUEDATE": null,
      "ABANDONEDDATE": null,
      "COMPLETEDDATE": null,
      "OVERDUE": null,
      "VIEWHELP": null,
      "RETURN": null,
      "SUBMIT": null,
    },
  },
  "CANDIDATE-OPPS": {
    "HEADER": null,
    "NOTE": null,
    "TABLE": {
      "ALL-JOB-CANDIDATES-CHAT": null,
      "CHATS": null,
      "COMMENTS": null,
      "DESTINATION-CHAT": null,
      "NAME": null,
      "SOURCE-CHAT": null,
      "STAGE": null,
      "NONE": null,
      "OFFER": null,
      "INTERVIEW-GUIDANCE": null
    },
    "OPP" : {
      "CHAT-RETURN": null,
      "CHAT-MARK-AS-READ": null,
      "JOB-RETURN": null,
    },
  },
  "CHAT": {
      "HEADER": null,
      "POST-HEADER": null,
      "POST-SEND": null,
      "MARK-AS-READ": null,
      "NO-POSTS": null,
      "ADD-LINK": null,
      "LINK-PLACEHOLDER": null,
      "LINK-URL": null,
      "URL-PREFIX": null,
      "URL-PREFIX-ERROR": null,
      "LINK-EDIT-BTN": null,
      "LINK-REMOVE-BTN": null,
      "LINK-SAVE-BTN": null,
      "LINK-CANCEL-BTN": null
  },
  "CHAT_INFO": {
      "LABEL": {
        "PARTICIPANTS": null,
        "PURPOSE": null,
      },
      "HEADING": {
        "CANDIDATE_PROSPECT": null,
        "ALL_JOB_CANDIDATES": null,
        "JOB_CREATOR_SOURCE_PARTNER": null,
        "CANDIDATE_RECRUITING": null,
        "JOB_CREATOR_ALL_SOURCE_PARTNERS": null
      },
      "PARTICIPANTS": {
        "CANDIDATE_PROSPECT": null,
        "ALL_JOB_CANDIDATES": null,
        "JOB_CREATOR_SOURCE_PARTNER": null,
        "CANDIDATE_RECRUITING": null,
        "JOB_CREATOR_ALL_SOURCE_PARTNERS": null
      },
      "PURPOSE": {
        "CANDIDATE_PROSPECT": null,
        "ALL_JOB_CANDIDATES": null,
        "JOB_CREATOR_SOURCE_PARTNER": null,
        "CANDIDATE_RECRUITING": null,
        "JOB_CREATOR_ALL_SOURCE_PARTNERS": null
      }
  }
}
