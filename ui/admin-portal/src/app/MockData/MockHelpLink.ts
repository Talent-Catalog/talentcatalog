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
import {HelpFocus, HelpLink, NextStepInfo} from "../model/help-link";
import {Country} from "../model/country";
import {CandidateOpportunityStage} from "../model/candidate-opportunity";
import {JobOpportunityStage} from "../model/job";
import {User} from "../model/user";
import {MockJob} from "./MockJob";

export class MockHelpLink implements HelpLink {
  id: number;
  label: string;
  link: string;
  country?: Country;
  caseStage?: CandidateOpportunityStage;
  focus?: HelpFocus;
  jobStage?: JobOpportunityStage;
  nextStepInfo?: NextStepInfo;
  createdBy: User;
  createdDate: Date;
  updatedBy?: User;
  updatedDate?: Date;

  constructor(
    id: number,
    label: string,
    link: string,
    country?: Country,
    caseStage?: CandidateOpportunityStage,
    focus?: HelpFocus,
    jobStage?: JobOpportunityStage,
    nextStepInfo?: NextStepInfo,
    createdBy?: User,
    createdDate?: Date,
    updatedBy?: User,
    updatedDate?: Date
  ) {
    this.id = id;
    this.label = label;
    this.link = link;
    this.country = country;
    this.caseStage = caseStage;
    this.focus = focus;
    this.jobStage = jobStage;
    this.nextStepInfo = nextStepInfo;
    this.createdBy = createdBy;
    this.createdDate = createdDate;
    this.updatedBy = updatedBy;
    this.updatedDate = updatedDate;
  }
}

export const MOCK_HELP_LINK: HelpLink = {
  id: 1,
  label: 'Example Help Link',
  link: 'http://example.com',
  country: MockJob.country,
  caseStage: CandidateOpportunityStage.cvReview,
  focus: HelpFocus.updateNextStep,
  jobStage: JobOpportunityStage.jobOffer,
  nextStepInfo: new class implements NextStepInfo {
    nextStepDays: number;
    nextStepName: string;
    nextStepText: string;
  }(),
  createdBy: new User(/* user data */),
  createdDate: new Date(),
  updatedBy: new User(/* user data */),
  updatedDate: new Date()
};

export default MOCK_HELP_LINK;
