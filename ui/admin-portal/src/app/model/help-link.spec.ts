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

import {
  HelpFocus,
  HelpLink,
  NextStepInfo,
  SearchHelpLinkRequest,
  UpdateHelpLinkRequest
} from './help-link';
import {CandidateOpportunityStage} from './candidate-opportunity';
import {JobOpportunityStage} from './job';
import {User} from './user';
import {Country} from './country';
import {MockUser} from "../MockData/MockUser";

describe('HelpLink', () => {
  it('should create a HelpLink instance', () => {
    const user: User = new MockUser();
    const country: Country = { id: 1, name: 'Country1', status: 'Active', translatedName: 'Country One' }; // Mock country
    const caseStage: CandidateOpportunityStage = CandidateOpportunityStage.fullIntake;
    const jobStage: JobOpportunityStage = JobOpportunityStage.jobOffer;
    const nextStepInfo: NextStepInfo = { nextStepDays: 5, nextStepName: 'NextStep1', nextStepText: 'Next step details' }; // Mock next step info

    const helpLink: HelpLink = {
      id: 1,
      label: 'Help Link 1',
      link: 'https://example.com/help',
      country: country,
      caseStage: caseStage,
      focus: HelpFocus.updateNextStep,
      jobStage: jobStage,
      nextStepInfo: nextStepInfo,
      createdBy: user,
      createdDate: new Date(),
      updatedBy: user,
      updatedDate: new Date()
    };

    expect(helpLink).toBeTruthy();
    expect(helpLink.id).toBe(1);
    expect(helpLink.label).toBe('Help Link 1');
    expect(helpLink.link).toBe('https://example.com/help');
    expect(helpLink.country).toBe(country);
    expect(helpLink.caseStage).toBe(caseStage);
    expect(helpLink.focus).toBe(HelpFocus.updateNextStep);
    expect(helpLink.jobStage).toBe(jobStage);
    expect(helpLink.nextStepInfo).toBe(nextStepInfo);
    expect(helpLink.createdBy).toBe(user);
    expect(helpLink.updatedBy).toBe(user);
  });
});

describe('SearchHelpLinkRequest', () => {
  it('should create a SearchHelpLinkRequest instance', () => {
    const searchRequest: SearchHelpLinkRequest = new SearchHelpLinkRequest();
    searchRequest.countryId = 1;
    searchRequest.caseOppId = 1;
    searchRequest.caseStage = 'Stage1';
    searchRequest.focus = 'Update Next Step';
    searchRequest.jobOppId = 1;
    searchRequest.jobStage = 'JobStage1';
    searchRequest.nextStepName = 'NextStep1';
    searchRequest.userId = 1;

    expect(searchRequest).toBeTruthy();
    expect(searchRequest.countryId).toBe(1);
    expect(searchRequest.caseOppId).toBe(1);
    expect(searchRequest.caseStage).toBe('Stage1');
    expect(searchRequest.focus).toBe('Update Next Step');
    expect(searchRequest.jobOppId).toBe(1);
    expect(searchRequest.jobStage).toBe('JobStage1');
    expect(searchRequest.nextStepName).toBe('NextStep1');
    expect(searchRequest.userId).toBe(1);
  });
});

describe('UpdateHelpLinkRequest', () => {
  it('should create an UpdateHelpLinkRequest instance', () => {
    const updateRequest: UpdateHelpLinkRequest = {
      label: 'Updated Help Link',
      link: 'https://example.com/updated-help',
      countryId: 2,
      caseStage: CandidateOpportunityStage.fullIntake,
      focus: HelpFocus.updateStage,
      jobStage: JobOpportunityStage.jobOffer,
      nextStepInfo: { nextStepDays: 7, nextStepName: 'NextStep2', nextStepText: 'Updated next step details' }
    };

    expect(updateRequest).toBeTruthy();
    expect(updateRequest.label).toBe('Updated Help Link');
    expect(updateRequest.link).toBe('https://example.com/updated-help');
    expect(updateRequest.countryId).toBe(2);
    expect(updateRequest.caseStage).toEqual(CandidateOpportunityStage.fullIntake);
    expect(updateRequest.focus).toBe(HelpFocus.updateStage);
    expect(updateRequest.jobStage).toEqual(JobOpportunityStage.jobOffer);
    expect(updateRequest.nextStepInfo).toEqual({ nextStepDays: 7, nextStepName: 'NextStep2', nextStepText: 'Updated next step details' });
  });
});
