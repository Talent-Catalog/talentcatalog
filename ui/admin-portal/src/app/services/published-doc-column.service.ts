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

import {Injectable} from '@angular/core';
import {
  ExportColumn,
  PublishedDocColumnConfig,
  PublishedDocColumnDef,
  PublishedDocColumnProps,
  PublishedDocConstantSource,
  PublishedDocFieldSource,
  PublishedDocPropertySource,
  PublishedDocValueSource
} from "../model/saved-list";
import {PublishedDocColumnType, PublishedDocColumnWidth} from "../model/base";
import {CandidatePropertyDefinitionService} from "./candidate-property-definition.service";
import {CandidatePropertyDefinition} from "../model/candidate-property-definition";

@Injectable({
  providedIn: 'root'
})
export class PublishedDocColumnService {

  private allColumnInfosMap = new Map<string, PublishedDocColumnDef>();

  constructor(private candidatePropertyDefinitionService: CandidatePropertyDefinitionService) {
    //Keep empty column first, so we know the index and can sort at the end.
    this.addColumn("emptyColumn", "Empty Column", null);

    this.addColumn("candidateNumber", "Candidate #",
      new PublishedDocFieldSource("candidateNumber"))
    .width = PublishedDocColumnWidth.Narrow;

    this.addColumnWithLink("candidateNumberLinkCv", "Candidate # \n (link to CV)",
      new PublishedDocFieldSource("candidateNumber"),
      new PublishedDocFieldSource("shareableCv.url"))
    .width = PublishedDocColumnWidth.Narrow;

    this.addColumnWithLink("candidateNumberLinkAutoCv", "Candidate # \n (auto CV)",
      new PublishedDocFieldSource("candidateNumber"),
      new PublishedDocFieldSource("autoCvLink"))
    .width = PublishedDocColumnWidth.Narrow;

    this.addColumnWithLink("candidateNumberSmartCv", "Candidate # \n (smart CV)",
      new PublishedDocFieldSource("candidateNumber"),
      new PublishedDocFieldSource("smartCvLink"))
    .width = PublishedDocColumnWidth.Narrow;

    this.addColumnWithLink("candidateNumberLinkTc", "Candidate # \n(link to TC)",
      new PublishedDocFieldSource("candidateNumber"),
      new PublishedDocFieldSource("tcLink"))
    .width = PublishedDocColumnWidth.Narrow;

    this.addColumn("contextNote", "Context Note",
      new PublishedDocFieldSource("contextNote"))
    .width = PublishedDocColumnWidth.Wide;

    this.addColumn("dob", "DOB", new PublishedDocFieldSource("dob"));

    this.addColumn("email", "Email", new PublishedDocFieldSource("user.email"));

    this.addColumn("phone", "Phone Number", new PublishedDocFieldSource("phone"));

    this.addColumn("whatsapp", "Whatsapp", new PublishedDocFieldSource("whatsapp"));

    //These are special employer feedback fields (ie not display only fields, which are the default type)
    this.addColumnWithType("employerDecision", "Employer\nDecision",
      PublishedDocColumnType.EmployerCandidateDecision, null)
    .width = PublishedDocColumnWidth.Narrow;

    this.addColumnWithType("employerFeedback", "Employer\nFeedback",
      PublishedDocColumnType.EmployerCandidateNotes, null)
    .width = PublishedDocColumnWidth.Wide;

    this.addColumnWithType("employerInterview", "Employer\nInterview?",
      PublishedDocColumnType.YesNoDropdown, null)
      .width = PublishedDocColumnWidth.Narrow

    this.addColumn("firstName", "First Name", new PublishedDocFieldSource("user.firstName"));
    this.addColumn("partner", "Partner", new PublishedDocFieldSource("user.partner.abbreviation"));

    this.addColumn("gender", "Gender", new PublishedDocFieldSource("gender"))
    .width = PublishedDocColumnWidth.Narrow;

    // English exams
    this.addColumn("ieltsScore", "IELTS Score", new PublishedDocFieldSource("ieltsScore"));
    this.addColumn("OETOverall", "OET Overall", new PublishedDocFieldSource("oetOverall"));
    this.addColumn("OETRead", "OET Reading", new PublishedDocFieldSource("oetReading"));
    this.addColumn("OETList", "OET Listening", new PublishedDocFieldSource("oetListening"));
    this.addColumn("OETLang", "OET Language", new PublishedDocFieldSource("oetLanguage"));

    this.addColumn("lastName", "Last Name", new PublishedDocFieldSource("user.lastName"));
    this.addColumn("location", "Location", new PublishedDocFieldSource("country.name"));
    this.addColumn("city", "City", new PublishedDocFieldSource("city"));
    this.addColumn("state", "State", new PublishedDocFieldSource("state"));
    this.addColumn("name", "Name", new PublishedDocFieldSource("user"));
    this.addColumn("nationality", "Nationality", new PublishedDocFieldSource("nationality.name"));
    this.addColumn("dependants", "Dependants", new PublishedDocFieldSource("numberDependants"));

    // Summary fields
    this.addColumn("occupations", "Occupations", new PublishedDocFieldSource("occupationSummary"));
    this.addColumn("englishExams", "English Exams", new PublishedDocFieldSource("englishExamsSummary"));
    this.addColumn("educations", "Education", new PublishedDocFieldSource("educationsSummary"));
    this.addColumn("certifications", "Certifications", new PublishedDocFieldSource("certificationsSummary"));

    // Candidate healthcare pre-employment questionaire fields
    this.addColumn("maritalStatus", "What is your marital status?",
      new PublishedDocFieldSource("maritalStatus"));
    this.addColumn("nmcPrn", "What is your NMC PRN?",
      new PublishedDocPropertySource("nmcPrn"));
    this.addColumn("childrenToUk", "How many children do you want to bring to the UK?",
      new PublishedDocPropertySource("childrenToUk"));
    this.addColumn("spouseJobUk", "Is your spouse also applying for a job in the UK?",
      new PublishedDocPropertySource("spouseJobUk"));
    this.addColumn("validPassport", "Do you have a valid passport?",
      new PublishedDocPropertySource("validPassport"));
    this.addColumn("covidVaccinatedStatus", "Are you fully vaccinated (double dose) or partially vaccinated (1st dose only) against Covid-19?",
      new PublishedDocFieldSource("covidVaccinatedStatus"));
    this.addColumn("covidVaccinatedDate", "What was the date of your last Covid-19 vaccination?",
      new PublishedDocFieldSource("covidVaccinatedDate"));
    this.addColumn("covidVaccinatedName", "What was the name of your last Covid-19 vaccination?",
      new PublishedDocFieldSource("covidVaccineName"));
    this.addColumn("familyFriendsUkLoc", "If you have family/friends in the UK where are they located?",
      new PublishedDocPropertySource("familyFriendsUkLoc"));
    this.addColumn("placedTbbCandidate", "Do you want to be placed with another TBB candidate?",
      new PublishedDocPropertySource("placedTbbCandidate"));
    this.addColumn("studiedEnglishUniversity", "Did you study in English at University?",
      new PublishedDocPropertySource("studiedEnglishUniversity"));
    this.addColumn("minBachelorsDegree", "Did you get at least a Bachelors degree?",
      new PublishedDocPropertySource("minBachelorsDegree"));
    this.addColumn("noticePeriod", "How long is your notice period?",
      new PublishedDocPropertySource("noticePeriod"));
    this.addColumn("colleaguesKnowRefugeeStatus", "Are you happy for colleagues to know you are a refugee/displaced talent at work?",
      new PublishedDocPropertySource("colleaguesKnowRefugeeStatus"));
    this.addColumn("ownLaptopUK", "Do you have a laptop you can bring to the UK?",
      new PublishedDocPropertySource("ownLaptopUK"));
    this.addColumn("shareDetailsTbbPartners", "Do you agree to TBB sharing your contact details with our partners?",
      new PublishedDocPropertySource("shareDetailsTbbPartners"));
    this.addColumn("mediaWillingness", "Do you agree to be included in media that is shared by TBB and partners on social media? ",
      new PublishedDocFieldSource("mediaWillingness"));

    this.addColumn("shareableNotes", "Notes", new PublishedDocFieldSource("shareableNotes"))
    .width = PublishedDocColumnWidth.Wide;

    this.addColumnWithLink("doc", "Other document", new PublishedDocConstantSource("doc"),
      new PublishedDocFieldSource("shareableDoc.url"))
    .width = PublishedDocColumnWidth.Narrow;

    this.addColumnWithLink("cv", "CV", new PublishedDocConstantSource("cv"),
      new PublishedDocFieldSource("shareableCv.url"))
    .width = PublishedDocColumnWidth.Narrow;

    this.addColumnWithLink("linkedIn", "LinkedIn Link", new PublishedDocConstantSource("link"),
      new PublishedDocFieldSource("linkedInLink"))
    .width = PublishedDocColumnWidth.Narrow;

    this.addColumnWithLink("address", "Folder: Address",
      new PublishedDocConstantSource("folder"),
      new PublishedDocFieldSource("folderlinkAddress"))
      .width = PublishedDocColumnWidth.Narrow;

    this.addColumnWithLink("character", "Folder: Character",
      new PublishedDocConstantSource("folder"),
      new PublishedDocFieldSource("folderlinkCharacter"))
      .width = PublishedDocColumnWidth.Narrow;

    this.addColumnWithLink("employer", "Folder: Employer",
      new PublishedDocConstantSource("folder"),
      new PublishedDocFieldSource("folderlinkEmployer"))
    .width = PublishedDocColumnWidth.Narrow;

    this.addColumnWithLink("engagement", "Folder: Engagement",
      new PublishedDocConstantSource("folder"),
      new PublishedDocFieldSource("folderlinkEngagement"))
    .width = PublishedDocColumnWidth.Narrow;

    this.addColumnWithLink("experience", "Folder: Experience",
      new PublishedDocConstantSource("folder"),
      new PublishedDocFieldSource("folderlinkExperience"))
    .width = PublishedDocColumnWidth.Narrow;

    this.addColumnWithLink("family", "Folder: Family",
      new PublishedDocConstantSource("folder"),
      new PublishedDocFieldSource("folderlinkFamily"))
    .width = PublishedDocColumnWidth.Narrow;

    this.addColumnWithLink("identity", "Folder: Identity",
      new PublishedDocConstantSource("folder"),
      new PublishedDocFieldSource("folderlinkIdentity"))
      .width = PublishedDocColumnWidth.Narrow;

    this.addColumnWithLink("immigration", "Folder: Immigration",
      new PublishedDocConstantSource("folder"),
      new PublishedDocFieldSource("folderlinkImmigration"))
      .width = PublishedDocColumnWidth.Narrow;

    this.addColumnWithLink("language", "Folder: Language",
      new PublishedDocConstantSource("folder"),
      new PublishedDocFieldSource("folderlinkLanguage"))
      .width = PublishedDocColumnWidth.Narrow;

    this.addColumnWithLink("medical", "Folder: Medical",
      new PublishedDocConstantSource("folder"),
      new PublishedDocFieldSource("folderlinkMedical"))
    .width = PublishedDocColumnWidth.Narrow;

    this.addColumnWithLink("qualification", "Folder: Qualification",
      new PublishedDocConstantSource("folder"),
      new PublishedDocFieldSource("folderlinkQualification"))
      .width = PublishedDocColumnWidth.Narrow;

    this.addColumnWithLink("registration", "Folder: Registration",
      new PublishedDocConstantSource("folder"),
      new PublishedDocFieldSource("folderlinkRegistration"))
    .width = PublishedDocColumnWidth.Narrow;

    this.loadDynamicProperties();
  }

  private loadDynamicProperties() {
    //We want to load all properties, but Spring Data Rest only provides paged access.
    //So to get them simply in one go, I am just setting a huge page size.
    //Alternately, you could use "expand" in a "pipe" to keep calling "get" repeatedly with a normal
    //page size. But the code for that is a bit cryptic, and this is good enough.
    //We don't expect to have a huge number of properties.
    this.candidatePropertyDefinitionService.get(0,100000).subscribe({
      next: page =>
          this.processDynamicProperties(page._embedded?.candidatePropertyDefinitions),
      error: err => {
        console.log("Error getting dynamic properties: " + err);
      }
    })
  }

  private processDynamicProperties(defs: CandidatePropertyDefinition[]) {
    if (defs != null) {
      defs.forEach(def => {
        //Use name as the label if we don't have a label
        this.addColumn(def.name, def.label ? def.label : def.name,
          new PublishedDocPropertySource(def.name));
      })
    }
  }

  getColumnConfigFromExportColumns(exportColumns: ExportColumn[]): PublishedDocColumnConfig[] {
    const columnConfigs: PublishedDocColumnConfig[] = [];
    for (const exportColumn of exportColumns) {
      const config = this.getDefaultColumnConfigFromKey(exportColumn.key);
      if (config != null) {
        const props = config.columnProps;
        if (exportColumn.properties != null) {
          props.header = exportColumn.properties?.header;
          props.constant = exportColumn.properties?.constant;
        }
        columnConfigs.push(config);
      }
    }
    return columnConfigs;
  }

  getColumnConfigFromAllColumns(): PublishedDocColumnConfig[] {
    const columnConfigs: PublishedDocColumnConfig[] = [];
    for (const exportColumn of this.allColumnInfosMap) {
      const columnDef = this.getColumnDefFromKey(exportColumn[0]);
      if (columnDef != null) {
        const config = new PublishedDocColumnConfig();
        config.columnDef = columnDef;
        columnConfigs.push(config);
      }
    }
    return columnConfigs;
  }

  private getColumnDefFromKey(columnKey: string): PublishedDocColumnDef {
    return this.allColumnInfosMap.get(columnKey);
  }

  private getDefaultColumnConfigFromKey(columnKey: string): PublishedDocColumnConfig {
    const columnDef = this.getColumnDefFromKey(columnKey);
    if (columnDef == null) {
      return null;
    }
    const config = new PublishedDocColumnConfig();
    config.columnDef = columnDef;
    config.columnProps = new PublishedDocColumnProps();
    return config;
  }

  public getDefaultColumns(): PublishedDocColumnConfig[] {
    const columns: PublishedDocColumnConfig[] = [];
    columns.push(this.getDefaultColumnConfigFromKey("candidateNumberLinkCv"));
    columns.push(this.getDefaultColumnConfigFromKey("name"));
    columns.push(this.getDefaultColumnConfigFromKey("shareableNotes"));
    columns.push(this.getDefaultColumnConfigFromKey("contextNote"));
    return columns;
  }

  private addColumnWithTypeLink(
    key: string, name: string, type: PublishedDocColumnType, value: PublishedDocValueSource,
    link: PublishedDocValueSource): PublishedDocColumnDef {
    const info = new PublishedDocColumnDef(key, name);
    info.type = type;
    info.content.value = value;
    info.content.link = link;
    this.allColumnInfosMap.set(key, info);
    return info;
  }

  private addColumnWithLink(key: string, name: string, value: PublishedDocValueSource,
                            link: PublishedDocValueSource): PublishedDocColumnDef {
    return this.addColumnWithTypeLink(key, name, PublishedDocColumnType.DisplayOnly,
      value, link);
  }

  private addColumn(key: string, name: string, value: PublishedDocValueSource): PublishedDocColumnDef {
    return this.addColumnWithLink(key, name, value, null);
  }

  private addColumnWithType(key: string, name: string, type: PublishedDocColumnType,
                            value: PublishedDocValueSource): PublishedDocColumnDef {
    return this.addColumnWithTypeLink(key, name, type, value, null);
  }
}
