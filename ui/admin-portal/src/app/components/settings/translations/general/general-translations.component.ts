import {Component, Input, OnInit} from '@angular/core';
import {TranslationService} from '../../../../services/translation.service';
import {User} from '../../../../model/user';

@Component({
  selector: 'app-general-translations',
  templateUrl: './general-translations.component.html',
  styleUrls: ['./general-translations.component.scss']
})
export class GeneralTranslationsComponent implements OnInit {

  @Input() loggedInUser: User;

  loading: boolean;
  languages = ['en', 'ar'];
  language: string;
  fields;

  saving: boolean;
  saveError: any;

  constructor(private translationService: TranslationService) {
  }

  ngOnInit() {
    this.setLanguage(this.languages[0]);
  }

  setLanguage(language: string) {
    this.language = language;
    this.loading = true;
    this.translationService.loadTranslationsFile(this.language).subscribe(translations => {
      this.loading = false;
      this.fields = [];
      this.getFields(this.fields, null, ALL_FIELDS, translations);
    });
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
    this.translationService.updateTranslationFile(this.language, result).subscribe(
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
}

const ALL_FIELDS = {
    "HEADER": {
      "NAV": {
        "ACCOUNT": null,
        "LOGOUT": null,
        "LOGIN": null
      }
    },
    "LOADING": null,
    "LOGIN": {
      "TITLE": null,
      "LABEL": {
        "USERNAME": null,
        "PASSWORD": null
      },
      "BUTTON": {
        "FORGOT": null,
        "LOGIN": null
      }
    },
    "RESETPASSWORD": {
      "TITLE": null,
      "ERROR": null,
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
      "INTRO": null,
      "REGISTER": null,
      "LOGIN": null,
      "PARA1": null,
      "HEADING2": null,
      "PARA2": null,
      "HEADING3": null,
      "PARA3": null,
      "HEADING4": null,
      "PARA4": null
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
        "EXPLANATION": null,
        "BUTTON": null
      },
      "INACTIVE": {
        "EXPLANATION": null
      }
    },
    "REGISTRATION": {
      "HEADER": {
        "EXPLANATION": null,
        "STEP": null,
        "TITLE": {
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
          "CERTIFICATIONS": null,
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
      "LANDING": {
        "TITLE": null,
        "PARA1": null,
        "PARA2": null,
        "BUTTON1": null
      },
      "CONTACT": {
        "LABEL": {
          "EMAIL": null,
          "PHONE": null,
          "WHATSAPP": null,
          "USERNAME": null,
          "PASSWORD": null,
          "PASSWORDCONFIRMATION": null
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
          "YEAROFARRIVAL": null,
          "NATIONALITY": null,
          "REGISTEREDWITHUN": null,
          "REGISTRATIONID": null
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
          "COMMENT": null
        }
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
        }
      }
    },
    "PROFILE": {
      "CONTACT": {
        "TITLE": null,
        "EMAIL": null,
        "PHONE": null,
        "WHATSAPP": null
      },
      "PERSONAL": {
        "TITLE": null,
        "FIRSTNAME": null,
        "LASTNAME": null,
        "GENDER": null,
        "DOB": null,
        "COUNTRY": null,
        "CITY": null,
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
      "CERTIFICATIONS": {
        "TITLE": null
      },
      "LANGUAGES": {
        "TITLE": null
      },
      "OTHER": {
        "TITLE": null,
        "ADDITIONALINFO": null
      },
      "BUTTON": {
        "EDIT": null
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
        "CERTIFICATIONS": null,
        "OTHER": null
      }
    },
    "FORM": {
      "LABEL": {
        "OPTIONAL": null,
        "CLEAR": null,
        "CHOOSE": null,
        "SAVE": null
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
          "BROWSE": null,
          "UPLOADING": null
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
      "UNKNOWN_OCCUPATION": null
    },
  };
