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

import {BrowserModule, Title} from '@angular/platform-browser';
import {APP_INITIALIZER, NgModule} from '@angular/core';
import {NgbDateAdapter, NgbDateParserFormatter, NgbModule} from '@ng-bootstrap/ng-bootstrap';
import {DatePipe, TitleCasePipe} from '@angular/common';

import {AppRoutingModule} from './app-routing.module';
import {AppComponent} from './components/app.component';
import {HeaderComponent} from './components/header/header.component';
import {ShowCandidatesComponent} from './components/candidates/show/show-candidates.component';
import {HomeComponent} from './components/candidates/home.component';
import {HTTP_INTERCEPTORS, HttpClientModule} from '@angular/common/http';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {ViewCandidateComponent} from './components/candidates/view/view-candidate.component';
import {EditCandidateStatusComponent} from './components/candidates/view/status/edit-candidate-status.component';
import {DeleteCandidateComponent} from './components/candidates/view/delete/delete-candidate.component';
import {InfiniteScrollModule} from 'ngx-infinite-scroll';
import {JwtInterceptor} from './services/jwt.interceptor';
import {ErrorInterceptor} from './services/error.interceptor';
import {AuthorizationService} from './services/authorization.service';
import {LoginComponent} from './components/login/login.component';
import {SettingsComponent} from './components/settings/settings.component';
import {SearchUsersComponent} from './components/settings/users/search-users.component';
import {ConfirmationComponent} from './components/util/confirm/confirmation.component';
import {SearchCountriesComponent} from './components/settings/countries/search-countries.component';
import {CreateCountryComponent} from './components/settings/countries/create/create-country.component';
import {EditCountryComponent} from './components/settings/countries/edit/edit-country.component';
import {SearchLanguagesComponent} from './components/settings/languages/search-languages.component';
import {CreateLanguageComponent} from './components/settings/languages/create/create-language.component';
import {EditLanguageComponent} from './components/settings/languages/edit/edit-language.component';
import {SearchSavedSearchesComponent} from './components/search/load-search/search-saved-searches.component';
import {CreateUpdateSearchComponent} from './components/search/create-update/create-update-search.component';
import {CandidateSearchCardComponent} from './components/util/candidate-search-card/candidate-search-card.component';
import {
  CandidateGeneralTabComponent
} from './components/candidates/view/tab/candidate-general-tab/candidate-general-tab.component';
import {
  CandidateExperienceTabComponent
} from './components/candidates/view/tab/candidate-experience-tab/candidate-experience-tab.component';
import {
  CandidateHistoryTabComponent
} from './components/candidates/view/tab/candidate-history-tab/candidate-history-tab.component';
import {
  CandidateEligibilityTabComponent
} from './components/candidates/view/tab/candidate-eligibility-tab/candidate-eligibility-tab.component';
import {SearchOccupationsComponent} from './components/settings/occupations/search-occupations.component';
import {CreateOccupationComponent} from './components/settings/occupations/create/create-occupation.component';
import {EditOccupationComponent} from './components/settings/occupations/edit/edit-occupation.component';
import {SearchIndustriesComponent} from './components/settings/industries/search-industries.component';
import {CreateIndustryComponent} from './components/settings/industries/create/create-industry.component';
import {EditIndustryComponent} from './components/settings/industries/edit/edit-industry.component';
import {SearchLanguageLevelsComponent} from './components/settings/language-levels/search-language-levels.component';
import {
  CreateLanguageLevelComponent
} from './components/settings/language-levels/create/create-language-level.component';
import {EditLanguageLevelComponent} from './components/settings/language-levels/edit/edit-language-level.component';
import {SearchEducationLevelsComponent} from './components/settings/education-levels/search-education-levels.component';
import {
  CreateEducationLevelComponent
} from './components/settings/education-levels/create/create-education-level.component';
import {EditEducationLevelComponent} from './components/settings/education-levels/edit/edit-education-level.component';
import {SearchEducationMajorsComponent} from './components/settings/education-majors/search-education-majors.component';
import {
  CreateEducationMajorComponent
} from './components/settings/education-majors/create/create-education-major.component';
import {EditEducationMajorComponent} from './components/settings/education-majors/edit/edit-education-major.component';
import {
  DropdownTranslationsComponent
} from './components/settings/translations/dropdowns/dropdown-translations.component';

import {ViewCandidateContactComponent} from './components/candidates/view/contact/view-candidate-contact.component';
import {ViewCandidateLanguageComponent} from './components/candidates/view/language/view-candidate-language.component';
import {
  EditCandidateContactComponent
} from './components/candidates/view/contact/edit/edit-candidate-contact.component';

import {ViewCandidateNoteComponent} from './components/candidates/view/note/view-candidate-note.component';
import {CreateCandidateNoteComponent} from './components/candidates/view/note/create/create-candidate-note.component';
import {EditCandidateNoteComponent} from './components/candidates/view/note/edit/edit-candidate-note.component';

import {
  ViewCandidateEducationComponent
} from './components/candidates/view/education/view-candidate-education.component';
import {
  CreateCandidateEducationComponent
} from './components/candidates/view/education/create/create-candidate-education.component';
import {
  EditCandidateEducationComponent
} from './components/candidates/view/education/edit/edit-candidate-education.component';

import {
  ViewCandidateCertificationComponent
} from './components/candidates/view/certification/view-candidate-certification.component';
import {
  CreateCandidateCertificationComponent
} from './components/candidates/view/certification/create/create-candidate-certification.component';
import {
  EditCandidateCertificationComponent
} from './components/candidates/view/certification/edit/edit-candidate-certification.component';
import {
  ViewCandidateOccupationComponent
} from './components/candidates/view/occupation/view-candidate-occupation.component';
import {
  ViewCandidateJobExperienceComponent
} from './components/candidates/view/occupation/experience/view-candidate-job-experience.component';
import {CreateUpdateUserComponent} from './components/settings/users/create-update-user/create-update-user.component';
import {
  CandidateEducationTabComponent
} from './components/candidates/view/tab/candidate-education-tab/candidate-education-tab.component';
import {JoinSavedSearchComponent} from './components/search/join-search/join-saved-search.component';
import {CandidateSourceComponent} from './components/util/candidate-source/candidate-source.component';
import {
  LanguageLevelFormControlComponent
} from './components/util/form/language-proficiency/language-level-form-control.component';
import {CandidatePipe} from './pipes/candidate.pipe';
import {
  EditCandidateReviewStatusItemComponent
} from './components/util/candidate-review/edit/edit-candidate-review-status-item.component';
import {
  CandidateReviewStatusItemComponent
} from './components/util/candidate-review/candidate-review-status-item.component';
import {UserPipe} from './components/util/user/user.pipe';
import {UpdatedByComponent} from './components/util/user/updated-by/updated-by.component';
import {DateRangePickerComponent} from './components/util/form/date-range-picker/date-range-picker.component';
import {
  EditCandidateJobExperienceComponent
} from './components/candidates/view/occupation/experience/edit/edit-candidate-job-experience.component';
import {
  CreateCandidateJobExperienceComponent
} from './components/candidates/view/occupation/experience/create/create-candidate-job-experience.component';
import {
  ViewCandidateAttachmentComponent
} from './components/candidates/view/attachment/view-candidate-attachment.component';
import {
  EditCandidateOccupationComponent
} from './components/candidates/view/occupation/edit/edit-candidate-occupation.component';
import {SortedByComponent} from './components/util/sort/sorted-by.component';
import {
  EditCandidateLanguageComponent
} from './components/candidates/view/language/edit/edit-candidate-language.component';
import {ViewCandidateAccountComponent} from './components/candidates/view/account/view-candidate-account.component';
import {ChangePasswordComponent} from './components/account/change-password/change-password.component';
import {
  CreateCandidateAttachmentComponent
} from './components/candidates/view/attachment/create/create-candidate-attachment.component';
import {
  EditCandidateAttachmentComponent
} from './components/candidates/view/attachment/edit/edit-candidate-attachment.component';
import {FileUploadComponent} from './components/util/file-upload/file-upload.component';
import {
  CandidateAdditionalInfoTabComponent
} from './components/candidates/view/tab/candidate-additional-info-tab/candidate-additional-info-tab.component';
import {
  ViewCandidateAdditionalInfoComponent
} from './components/candidates/view/additional-info/view-candidate-additional-info.component';
import {ViewCandidateSkillComponent} from './components/candidates/view/skill/view-candidate-skill.component';
import {BrowseCandidateSourcesComponent} from './components/candidates/show/browse/browse-candidate-sources.component';
import {NgChartsModule} from 'ng2-charts';
import {InfographicComponent} from './components/infographics/infographic.component';
import {ChartComponent} from './components/infographics/chart/chart.component';
import {MonthPickerComponent} from './components/util/month-picker/month-picker.component';
import {CandidateSourceResultsComponent} from './components/candidates/show/returns/candidate-source-results.component';
import {DefineSearchComponent} from './components/search/define-search/define-search.component';
import {NotFoundComponent} from './not-found/not-found.component';
import {GeneralTranslationsComponent} from './components/settings/translations/general/general-translations.component';
import {
  ViewCandidateSpecialLinksComponent
} from './components/candidates/view/special-links/view-candidate-special-links.component';
import {
  EditCandidateSpecialLinksComponent
} from './components/candidates/view/special-links/edit/edit-candidate-special-links.component';
import {NgxWigModule} from 'ngx-wig';
import {ViewCandidateSurveyComponent} from './components/candidates/view/survey/view-candidate-survey.component';
import {
  EditCandidateAdditionalInfoComponent
} from './components/candidates/view/additional-info/edit/edit-candidate-additional-info.component';
import {EditCandidateSurveyComponent} from './components/candidates/view/survey/edit/edit-candidate-survey.component';
import {CreateUpdateListComponent} from './components/list/create-update/create-update-list.component';
import {SelectListComponent} from './components/list/select/select-list.component';
import {CandidatesSearchComponent} from './components/candidates/candidates-search/candidates-search.component';
import {CandidatesListComponent} from './components/candidates/candidates-list/candidates-list.component';
import {
  CreateCandidateOccupationComponent
} from './components/candidates/view/occupation/create/create-candidate-occupation.component';
import {CvIconComponent} from './components/util/cv-icon/cv-icon.component';
import {JoblinkComponent} from './components/util/joblink/joblink.component';
import {CandidateContextNoteComponent} from './components/util/candidate-context-note/candidate-context-note.component';
import {ReturnedHomeComponent} from './components/candidates/intake/returned-home/returned-home.component';
import {
  CandidateIntakeTabComponent
} from './components/candidates/view/tab/candidate-intake-tab/candidate-intake-tab.component';
import {VisaIssuesComponent} from './components/candidates/intake/visa-issues/visa-issues.component';
import {CitizenshipsComponent} from './components/candidates/intake/citizenships/citizenships.component';
import {
  CandidateCitizenshipCardComponent
} from './components/candidates/intake/citizenships/card/candidate-citizenship-card.component';
import {AvailImmediateComponent} from './components/candidates/intake/avail-immediate/avail-immediate.component';
import {FamilyComponent} from './components/candidates/intake/family/family.component';
import {
  CandidateMiniIntakeTabComponent
} from './components/candidates/view/tab/candidate-mini-intake-tab/candidate-mini-intake-tab.component';
import {IntRecruitmentComponent} from './components/candidates/intake/int-recruitment/int-recruitment.component';
import {RuralComponent} from './components/candidates/intake/rural/rural.component';
import {ReturnHomeSafeComponent} from './components/candidates/intake/return-home-safe/return-home-safe.component';
import {WorkPermitComponent} from './components/candidates/intake/work-permit/work-permit.component';
import {WorkLegallyComponent} from './components/candidates/intake/work-legally/work-legally.component';
import {WorkStatusComponent} from './components/candidates/intake/work-status/work-status.component';
import {HostEntryComponent} from './components/candidates/intake/host-entry/host-entry.component';
import {
  CandidateVisaTabComponent
} from './components/candidates/view/tab/candidate-visa-tab/candidate-visa-tab.component';
import {CustomDateAdapter, CustomDateParserFormatter} from './util/date-adapter/ngb-date-adapter';
import {
  RegistrationUnhcrComponent
} from './components/candidates/intake/registration-unhcr/registration-unhcr.component';
import {
  RegistrationUnrwaComponent
} from './components/candidates/intake/registration-unrwa/registration-unrwa.component';
import {HomeLocationComponent} from './components/candidates/intake/home-location/home-location.component';
import {AsylumYearComponent} from './components/candidates/intake/asylum-year/asylum-year.component';
import {VisaAssessmentComponent} from './components/candidates/intake/visa-assessment/visa-assessment.component';
import {VisaCheckAuComponent} from './components/candidates/view/tab/candidate-visa-tab/au/visa-check-au.component';
import {VisaCheckCaComponent} from './components/candidates/view/tab/candidate-visa-tab/ca/visa-check-ca.component';
import {VisaCheckNzComponent} from './components/candidates/view/tab/candidate-visa-tab/nz/visa-check-nz.component';
import {VisaCheckUkComponent} from './components/candidates/view/tab/candidate-visa-tab/uk/visa-check-uk.component';
import {DestinationLimitComponent} from './components/candidates/intake/destination-limit/destination-limit.component';
import {FixedInputComponent} from './components/util/intake/fixed-input/fixed-input.component';
import {ConfirmContactComponent} from './components/candidates/intake/confirm-contact/confirm-contact.component';
import {ExamsComponent} from './components/candidates/intake/exams/exams.component';
import {CandidateExamCardComponent} from './components/candidates/intake/exams/card/candidate-exam-card.component';
import {HasNameSelectorComponent} from './components/util/has-name-selector/has-name-selector.component';
import {DestinationJobComponent} from './components/candidates/intake/destination-job/destination-job.component';
import {CrimeComponent} from './components/candidates/intake/crime/crime.component';
import {ConflictComponent} from './components/candidates/intake/conflict/conflict.component';
import {ResidenceStatusComponent} from './components/candidates/intake/residence-status/residence-status.component';
import {WorkAbroadComponent} from './components/candidates/intake/work-abroad/work-abroad.component';
import {
  HostEntryLegallyComponent
} from './components/candidates/intake/host-entry-legally/host-entry-legally.component';
import {LeftHomeReasonComponent} from './components/candidates/intake/left-home-reasons/left-home-reason.component';
import {
  ReturnHomeFutureComponent
} from './components/candidates/intake/return-home-future/return-home-future.component';
import {
  ResettlementThirdComponent
} from './components/candidates/intake/resettlement-third/resettlement-third.component';
import {HostChallengesComponent} from './components/candidates/intake/host-challenges/host-challenges.component';
import {MaritalStatusComponent} from './components/candidates/intake/marital-status/marital-status.component';
import {AutosaveStatusComponent} from './components/util/autosave-status/autosave-status.component';
import {DragulaModule} from 'ng2-dragula';
import {
  CandidateColumnSelectorComponent
} from './components/util/candidate-column-selector/candidate-column-selector.component';
import {
  CandidateNameNumSearchComponent
} from './components/util/candidate-name-num-search/candidate-name-num-search.component';
import {MilitaryServiceComponent} from './components/candidates/intake/military-service/military-service.component';
import {VisaRejectComponent} from './components/candidates/intake/visa-reject/visa-reject.component';
import {DrivingLicenseComponent} from './components/candidates/intake/driving-license/driving-license.component';
import {DependantsComponent} from './components/candidates/intake/dependants/dependants.component';
import {DependantsCardComponent} from './components/candidates/intake/dependants/card/dependants-card.component';
import {LangAssessmentComponent} from './components/candidates/intake/lang-assessment/lang-assessment.component';
import {ExtendDatePipe} from './util/date-adapter/extend-date-pipe';
import {DatePickerComponent} from './components/util/date-picker/date-picker.component';
import {IntProtectionComponent} from "./components/candidates/visa/int-protection/int-protection.component";
import {
  CharacterAssessmentComponent
} from "./components/candidates/visa/character-assessment/character-assessment.component";
import {SecurityRiskComponent} from "./components/candidates/visa/security-risk/security-risk.component";
import {TravelDocumentComponent} from "./components/candidates/visa/travel-document/travel-document.component";
import {
  SalaryTsmitComponent
} from "./components/candidates/visa/visa-job-assessments/salary-tsmit/salary-tsmit.component";
import {
  RegionalAreaComponent
} from "./components/candidates/visa/visa-job-assessments/regional-area/regional-area.component";
import {
  JobInterestComponent
} from "./components/candidates/visa/visa-job-assessments/job-interest/job-interest.component";
import {
  JobFamilyAusComponent
} from "./components/candidates/visa/visa-job-assessments/job-family-aus/job-family-aus.component";
import {
  JobEligibilityAssessmentComponent
} from "./components/candidates/visa/visa-job-assessments/job-eligibility-assessment/job-eligibility-assessment.component";
import {
  VisaFourNineFourComponent
} from "./components/candidates/visa/visa-job-assessments/visa-four-nine-four/visa-four-nine-four.component";
import {
  VisaOneEightSixComponent
} from "./components/candidates/visa/visa-job-assessments/visa-one-eight-six/visa-one-eight-six.component";
import {
  VisaOtherOptionsComponent
} from "./components/candidates/visa/visa-job-assessments/visa-other-options/visa-other-options.component";
import {IeltsLevelComponent} from "./components/candidates/visa/visa-job-assessments/ielts-level/ielts-level.component";
import {
  QualificationRelevantComponent
} from "./components/candidates/visa/visa-job-assessments/qualification-relevant/qualification-relevant.component";
import {
  VisaFinalAssessmentComponent
} from "./components/candidates/visa/visa-job-assessments/visa-final-assessment/visa-final-assessment.component";
import {
  JobOccupationComponent
} from "./components/candidates/visa/visa-job-assessments/job-occupation/job-occupation.component";
import {RiskAssessmentComponent} from "./components/candidates/visa/risk-assessment/risk-assessment.component";
import {HealthAssessmentComponent} from "./components/candidates/visa/health-assessment/health-assessment.component";
import {ShowQrCodeComponent} from './components/util/qr/show-qr-code/show-qr-code.component';
import {HealthIssuesComponent} from './components/candidates/intake/health-issues/health-issues.component';
import {
  VisaJobPutForwardComponent
} from './components/candidates/visa/visa-job-assessments/put-forward/visa-job-put-forward.component';
import {
  VisaJobNotesComponent
} from './components/candidates/visa/visa-job-assessments/visa-job-notes/visa-job-notes.component';
import {
  VisaJobCheckAuComponent
} from './components/candidates/view/tab/candidate-visa-tab/au/job/visa-job-check-au.component';
import {
  CandidateStatusSelectorComponent
} from './components/util/candidate-status-selector/candidate-status-selector.component';
import {FinalAgreementComponent} from './components/candidates/intake/final-agreement/final-agreement.component';
import {NgSelectModule} from "@ng-select/ng-select";
import {
  CreateCandidateLanguageComponent
} from './components/candidates/view/language/create/create-candidate-language.component';
import {EditCandidateOppComponent} from './components/candidate-opp/edit-candidate-opp/edit-candidate-opp.component';
import {IeltsScoreValidationComponent} from './components/util/ielts-score-validation/ielts-score-validation.component';
import {FileSelectorComponent} from './components/util/file-selector/file-selector.component';
import {NewJobComponent} from './components/job/new-job/new-job.component';
import {OldIntakeInputComponent} from './components/util/old-intake-input-modal/old-intake-input.component';
import {
  CandidateShareableNotesComponent
} from './components/util/candidate-shareable-notes/candidate-shareable-notes.component';
import {ShareableDocsComponent} from './components/candidates/view/shareable-docs/shareable-docs.component';
import {
  PublishedDocColumnSelectorComponent
} from "./components/util/published-doc-column-selector/published-doc-column-selector.component";
import {
  CandidateSourceDescriptionComponent
} from './components/util/candidate-source-description/candidate-source-description.component';
import {SearchExternalLinksComponent} from './components/settings/external-links/search-external-links.component';
import {CreateExternalLinkComponent} from './components/settings/external-links/create/create-external-link.component';
import {EditExternalLinkComponent} from './components/settings/external-links/edit/edit-external-link.component';
import {CovidVaccinationComponent} from './components/candidates/intake/vaccination/covid-vaccination.component';
import {EnglishThresholdComponent} from './components/candidates/visa/english-threshold/english-threshold.component';
import {FilterPipe} from "./pipes/filter.pipe";
import {SafePipe} from "./pipes/safe.pipe";
import {
  CandidateTaskTabComponent
} from './components/candidates/view/tab/candidate-task-tab/candidate-task-tab.component';
import {DownloadCvComponent} from './components/util/download-cv/download-cv.component';
import {AssignTasksListComponent} from './components/tasks/assign-tasks-list/assign-tasks-list.component';
import {
  AssignTasksCandidateComponent
} from './components/tasks/assign-tasks-candidate/assign-tasks-candidate.component';
import {EditTaskAssignmentComponent} from './components/candidates/view/tasks/edit/edit-task-assignment.component';
import {ViewCandidateTasksComponent} from "./components/candidates/view/tasks/view-candidate-tasks.component";
import {BrowseTasksComponent} from './components/tasks/browse-tasks/browse-tasks.component';
import {ViewTaskDetailsComponent} from './components/tasks/view-task-details/view-task-details.component';
import {SearchTasksComponent} from './components/settings/tasks/search-tasks.component';
import {TasksMonitorComponent} from './components/util/tasks-monitor/tasks-monitor.component';
import {
  ViewCandidateMediaWillingnessComponent
} from './components/candidates/view/media/view-candidate-media-willingness.component';
import {
  EditCandidateMediaWillingnessComponent
} from './components/candidates/view/media/edit/edit-candidate-media-willingness.component';
import {ViewResponseComponent} from './components/candidates/view/tasks/view-response/view-response.component';
import {
  ViewCandidateRegistrationComponent
} from './components/candidates/view/registration/view-candidate-registration.component';
import {
  EditCandidateRegistrationComponent
} from './components/candidates/view/registration/edit/edit-candidate-registration.component';
import {EditTaskComponent} from './components/settings/tasks/edit/edit-task.component';
import {TasksMonitorListComponent} from './components/util/tasks-monitor-list/tasks-monitor-list.component';
import {SearchPartnersComponent} from './components/settings/partners/search-partners/search-partners.component';
import {
  CreateUpdatePartnerComponent
} from './components/settings/partners/create-update-partner/create-update-partner.component';
import {RoleGuardService} from "./services/role-guard.service";
import {ViewJobComponent} from './components/job/view/view-job/view-job.component';
import {JobsComponent} from './components/job/jobs/jobs.component';
import {JobsWithDetailComponent} from './components/job/jobs-with-detail/jobs-with-detail.component';
import {ViewJobFromUrlComponent} from './components/job/view/view-job-from-url/view-job-from-url.component';
import {JobGeneralTabComponent} from './components/job/view/tab/job-general-tab/job-general-tab.component';
import {
  ViewJobDescriptionComponent
} from './components/job/view/description/view-job-description/view-job-description.component';
import {ViewJobInfoComponent} from './components/job/view/info/view-job-info/view-job-info.component';
import {
  ViewJobSubmissionListComponent
} from './components/job/view/submission-list/view-job-submission-list/view-job-submission-list.component';
import {EditJobInfoComponent} from './components/job/view/info/edit-job-info/edit-job-info.component';
import {
  ViewJobSuggestedListComponent
} from './components/job/view/suggested-list/view-job-suggested-list/view-job-suggested-list.component';
import {ViewJobSummaryComponent} from './components/job/view/summary/view-job-summary/view-job-summary.component';
import {EditJobSummaryComponent} from './components/job/view/summary/edit-job-summary/edit-job-summary.component';
import {
  JobSuggestedSearchesTabComponent
} from './components/job/view/tab/job-suggested-searches-tab/job-suggested-searches-tab.component';
import {
  ViewJobSuggestedSearchesComponent
} from './components/job/view/suggested-searches/view-job-suggested-searches/view-job-suggested-searches.component';
import {InputTextComponent} from './components/util/input/input-text/input-text.component';
import {JobUploadTabComponent} from './components/job/view/tab/job-upload-tab/job-upload-tab.component';
import {ViewJobUploadsComponent} from './components/job/view/uploads/view-job-uploads/view-job-uploads.component';
import {InputLinkComponent} from './components/util/input/input-link/input-link.component';
import {
  JobSourceContactsTabComponent
} from './components/job/view/tab/job-source-contacts-tab/job-source-contacts-tab.component';
import {
  ViewJobSourceContactsComponent
} from './components/job/view/source-contacts/view-job-source-contacts/view-job-source-contacts.component';
import {
  ViewJobPreparationItemsComponent
} from './components/job/view/preparation-items/view-job-preparation-items/view-job-preparation-items.component';
import {AdminApiComponent} from './components/settings/admin-api/admin-api.component';
import {JobIntakeTabComponent} from './components/job/view/tab/job-intake-tab/job-intake-tab.component';
import {CostCommitEmployerComponent} from './components/job/intake/cost-commit-employer/cost-commit-employer.component';
import {
  CandidateJobsTabComponent
} from './components/candidates/view/tab/candidate-jobs-tab/candidate-jobs-tab.component';
import {
  ViewCandidateJobsComponent
} from './components/candidates/view/jobs/view-candidate-jobs/view-candidate-jobs.component';
import {
  ViewCandidateOppFromUrlComponent
} from './components/candidate-opp/view-candidate-opp-from-url/view-candidate-opp-from-url.component';
import {ViewCandidateOppComponent} from './components/candidate-opp/view-candidate-opp/view-candidate-opp.component';
import {CandidateOppsComponent} from './components/candidate-opp/candidate-opps/candidate-opps.component';
import {
  CandidateOppsWithDetailComponent
} from './components/candidate-opp/candidate-opps-with-detail/candidate-opps-with-detail.component';
import {TruncatePipe} from "./pipes/truncate.pipe";
import {CreatedByComponent} from './components/util/user/created-by/created-by.component';
import {TailoredCvComponent} from './components/candidates/view/tailored-cv.component';
import {
  OpportunityStageNextStepComponent
} from './components/util/opportunity-stage-next-step/opportunity-stage-next-step.component';
import {EditOppComponent} from './components/opportunity/edit-opp/edit-opp.component';
import {PathwayAssessmentComponent} from './components/candidates/visa/pathway-assessment/pathway-assessment.component';
import {
  CandidateVisaJobComponent
} from './components/candidates/view/tab/candidate-visa-tab/job/candidate-visa-job.component';
import {CvPreviewComponent} from './components/util/cv-preview/cv-preview.component';
import {
  VisaJobCheckCaComponent
} from './components/candidates/view/tab/candidate-visa-tab/ca/job/visa-job-check-ca.component';
import {
  RelevantWorkExpComponent
} from './components/candidates/visa/visa-job-assessments/relevant-work-exp/relevant-work-exp.component';
import {
  AgeRequirementComponent
} from './components/candidates/visa/visa-job-assessments/age-requirement/age-requirement.component';
import {
  PreferredPathwaysComponent
} from './components/candidates/visa/visa-job-assessments/preferred-pathways/preferred-pathways.component';
import {
  EligiblePathwaysComponent
} from './components/candidates/visa/visa-job-assessments/eligible-pathways/eligible-pathways.component';
import {
  IneligiblePathwaysComponent
} from './components/candidates/visa/visa-job-assessments/ineligible-pathways/ineligible-pathways.component';
import {
  OccupationCategoryComponent
} from './components/candidates/visa/visa-job-assessments/occupation-category/occupation-category.component';
import {
  OccupationSubcategoryComponent
} from './components/candidates/visa/visa-job-assessments/occupation-subcategory/occupation-subcategory.component';
import {DirectiveModule} from "./directives/directive.module";
import {RxStompService} from "./services/rx-stomp.service";
import {CreateUpdatePostComponent} from './components/chat/create-update-post/create-update-post.component';
import {EnvService} from "./services/env.service";
import {ExportPdfComponent} from './components/util/export-pdf/export-pdf.component';
import {ChatsComponent} from './components/chat/chats/chats.component';
import {ChatsWithPostsComponent} from './components/chat/chats-with-posts/chats-with-posts.component';
import {ViewChatComponent} from './components/chat/view-chat/view-chat.component';
import {CreateUpdateChatComponent} from './components/chat/create-update-chat/create-update-chat.component';
import {ViewPostComponent} from './components/chat/view-post/view-post.component';
import {ManageChatsComponent} from './components/chat/manage-chats/manage-chats.component';
import {
  JobSourceContactsWithChatsComponent
} from './components/job/view/source-contacts/job-source-contacts-with-chats/job-source-contacts-with-chats.component';
import {ViewChatPostsComponent} from './components/chat/view-chat-posts/view-chat-posts.component';
import {JobGroupChatsTabComponent} from './components/job/view/tab/job-group-chats-tab/job-group-chats-tab.component';
import {
  RelocatingDependantsComponent
} from './components/candidates/visa/visa-job-assessments/relocating-dependants/relocating-dependants.component';
import {SearchHomeComponent} from './components/search/search-home/search-home.component';
import {JobHomeComponent} from './components/job/job-home/job-home.component';
import {ListHomeComponent} from './components/list/list-home/list-home.component';
import {QuillModule} from 'ngx-quill';
import {ChatReadStatusComponent} from './components/chat/chat-read-status/chat-read-status.component';
import {
  LanguageThresholdComponent
} from './components/candidates/visa/visa-job-assessments/language-threshold/language-threshold.component';
import {
  MonitoringEvaluationConsentComponent
} from './components/candidates/intake/monitoring-evaluation-consent/monitoring-evaluation-consent.component';
import {ResetPasswordComponent} from './components/account/reset-password/reset-password.component';
import {UserChangePasswordComponent} from './components/account/user-change-password/user-change-password.component';
import {PickerModule} from "@ctrl/ngx-emoji-mart";
import {NclcScoreValidationComponent} from './components/util/nclc-score-validation/nclc-score-validation.component';
import {ArrestImprisonComponent} from './components/candidates/intake/arrest-imprison/arrest-imprison.component';
import {HelpComponent} from './components/help/help.component';
import {SearchHelpLinksComponent} from './components/settings/help-links/search-help-links.component';
import {
  CreateUpdateHelpLinkComponent
} from './components/settings/help-links/create-update-help-link/create-update-help-link.component';
import {TranslateLoader, TranslateModule} from '@ngx-translate/core';
import {LanguageLoader} from "./services/language.loader";
import {SfJoblinkComponent} from './components/util/sf-joblink/sf-joblink.component';
import {SelectJobCopyComponent} from './components/util/select-job-copy/select-job-copy.component';
import {
  VisaJobCheckUkComponent
} from './components/candidates/view/tab/candidate-visa-tab/uk/job/visa-job-check-uk.component';
import {PreviewLinkComponent} from './components/chat/preview-link/preview-link.component';
import {BuildLinkComponent} from './util/build-link/build-link.component';
import {LinkTooltipComponent} from './util/link-tooltip/link-tooltip.component';
import {CandidatesWithChatComponent} from './components/chat/candidates-with-chat/candidates-with-chat.component';
import {
  ShowCandidatesWithChatComponent
} from './components/chat/show-candidates-with-chat/show-candidates-with-chat.component';
import {
  ViewCandidateDestinationsComponent
} from './components/candidates/view/destinations/view-candidate-destinations.component';
import {
  EditCandidateDestinationsComponent
} from './components/candidates/view/destinations/edit/edit-candidate-destinations/edit-candidate-destinations.component';
import {DestinationFamilyComponent} from './components/candidates/visa/destination-family/destination-family.component';
import {ViewCandidateExamComponent} from "./components/candidates/view/exam/view-candidate-exam.component";
import {CreateCandidateExamComponent} from "./components/candidates/view/exam/create/create-candidate-exam.component";
import {EditCandidateExamComponent} from "./components/candidates/view/exam/edit/edit-candidate-exam.component";
import {JoiDataComponent} from './components/job/intake/joi-data/joi-data.component';
import {
  FindCandidateSourceComponent
} from './components/candidates/find-candidate-source/find-candidate-source.component';
import {UnsavedChangesGuard} from "./services/unsaved-changes.guard";
import {
  PotentialDuplicateIconComponent
} from './components/candidates/potential-duplicates/potential-duplicate-icon/potential-duplicate-icon.component';
import {
  DuplicatesDetailComponent
} from './components/candidates/potential-duplicates/duplicates-detail/duplicates-detail.component';
import {
  ImportDuolingoCouponsComponent
} from "./components/settings/import-duolingo-coupons/import-duolingo-coupons.component";
import {DetScoreValidationComponent} from './components/util/det-score-validation/det-score-validation.component';
import {DuolingoAssignmentComponent} from './components/util/duolingo-assignment/duolingo-assignment.component';
import {VerifyEmailComponent} from './components/account/verify-email/verify-email.component';
import {VerifyEmailToastComponent} from './components/account/verify-email-toast/verify-email-toast.component';
import {PresetEmbedComponent} from './components/intelligence/preset-embed/preset-embed.component';
import {LoadingIndicatorComponent} from './components/util/loading-indicator/loading-indicator.component';
import {ErrorDisplayComponent} from './components/util/error-display/error-display.component';
import {IntelligenceComponent} from './components/intelligence/intelligence.component';
import {OfferToAssistComponent} from './components/settings/offer-to-assist/offer-to-assist.component';
import {IntlPhoneInputComponent} from './components/util/intl-phone-input/intl-phone-input.component';
import {
  EditMaxEducationLevelComponent
} from './components/candidates/view/education/edit-max-education-level/edit-max-education-level.component';
import {SharedModule} from './shared/shared.module';
import {
  ChatMuteToggleButtonComponent
} from './components/chat/chat-mute-toggle-button/chat-mute-toggle-button.component';

@NgModule({
  declarations: [
    AppComponent,
    LoginComponent,
    HeaderComponent,
    ConfirmationComponent,
    HomeComponent,
    ViewCandidateComponent,
    EditCandidateStatusComponent,
    DeleteCandidateComponent,
    SettingsComponent,
    SearchUsersComponent,
    SearchCountriesComponent,
    CreateCountryComponent,
    EditCountryComponent,
    SearchLanguagesComponent,
    CreateLanguageComponent,
    EditLanguageComponent,
    SearchSavedSearchesComponent,
    CreateUpdateSearchComponent,
    CandidateSearchCardComponent,
    CandidateGeneralTabComponent,
    CandidateExperienceTabComponent,
    CandidateHistoryTabComponent,
    CandidateEducationTabComponent,
    CandidateEligibilityTabComponent,
    SearchOccupationsComponent,
    CreateOccupationComponent,
    EditOccupationComponent,
    SearchIndustriesComponent,
    CreateIndustryComponent,
    EditIndustryComponent,
    SearchLanguageLevelsComponent,
    CreateLanguageLevelComponent,
    EditLanguageLevelComponent,
    SearchEducationLevelsComponent,
    CreateEducationLevelComponent,
    EditEducationLevelComponent,
    SearchEducationMajorsComponent,
    CreateEducationMajorComponent,
    EditEducationMajorComponent,
    CreateUpdateUserComponent,
    EditEducationMajorComponent,
    ViewCandidateContactComponent,
    EditCandidateContactComponent,
    ViewCandidateLanguageComponent,
    ViewCandidateNoteComponent,
    ViewCandidateAttachmentComponent,
    CreateCandidateNoteComponent,
    EditCandidateNoteComponent,
    ViewCandidateEducationComponent,
    CreateCandidateEducationComponent,
    EditCandidateEducationComponent,
    ViewCandidateCertificationComponent,
    CreateCandidateCertificationComponent,
    EditCandidateCertificationComponent,
    JoinSavedSearchComponent,
    CandidateReviewStatusItemComponent,
    EditCandidateReviewStatusItemComponent,
    DateRangePickerComponent,
    DropdownTranslationsComponent,
    LanguageLevelFormControlComponent,
    DateRangePickerComponent,
    UserPipe,
    UpdatedByComponent,
    CandidateSourceComponent,
    ViewCandidateOccupationComponent,
    ViewCandidateJobExperienceComponent,
    LanguageLevelFormControlComponent,
    CandidatePipe,
    EditCandidateJobExperienceComponent,
    CreateCandidateJobExperienceComponent,
    EditCandidateOccupationComponent,
    SortedByComponent,
    EditCandidateLanguageComponent,
    ViewCandidateAccountComponent,
    ChangePasswordComponent,
    CreateCandidateAttachmentComponent,
    EditCandidateAttachmentComponent,
    FileUploadComponent,
    CandidateAdditionalInfoTabComponent,
    ViewCandidateAdditionalInfoComponent,
    ViewCandidateSkillComponent,
    FileUploadComponent,
    InfographicComponent,
    ChartComponent,
    MonthPickerComponent,
    NotFoundComponent,
    GeneralTranslationsComponent,
    DefineSearchComponent,
    MonthPickerComponent,
    NotFoundComponent,
    ViewCandidateSpecialLinksComponent,
    EditCandidateSpecialLinksComponent,
    ViewCandidateSurveyComponent,
    EditCandidateAdditionalInfoComponent,
    EditCandidateSurveyComponent,
    CreateUpdateListComponent,
    SelectListComponent,
    BrowseCandidateSourcesComponent,
    CandidatesSearchComponent,
    CandidatesListComponent,
    CandidateSourceResultsComponent,
    CreateCandidateOccupationComponent,
    ShowCandidatesComponent,
    CreateUpdateListComponent,
    CreateCandidateOccupationComponent,
    CvIconComponent,
    JoblinkComponent,
    CandidateContextNoteComponent,
    ReturnedHomeComponent,
    CandidateIntakeTabComponent,
    VisaIssuesComponent,
    CitizenshipsComponent,
    CandidateCitizenshipCardComponent,
    AvailImmediateComponent,
    FamilyComponent,
    CandidateMiniIntakeTabComponent,
    IntRecruitmentComponent,
    RuralComponent,
    ReturnHomeSafeComponent,
    WorkPermitComponent,
    WorkLegallyComponent,
    WorkStatusComponent,
    HostEntryComponent,
    CandidateVisaTabComponent,
    RegistrationUnhcrComponent,
    RegistrationUnrwaComponent,
    HomeLocationComponent,
    AsylumYearComponent,
    VisaAssessmentComponent,
    VisaCheckAuComponent,
    VisaCheckCaComponent,
    VisaCheckNzComponent,
    VisaCheckUkComponent,
    DestinationLimitComponent,
    FixedInputComponent,
    ConfirmContactComponent,
    ExamsComponent,
    CandidateExamCardComponent,
    HasNameSelectorComponent,
    DestinationJobComponent,
    CrimeComponent,
    ConflictComponent,
    ResidenceStatusComponent,
    WorkAbroadComponent,
    HostEntryLegallyComponent,
    LeftHomeReasonComponent,
    ReturnHomeFutureComponent,
    ResettlementThirdComponent,
    HostChallengesComponent,
    MaritalStatusComponent,
    AutosaveStatusComponent,
    CandidateColumnSelectorComponent,
    AutosaveStatusComponent,
    MaritalStatusComponent,
    CandidateNameNumSearchComponent,
    MilitaryServiceComponent,
    VisaRejectComponent,
    DrivingLicenseComponent,
    DependantsComponent,
    DependantsCardComponent,
    LangAssessmentComponent,
    ExtendDatePipe,
    DatePickerComponent,
    IntProtectionComponent,
    HealthAssessmentComponent,
    CharacterAssessmentComponent,
    SecurityRiskComponent,
    TravelDocumentComponent,
    RiskAssessmentComponent,
    VisaFinalAssessmentComponent,
    JobOccupationComponent,
    SalaryTsmitComponent,
    RegionalAreaComponent,
    JobInterestComponent,
    JobFamilyAusComponent,
    JobEligibilityAssessmentComponent,
    VisaFourNineFourComponent,
    VisaOneEightSixComponent,
    VisaOtherOptionsComponent,
    IeltsLevelComponent,
    QualificationRelevantComponent,
    DatePickerComponent,
    ShowQrCodeComponent,
    HealthIssuesComponent,
    VisaJobPutForwardComponent,
    VisaJobNotesComponent,
    VisaJobCheckAuComponent,
    CandidateStatusSelectorComponent,
    FinalAgreementComponent,
    CreateCandidateLanguageComponent,
    EditCandidateOppComponent,
    IeltsScoreValidationComponent,
    FileSelectorComponent,
    NewJobComponent,
    OldIntakeInputComponent,
    CandidateShareableNotesComponent,
    ShareableDocsComponent,
    CandidateShareableNotesComponent,
    PublishedDocColumnSelectorComponent,
    CandidateSourceDescriptionComponent,
    SearchExternalLinksComponent,
    CreateExternalLinkComponent,
    EditExternalLinkComponent,
    CovidVaccinationComponent,
    EnglishThresholdComponent,
    SafePipe,
    CandidateTaskTabComponent,
    FilterPipe,
    DownloadCvComponent,
    AssignTasksListComponent,
    AssignTasksCandidateComponent,
    EditTaskAssignmentComponent,
    ViewCandidateTasksComponent,
    BrowseTasksComponent,
    ViewTaskDetailsComponent,
    SearchTasksComponent,
    TasksMonitorComponent,
    ViewCandidateMediaWillingnessComponent,
    EditCandidateMediaWillingnessComponent,
    ViewResponseComponent,
    ViewCandidateRegistrationComponent,
    EditCandidateRegistrationComponent,
    EditTaskComponent,
    TasksMonitorListComponent,
    SearchPartnersComponent,
    CreateUpdatePartnerComponent,
    ViewJobComponent,
    JobsComponent,
    JobsWithDetailComponent,
    ViewJobFromUrlComponent,
    JobGeneralTabComponent,
    ViewJobDescriptionComponent,
    ViewJobInfoComponent,
    ViewJobSubmissionListComponent,
    EditJobInfoComponent,
    ViewJobSuggestedListComponent,
    ViewJobSummaryComponent,
    EditJobSummaryComponent,
    JobSuggestedSearchesTabComponent,
    ViewJobSuggestedSearchesComponent,
    InputTextComponent,
    JobUploadTabComponent,
    ViewJobUploadsComponent,
    InputLinkComponent,
    JobSourceContactsTabComponent,
    ViewJobSourceContactsComponent,
    ViewJobPreparationItemsComponent,
    ViewJobSourceContactsComponent,
    AdminApiComponent,
    JobIntakeTabComponent,
    CostCommitEmployerComponent,
    CandidateJobsTabComponent,
    ViewCandidateJobsComponent,
    ViewCandidateOppFromUrlComponent,
    ViewCandidateOppComponent,
    CandidateOppsComponent,
    CandidateOppsWithDetailComponent,
    TruncatePipe,
    CreatedByComponent,
    TailoredCvComponent,
    OpportunityStageNextStepComponent,
    EditOppComponent,
    PathwayAssessmentComponent,
    CandidateVisaJobComponent,
    CvPreviewComponent,
    CandidateVisaJobComponent,
    VisaJobCheckCaComponent,
    RelevantWorkExpComponent,
    AgeRequirementComponent,
    PreferredPathwaysComponent,
    EligiblePathwaysComponent,
    IneligiblePathwaysComponent,
    OccupationCategoryComponent,
    OccupationSubcategoryComponent,
    CreateUpdatePostComponent,
    ExportPdfComponent,
    ChatsComponent,
    ChatsWithPostsComponent,
    ViewChatComponent,
    CreateUpdateChatComponent,
    ViewPostComponent,
    ManageChatsComponent,
    JobSourceContactsWithChatsComponent,
    ViewChatPostsComponent,
    JobGroupChatsTabComponent,
    RelocatingDependantsComponent,
    JobGroupChatsTabComponent,
    SearchHomeComponent,
    JobHomeComponent,
    ListHomeComponent,
    ChatReadStatusComponent,
    ListHomeComponent,
    LanguageThresholdComponent,
    MonitoringEvaluationConsentComponent,
    ResetPasswordComponent,
    UserChangePasswordComponent,
    NclcScoreValidationComponent,
    ArrestImprisonComponent,
    HelpComponent,
    SearchHelpLinksComponent,
    CreateUpdateHelpLinkComponent,
    SfJoblinkComponent,
    SelectJobCopyComponent,
    VisaJobCheckUkComponent,
    PreviewLinkComponent,
    BuildLinkComponent,
    LinkTooltipComponent,
    VisaJobCheckUkComponent,
    CandidatesWithChatComponent,
    ShowCandidatesWithChatComponent,
    ViewCandidateDestinationsComponent,
    EditCandidateDestinationsComponent,
    DestinationFamilyComponent,
    ViewCandidateExamComponent,
    CreateCandidateExamComponent,
    EditCandidateExamComponent,
    JoiDataComponent,
    FindCandidateSourceComponent,
    PotentialDuplicateIconComponent,
    DuplicatesDetailComponent,
    ImportDuolingoCouponsComponent,
    DetScoreValidationComponent,
    DuolingoAssignmentComponent,
    VerifyEmailComponent,
    VerifyEmailToastComponent,
    DuolingoAssignmentComponent,
    PresetEmbedComponent,
    LoadingIndicatorComponent,
    ErrorDisplayComponent,
    IntelligenceComponent,
    OfferToAssistComponent,
    IntlPhoneInputComponent,
    EditMaxEducationLevelComponent,
    ChatMuteToggleButtonComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule,
    ReactiveFormsModule,
    NgbModule,
    FormsModule,
    InfiniteScrollModule,
    NgChartsModule,
    NgxWigModule,
    NgSelectModule,
    DirectiveModule,
    SharedModule,
    DragulaModule.forRoot(),
    QuillModule.forRoot(),
    PickerModule,
    TranslateModule.forRoot({
      defaultLanguage: 'en',
      loader: {
        provide: TranslateLoader,
        useClass: LanguageLoader
      },
    }),
  ],
  providers: [
    {provide: HTTP_INTERCEPTORS, useClass: JwtInterceptor, multi: true},
    {provide: HTTP_INTERCEPTORS, useClass: ErrorInterceptor, multi: true},
    {provide: NgbDateAdapter, useClass: CustomDateAdapter},
    {provide: NgbDateParserFormatter, useClass: CustomDateParserFormatter},
    {
      provide: APP_INITIALIZER,
      useFactory: (envService: EnvService) => () => envService.init(),
      deps: [EnvService],
      multi: true
    },
    {provide: RxStompService},
    AuthorizationService,
    RoleGuardService,
    UnsavedChangesGuard,
    Title,
    DatePipe, TitleCasePipe
  ],
  exports: [
    ViewChatPostsComponent,
    ChatReadStatusComponent,
    CandidateExamCardComponent
  ],
  bootstrap: [AppComponent]
})
export class AppModule {
}
