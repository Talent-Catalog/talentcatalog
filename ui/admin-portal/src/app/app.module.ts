import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';
import {NgbModule} from '@ng-bootstrap/ng-bootstrap';
import {NgMultiSelectDropDownModule} from 'ng-multiselect-dropdown';


import {AppRoutingModule} from './app-routing.module';
import {AppComponent} from './components/app.component';
import {HeaderComponent} from './components/header/header.component';
import {SearchCandidatesComponent} from './components/candidates/search/search-candidates.component';
import {HomeComponent} from './components/home/home.component';
import {HTTP_INTERCEPTORS, HttpClientModule} from '@angular/common/http';
import {CreateCandidateComponent} from './components/candidates/create/create-candidate.component';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {ViewCandidateComponent} from './components/candidates/view/view-candidate.component';
import {EditCandidateComponent} from './components/candidates/view/edit/edit-candidate.component';
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
import {SearchSavedSearchesComponent} from "./components/candidates/search/saved/search-saved-searches.component";
import {SaveSearchComponent} from "./components/candidates/search/save/save-search.component";
import {CandidateSearchCardComponent} from './components/candidates/search/candidate-search-card/candidate-search-card.component';
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
import {ViewCandidateContactComponent} from "./components/candidates/view/contact/view-candidate-contact.component";
import {ViewCandidateLanguageComponent} from "./components/candidates/view/language/view-candidate-language.component";
import {EditCandidateContactComponent} from "./components/candidates/view/contact/edit/edit-candidate-contact.component";
import { EditUserComponent } from './components/settings/users/edit/edit-user.component';
import { CreateUserComponent } from './components/settings/users/create/create-user.component';
import {CandidateEducationTabComponent} from "./components/candidates/view/tab/candidate-education-tab/candidate-education-tab.component";
import { ViewCandidateEducationComponent } from './components/candidates/view/education/view-candidate-education.component';
import { ViewCandidateCertificationComponent } from './components/candidates/view/certification/view-candidate-certification.component';
import { EditCandidateEducationComponent } from './components/candidates/view/education/edit/edit-candidate-education.component';
import { CreateCandidateEducationComponent } from './components/candidates/view/education/create/create-candidate-education.component';
import { CreateCandidateCertificationComponent } from './components/candidates/view/certification/create/create-candidate-certification.component';
import { EditCandidateCertificationComponent } from './components/candidates/view/certification/edit/edit-candidate-certification.component';
import { ViewCandidateNoteComponent } from './components/candidates/view/note/view-candidate-note.component';
import { CreateCandidateNoteComponent } from './components/candidates/view/note/create/create-candidate-note.component';
import { EditCandidateNoteComponent } from './components/candidates/view/note/edit/edit-candidate-note.component';

@NgModule({
  declarations: [
    AppComponent,
    LoginComponent,
    HeaderComponent,
    ConfirmationComponent,
    SearchCandidatesComponent,
    HomeComponent,
    CreateCandidateComponent,
    ViewCandidateComponent,
    EditCandidateComponent,
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
    SaveSearchComponent,
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
    ViewCandidateEducationComponent,
    ViewCandidateCertificationComponent,
    EditCandidateEducationComponent,
    CreateCandidateEducationComponent,
    CreateCandidateCertificationComponent,
    EditCandidateCertificationComponent,
    ViewCandidateNoteComponent,
    CreateCandidateNoteComponent,
    EditCandidateNoteComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule,
    ReactiveFormsModule,
    NgbModule,
    InfiniteScrollModule,
    NgMultiSelectDropDownModule.forRoot(),
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
    SaveSearchComponent,
    EditCandidateContactComponent,
    EditCandidateEducationComponent,
    CreateCandidateEducationComponent,
    CreateCandidateCertificationComponent,
    EditCandidateCertificationComponent
  ],
  providers: [
    {provide: HTTP_INTERCEPTORS, useClass: JwtInterceptor, multi: true},
    {provide: HTTP_INTERCEPTORS, useClass: ErrorInterceptor, multi: true},
    AuthService],
  bootstrap: [AppComponent]
})
export class AppModule {
}
