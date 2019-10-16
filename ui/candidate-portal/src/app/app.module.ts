import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';
import {AppRoutingModule} from './app-routing.module';
import {AppComponent} from './components/app.component';
import {LandingComponent} from './components/landing/landing.component';
import {ReactiveFormsModule} from "@angular/forms";
import {HTTP_INTERCEPTORS, HttpClient, HttpClientModule} from "@angular/common/http";
import {NgbModule} from "@ng-bootstrap/ng-bootstrap";
import {TranslateLoader, TranslateModule} from '@ngx-translate/core';
import {TranslateHttpLoader} from '@ngx-translate/http-loader';

import {RegistrationLandingComponent} from './components/register/landing/registration-landing.component';
import {RegistrationContactComponent} from './components/register/contact/registration-contact.component';
import {RegistrationAlternateContactComponent} from './components/register/contact/alternate/registration-alternate-contact.component';
import {RegistrationPersonalComponent} from './components/register/personal/registration-personal.component';
import {RegistrationCandidateOccupationComponent} from './components/register/candidate-occupation/registration-candidate-occupation.component';
import {RegistrationJobExperienceComponent} from './components/register/job-experience/registration-job-experience.component';
import {RegistrationEducationComponent} from './components/register/education/registration-education.component';
import {RegistrationMastersComponent} from './components/register/masters/registration-masters.component';
import {RegistrationUniversityComponent} from './components/register/university/registration-university.component';
import {RegistrationSchoolComponent} from './components/register/school/registration-school.component';
import {RegistrationLanguageComponent} from './components/register/language/registration-language.component';
import {RegistrationCertificationsComponent} from './components/register/certifications/registration-certifications.component';
import {RegistrationAdditionalInfoComponent} from './components/register/additional-info/registration-additional-info.component';
import {RegistrationAdditionalContactComponent} from './components/register/contact/additional/registration-additional-contact.component';
import {LocalStorageModule} from "angular-2-local-storage";
import {JwtInterceptor} from "./services/jwt.interceptor";
import {LanguageInterceptor} from "./services/language.interceptor";
import {LoginComponent} from "./components/login/login.component";
import {HomeComponent} from "./components/home/home.component";
import {ErrorInterceptor} from "./services/error.interceptor";
import {ResetPasswordComponent} from './components/reset-password/reset-password.component';
import {ChangePasswordComponent} from './components/change-password/change-password.component';
import {HeaderComponent} from "./components/header/header.component";
import {RegisterComponent} from './components/register/register.component';

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
    RegistrationAlternateContactComponent,
    RegistrationPersonalComponent,
    RegistrationCandidateOccupationComponent,
    RegistrationJobExperienceComponent,
    RegistrationEducationComponent,
    RegistrationMastersComponent,
    RegistrationUniversityComponent,
    RegistrationSchoolComponent,
    RegistrationLanguageComponent,
    RegistrationCertificationsComponent,
    RegistrationAdditionalInfoComponent,
    RegistrationAdditionalContactComponent,
    LoginComponent,
    HomeComponent,
    ResetPasswordComponent,
    ChangePasswordComponent,
    RegisterComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    ReactiveFormsModule,
    HttpClientModule,
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
