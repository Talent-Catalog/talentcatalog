import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';
import {AppRoutingModule} from './app-routing.module';
import {AppComponent} from './components/app.component';
import {LandingComponent} from './components/landing/landing.component';
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {HTTP_INTERCEPTORS, HttpClient, HttpClientModule} from "@angular/common/http";
import {NgbModule} from "@ng-bootstrap/ng-bootstrap";
import {TranslateLoader, TranslateModule} from '@ngx-translate/core';
import {TranslateHttpLoader} from '@ngx-translate/http-loader';

import {RegistrationLandingComponent} from './components/register/landing/registration-landing.component';
import {RegistrationContactComponent} from './components/register/contact/registration-contact.component';
import {RegistrationPersonalComponent} from './components/register/personal/registration-personal.component';
import {RegistrationCandidateOccupationComponent} from './components/register/candidate-occupation/registration-candidate-occupation.component';
import {RegistrationWorkExperienceComponent} from './components/register/work-experience/registration-work-experience.component';
import {RegistrationEducationComponent} from './components/register/education/registration-education.component';
import {CandidateEducationFormComponent} from './components/register/education/candidate-education-form/candidate-education-form.component';
import {RegistrationLanguageComponent} from './components/register/language/registration-language.component';
import {RegistrationCertificationsComponent} from './components/register/certifications/registration-certifications.component';
import {RegistrationAdditionalInfoComponent} from './components/register/additional-info/registration-additional-info.component';
import {LocalStorageModule} from "angular-2-local-storage";
import {JwtInterceptor} from "./services/jwt.interceptor";
import {LanguageInterceptor} from "./services/language.interceptor";
import {LoginComponent} from "./components/account/login/login.component";
import {HomeComponent} from "./components/home/home.component";
import {ErrorInterceptor} from "./services/error.interceptor";
import {ResetPasswordComponent} from './components/account/reset-password/reset-password.component';
import {ChangePasswordComponent} from './components/account/change-password/change-password.component';
import {HeaderComponent} from "./components/header/header.component";
import {RegisterComponent} from './components/register/register.component';
import {RegistrationFooterComponent} from './components/register/registration-footer/registration-footer.component';
import {CandidateJobExperienceFormComponent} from './components/common/candidate-job-experience-form/candidate-job-experience-form.component';
import {CandidateJobExperienceCardComponent} from './components/common/candidate-job-experience-card/candidate-job-experience-card.component';
import {ErrorComponent} from './components/common/error/error.component';
import {LoadingComponent} from './components/common/loading/loading.component';
import {CandidateProfileComponent} from './components/profile/candidate-profile.component';

export function createTranslateLoader(http: HttpClient) {
    return new TranslateHttpLoader(http, './assets/i18n/', '.json');
}

@NgModule({
  declarations: [
    AppComponent,
    HeaderComponent,
    LandingComponent,
    RegistrationLandingComponent,
    RegistrationContactComponent,
    RegistrationPersonalComponent,
    RegistrationCandidateOccupationComponent,
    RegistrationWorkExperienceComponent,
    RegistrationEducationComponent,
    CandidateEducationFormComponent,
    RegistrationLanguageComponent,
    RegistrationCertificationsComponent,
    RegistrationAdditionalInfoComponent,
    LoginComponent,
    HomeComponent,
    ResetPasswordComponent,
    ChangePasswordComponent,
    RegisterComponent,
    RegistrationFooterComponent,
    CandidateJobExperienceFormComponent,
    CandidateJobExperienceCardComponent,
    ErrorComponent,
    LoadingComponent,
    CandidateProfileComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    ReactiveFormsModule,
    HttpClientModule,
    FormsModule,
    NgbModule,
    LocalStorageModule.forRoot({
      prefix: 'tbb-candidate-portal',
      storageType: 'localStorage'
    }),
    TranslateModule.forRoot({
        loader: {
            provide: TranslateLoader,
            useFactory: (createTranslateLoader),
            deps: [HttpClient]
        }
    })
  ],
  providers: [
    {provide: HTTP_INTERCEPTORS, useClass: JwtInterceptor, multi: true},
    {provide: HTTP_INTERCEPTORS, useClass: LanguageInterceptor, multi: true},
    {provide: HTTP_INTERCEPTORS, useClass: ErrorInterceptor, multi: true},
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
