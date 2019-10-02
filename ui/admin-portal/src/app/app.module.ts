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
import {EditCandidateComponent} from './components/candidates/edit/edit-candidate.component';
import {DeleteCandidateComponent} from './components/candidates/delete/delete-candidate.component';
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
import {CandidateGeneralTabComponent} from './components/candidates/search/candidate-search-card/candidate-general-tab/candidate-general-tab.component';
import {CandidateExperienceTabComponent} from './components/candidates/search/candidate-search-card/candidate-experience-tab/candidate-experience-tab.component';
import {CandidateHistoryTabComponent} from './components/candidates/search/candidate-search-card/candidate-history-tab/candidate-history-tab.component';
import {CandidateEligibilityTabComponent} from './components/candidates/search/candidate-search-card/candidate-eligibility-tab/candidate-eligibility-tab.component';

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
    CandidateEligibilityTabComponent
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
    ConfirmationComponent,
    ConfirmationComponent,
    SaveSearchComponent
  ],
  providers: [
    {provide: HTTP_INTERCEPTORS, useClass: JwtInterceptor, multi: true},
    {provide: HTTP_INTERCEPTORS, useClass: ErrorInterceptor, multi: true},
    AuthService],
  bootstrap: [AppComponent]
})
export class AppModule {
}
