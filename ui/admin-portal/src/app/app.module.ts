import {BrowserModule, Title} from '@angular/platform-browser';
import {NgModule} from '@angular/core';
import {NgbModule} from '@ng-bootstrap/ng-bootstrap';
import {NgMultiSelectDropDownModule} from 'ng-multiselect-dropdown';


import {AppRoutingModule} from './app-routing.module';
import {AppComponent} from './components/app.component';
import {HeaderComponent} from './components/header/header.component';
import {ShowCandidatesComponent} from './components/candidates/search/show-candidates.component';
import {HomeComponent} from './components/candidates/home.component';
import {HTTP_INTERCEPTORS, HttpClientModule} from '@angular/common/http';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {ViewCandidateComponent} from './components/candidates/view/view-candidate.component';
import {EditCandidateStatusComponent} from './components/candidates/view/status/edit-candidate-status.component';
import {DeleteCandidateComponent} from './components/candidates/view/delete/delete-candidate.component';
import {InfiniteScrollModule} from 'ngx-infinite-scroll';
import {JwtInterceptor} from "./services/jwt.interceptor";
import {ErrorInterceptor} from "./services/error.interceptor";
import {AuthService} from "./services/auth.service";
import {LocalStorageModule} from "angular-2-local-storage";
import {LoginComponent} from "./components/login/login.component";
import {SettingsComponent} from "./components/settings/settings.component";
import {SearchUsersComponent} from "./components/settings/users/search-users.component";
import {SearchNationalitiesComponent} from "./components/settings/nationalities/search-nationalities.component";
import {CreateNationalityComponent} from "./components/settings/nationalities/create/create-nationality.component";
import {EditNationalityComponent} from "./components/settings/nationalities/edit/edit-nationality.component";
import {ConfirmationComponent} from "./components/util/confirm/confirmation.component";
import {SearchCountriesComponent} from "./components/settings/countries/search-countries.component";
import {CreateCountryComponent} from "./components/settings/countries/create/create-country.component";
import {EditCountryComponent} from "./components/settings/countries/edit/edit-country.component";
import {SearchLanguagesComponent} from './components/settings/languages/search-languages.component';
import {CreateLanguageComponent} from './components/settings/languages/create/create-language.component';
import {EditLanguageComponent} from './components/settings/languages/edit/edit-language.component';
import {SearchSavedSearchesComponent} from "./components/search/load-search/search-saved-searches.component";
import {CreateSearchComponent} from "./components/search/create/create-search.component";
import {CandidateSearchCardComponent} from './components/util/candidate-search-card/candidate-search-card.component';
import {CandidateGeneralTabComponent} from './components/candidates/view/tab/candidate-general-tab/candidate-general-tab.component';
import {CandidateExperienceTabComponent} from './components/candidates/view/tab/candidate-experience-tab/candidate-experience-tab.component';
import {CandidateHistoryTabComponent} from './components/candidates/view/tab/candidate-history-tab/candidate-history-tab.component';
import {CandidateEligibilityTabComponent} from './components/candidates/view/tab/candidate-eligibility-tab/candidate-eligibility-tab.component';
import {SearchOccupationsComponent} from './components/settings/occupations/search-occupations.component';
import {CreateOccupationComponent} from './components/settings/occupations/create/create-occupation.component';
import {EditOccupationComponent} from './components/settings/occupations/edit/edit-occupation.component';
import {SearchIndustriesComponent} from './components/settings/industries/search-industries.component';
import {CreateIndustryComponent} from './components/settings/industries/create/create-industry.component';
import {EditIndustryComponent} from './components/settings/industries/edit/edit-industry.component';
import {SearchLanguageLevelsComponent} from './components/settings/language-levels/search-language-levels.component';
import {CreateLanguageLevelComponent} from './components/settings/language-levels/create/create-language-level.component';
import {EditLanguageLevelComponent} from './components/settings/language-levels/edit/edit-language-level.component';
import {SearchEducationLevelsComponent} from './components/settings/education-levels/search-education-levels.component';
import {CreateEducationLevelComponent} from './components/settings/education-levels/create/create-education-level.component';
import {EditEducationLevelComponent} from './components/settings/education-levels/edit/edit-education-level.component';
import {SearchEducationMajorsComponent} from './components/settings/education-majors/search-education-majors.component';
import {CreateEducationMajorComponent} from './components/settings/education-majors/create/create-education-major.component';
import {EditEducationMajorComponent} from './components/settings/education-majors/edit/edit-education-major.component';
import {DropdownTranslationsComponent} from './components/settings/translations/dropdowns/dropdown-translations.component';

import {ViewCandidateContactComponent} from "./components/candidates/view/contact/view-candidate-contact.component";
import {ViewCandidateLanguageComponent} from "./components/candidates/view/language/view-candidate-language.component";
import {EditCandidateContactComponent} from "./components/candidates/view/contact/edit/edit-candidate-contact.component";

import {ViewCandidateNoteComponent} from "./components/candidates/view/note/view-candidate-note.component";
import {CreateCandidateNoteComponent} from "./components/candidates/view/note/create/create-candidate-note.component";
import {EditCandidateNoteComponent} from "./components/candidates/view/note/edit/edit-candidate-note.component";

import {ViewCandidateEducationComponent} from "./components/candidates/view/education/view-candidate-education.component";
import {CreateCandidateEducationComponent} from "./components/candidates/view/education/create/create-candidate-education.component";
import {EditCandidateEducationComponent} from "./components/candidates/view/education/edit/edit-candidate-education.component";

import {ViewCandidateCertificationComponent} from "./components/candidates/view/certification/view-candidate-certification.component";
import {CreateCandidateCertificationComponent} from "./components/candidates/view/certification/create/create-candidate-certification.component";
import {EditCandidateCertificationComponent} from "./components/candidates/view/certification/edit/edit-candidate-certification.component";
import {ViewCandidateOccupationComponent} from './components/candidates/view/occupation/view-candidate-occupation.component';
import {ViewCandidateJobExperienceComponent} from './components/candidates/view/occupation/experience/view-candidate-job-experience.component';
import {EditUserComponent} from './components/settings/users/edit/edit-user.component';
import {CreateUserComponent} from './components/settings/users/create/create-user.component';
import {CandidateEducationTabComponent} from "./components/candidates/view/tab/candidate-education-tab/candidate-education-tab.component";
import {JoinSavedSearchComponent} from "./components/search/join-search/join-saved-search.component";
import {SavedSearchComponent} from "./components/util/saved-search/saved-search.component";
import {LanguageLevelFormControlComponent} from './components/util/form/language-proficiency/language-level-form-control.component';
import {CandidatePipe} from './pipes/candidate.pipe';
import {EditCandidateShortlistItemComponent} from "./components/util/candidate-review/edit/edit-candidate-shortlist-item.component";
import {CandidateShortlistItemComponent} from "./components/util/candidate-review/candidate-shortlist-item.component";
import {UserPipe} from "./components/util/user/user.pipe";
import {UpdatedByComponent} from "./components/util/user/updated-by/updated-by.component";
import {DateRangePickerComponent} from './components/util/form/date-range-picker/date-range-picker.component';
import {EditCandidateJobExperienceComponent} from './components/candidates/view/occupation/experience/edit/edit-candidate-job-experience.component';
import {CreateCandidateJobExperienceComponent} from './components/candidates/view/occupation/experience/create/create-candidate-job-experience.component';
import {ViewCandidateAttachmentComponent} from "./components/candidates/view/attachment/view-candidate-attachment.component";
import {EditCandidateOccupationComponent} from "./components/candidates/view/occupation/edit/edit-candidate-occupation.component";
import {SortedByComponent} from "./components/util/sort/sorted-by.component";
import {EditCandidateLanguageComponent} from './components/candidates/view/language/edit/edit-candidate-language.component';
import {ViewCandidateAccountComponent} from './components/candidates/view/account/view-candidate-account.component';
import {ChangePasswordComponent} from './components/account/change-password/change-password.component';
import {ChangeUsernameComponent} from './components/account/change-username/change-username.component';
import {CreateCandidateAttachmentComponent} from './components/candidates/view/attachment/create/create-candidate-attachment.component';
import {EditCandidateAttachmentComponent} from './components/candidates/view/attachment/edit/edit-candidate-attachment.component';
import {FileUploadComponent} from "./components/util/file-upload/file-upload.component";
import {CandidateAdditionalInfoTabComponent} from './components/candidates/view/tab/candidate-additional-info-tab/candidate-additional-info-tab.component';
import {ViewCandidateAdditionalInfoComponent} from './components/candidates/view/additional-info/view-candidate-additional-info.component';
import {ViewCandidateSkillComponent} from "./components/candidates/view/skill/view-candidate-skill.component";
import {BrowseCandidateSourcesComponent} from './components/candidates/search/browse/browse-candidate-sources.component';
import {ChartsModule} from "ng2-charts";
import {InfographicComponent} from './components/infograhics/infographic.component';
import {ChartComponent} from './components/infograhics/chart/chart.component';
import {MonthPickerComponent} from "./components/util/month-picker/month-picker.component";
import {UpdateSearchComponent} from "./components/search/update/update-search.component";
import {CandidateSourceResultsComponent} from './components/candidates/search/returns/candidate-source-results.component';
import {DefineSearchComponent} from './components/search/define-search/define-search.component';
import {NotFoundComponent} from "./not-found/not-found.component";
import {GeneralTranslationsComponent} from './components/settings/translations/general/general-translations.component';
import {ViewCandidateSpecialLinksComponent} from './components/candidates/view/special-links/view-candidate-special-links.component';
import {EditCandidateSpecialLinksComponent} from './components/candidates/view/special-links/edit/edit-candidate-special-links.component';
import {RoleGuardService} from "./services/role-guard.service";
import {NgxWigModule} from 'ngx-wig';
import {ViewCandidateSurveyComponent} from './components/candidates/view/survey/view-candidate-survey.component';
import {EditCandidateAdditionalInfoComponent} from './components/candidates/view/additional-info/edit/edit-candidate-additional-info.component';
import {EditCandidateSurveyComponent} from './components/candidates/view/survey/edit/edit-candidate-survey.component';
import {CreateListComponent} from './components/list/create/create-list.component';
import {UpdateListComponent} from './components/list/update/update-list.component';
import {SelectListComponent} from './components/list/select/select-list.component';
import {CandidatesSearchComponent} from './components/candidates/candidates-search/candidates-search.component';
import {CandidatesListComponent} from './components/candidates/candidates-list/candidates-list.component';

@NgModule({
  declarations: [
    AppComponent,
    LoginComponent,
    HeaderComponent,
    ConfirmationComponent,
    ShowCandidatesComponent,
    HomeComponent,
    ViewCandidateComponent,
    EditCandidateStatusComponent,
    DeleteCandidateComponent,
    SettingsComponent,
    SearchUsersComponent,
    SearchNationalitiesComponent,
    EditNationalityComponent,
    CreateNationalityComponent,
    SearchCountriesComponent,
    CreateCountryComponent,
    EditCountryComponent,
    SearchLanguagesComponent,
    CreateLanguageComponent,
    EditLanguageComponent,
    CreateNationalityComponent,
    SearchSavedSearchesComponent,
    CreateSearchComponent,
    UpdateSearchComponent,
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
    EditUserComponent,
    CreateUserComponent,
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
    CandidateShortlistItemComponent,
    EditCandidateShortlistItemComponent,
    DateRangePickerComponent,
    DropdownTranslationsComponent,
    LanguageLevelFormControlComponent,
    DateRangePickerComponent,
    SavedSearchComponent,
    UserPipe,
    UpdatedByComponent,
    SavedSearchComponent,
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
    ChangeUsernameComponent,
    CreateCandidateAttachmentComponent,
    EditCandidateAttachmentComponent,
    FileUploadComponent,
    CandidateAdditionalInfoTabComponent,
    ViewCandidateAdditionalInfoComponent,
    ViewCandidateSkillComponent,
    FileUploadComponent,
    BrowseCandidateSourcesComponent,
    InfographicComponent,
    ChartComponent,
    MonthPickerComponent,
    NotFoundComponent,
    GeneralTranslationsComponent,
    CandidateSourceResultsComponent,
    DefineSearchComponent,
    MonthPickerComponent,
    NotFoundComponent,
    ViewCandidateSpecialLinksComponent,
    EditCandidateSpecialLinksComponent,
    ViewCandidateSurveyComponent,
    EditCandidateAdditionalInfoComponent,
    EditCandidateSurveyComponent,
    CreateListComponent,
    UpdateListComponent,
    SelectListComponent,
    CandidatesSearchComponent,
    CandidatesListComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule,
    ReactiveFormsModule,
    NgbModule,
    FormsModule,
    InfiniteScrollModule,
    NgMultiSelectDropDownModule.forRoot(),
    ChartsModule,
    NgxWigModule,
    LocalStorageModule.forRoot({
      prefix: 'tbb-admin',
      storageType: 'localStorage'
    })
  ],
  entryComponents: [
    DeleteCandidateComponent,
    SearchNationalitiesComponent,
    CreateNationalityComponent,
    EditNationalityComponent,
    CreateCountryComponent,
    EditCountryComponent,
    SearchCountriesComponent,
    SearchLanguagesComponent,
    CreateLanguageComponent,
    EditLanguageComponent,
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
    EditUserComponent,
    CreateUserComponent,
    ConfirmationComponent,
    CreateSearchComponent,
    UpdateSearchComponent,
    EditCandidateContactComponent,
    JoinSavedSearchComponent,
    CreateCandidateNoteComponent,
    EditCandidateNoteComponent,
    CreateCandidateEducationComponent,
    EditCandidateEducationComponent,
    CreateCandidateCertificationComponent,
    EditCandidateCertificationComponent,
    DropdownTranslationsComponent,
    EditCandidateCertificationComponent,
    EditCandidateShortlistItemComponent,
    CreateCandidateJobExperienceComponent,
    EditCandidateJobExperienceComponent,
    EditCandidateOccupationComponent,
    EditCandidateLanguageComponent,
    ChangePasswordComponent,
    ChangeUsernameComponent,
    CreateCandidateAttachmentComponent,
    EditCandidateAttachmentComponent,
    CreateCandidateAttachmentComponent,
    SearchSavedSearchesComponent,
    EditCandidateSpecialLinksComponent,
    EditCandidateStatusComponent,
    EditCandidateAdditionalInfoComponent,
    EditCandidateSurveyComponent,
    CreateListComponent,
    SelectListComponent,
    UpdateListComponent
  ],
  providers: [
    {provide: HTTP_INTERCEPTORS, useClass: JwtInterceptor, multi: true},
    {provide: HTTP_INTERCEPTORS, useClass: ErrorInterceptor, multi: true},
    AuthService,
    RoleGuardService,
    Title],
  bootstrap: [AppComponent]
})
export class AppModule {
}
