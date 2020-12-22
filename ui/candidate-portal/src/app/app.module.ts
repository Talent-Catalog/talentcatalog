import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';
import {AppRoutingModule} from './app-routing.module';
import {AppComponent} from './components/app.component';
import {LandingComponent} from './components/landing/landing.component';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {HTTP_INTERCEPTORS, HttpClientModule} from '@angular/common/http';
import {
  NgbDateAdapter,
  NgbDateParserFormatter,
  NgbDatepickerConfig,
  NgbModule
} from '@ng-bootstrap/ng-bootstrap';
import {TranslateLoader, TranslateModule} from '@ngx-translate/core';
import {RECAPTCHA_V3_SITE_KEY, RecaptchaV3Module} from 'ng-recaptcha';

import {RegistrationLandingComponent} from './components/register/landing/registration-landing.component';
import {RegistrationContactComponent} from './components/register/contact/registration-contact.component';
import {RegistrationPersonalComponent} from './components/register/personal/registration-personal.component';
import {RegistrationCandidateOccupationComponent} from './components/register/candidate-occupation/registration-candidate-occupation.component';
import {RegistrationWorkExperienceComponent} from './components/register/work-experience/registration-work-experience.component';
import {RegistrationEducationComponent} from './components/register/education/registration-education.component';
import {CandidateEducationFormComponent} from './components/common/candidate-education-form/candidate-education-form.component';
import {RegistrationLanguageComponent} from './components/register/language/registration-language.component';
import {RegistrationCertificationsComponent} from './components/register/certifications/registration-certifications.component';
import {RegistrationAdditionalInfoComponent} from './components/register/additional-info/registration-additional-info.component';
import {LocalStorageModule} from 'angular-2-local-storage';
import {JwtInterceptor} from './services/jwt.interceptor';
import {LanguageInterceptor} from './services/language.interceptor';
import {LoginComponent} from './components/account/login/login.component';
import {HomeComponent} from './components/home/home.component';
import {ErrorInterceptor} from './services/error.interceptor';
import {ResetPasswordComponent} from './components/account/reset-password/reset-password.component';
import {ChangePasswordComponent} from './components/account/change-password/change-password.component';
import {HeaderComponent} from './components/header/header.component';
import {RegisterComponent} from './components/register/register.component';
import {RegistrationFooterComponent} from './components/register/registration-footer/registration-footer.component';
import {CandidateJobExperienceFormComponent} from './components/common/candidate-job-experience-form/candidate-job-experience-form.component';
import {CandidateJobExperienceCardComponent} from './components/common/candidate-job-experience-card/candidate-job-experience-card.component';
import {ErrorComponent} from './components/common/error/error.component';
import {LoadingComponent} from './components/common/loading/loading.component';
import {CandidateProfileComponent} from './components/profile/view/candidate-profile.component';
import {FormControlErrorComponent} from './components/common/form-control-error/form-control-error.component';
import {CandidateCertificationCardComponent} from './components/common/candidate-certification-card/candidate-certification-card.component';
import {EditCandidateComponent} from './components/profile/edit/edit-candidate.component';
import {CandidateOccupationCardComponent} from './components/common/candidate-occupation-card/candidate-occupation-card.component';
import {CandidateEducationCardComponent} from './components/common/candidate-education-card/candidate-education-card.component';
import {CandidateLanguageCardComponent} from './components/common/candidate-language-card/candidate-language-card.component';
import {CandidateAttachmentsComponent} from './components/common/candidate-attachments/candidate-attachments.component';
import {FileUploadComponent} from './components/common/file-upload/file-upload.component';
import {InputFilterDirective} from './directives/input-filter.directive';
import {
  CustomDateAdapter,
  CustomDateParserFormatter
} from './util/date-adapter/ngb-date-adapter';
import {UserPipe} from './pipes/user.pipe';
import {TrimPipe} from './pipes/trim.pipe';
import {MonthPickerComponent} from './components/common/month-picker/month-picker.component';
import {
  FaIconLibrary,
  FontAwesomeModule
} from '@fortawesome/angular-fontawesome';
import {faEdit} from '@fortawesome/free-solid-svg-icons';
import {DeleteOccupationComponent} from './components/register/candidate-occupation/delete/delete-occupation.component';
import {CandidateCertificationFormComponent} from './components/common/candidate-certification-form/candidate-certification-form.component';
import {DownloadCvComponent} from './components/common/download-cv/download-cv.component';
import {RedirectGuard} from './services/redirect.guard';
import {LanguageLoader} from "./services/language.loader";

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
    CandidateProfileComponent,
    FormControlErrorComponent,
    CandidateCertificationCardComponent,
    EditCandidateComponent,
    CandidateOccupationCardComponent,
    CandidateEducationCardComponent,
    CandidateLanguageCardComponent,
    CandidateAttachmentsComponent,
    FileUploadComponent,
    InputFilterDirective,
    UserPipe,
    TrimPipe,
    MonthPickerComponent,
    DeleteOccupationComponent,
    CandidateCertificationFormComponent,
    DownloadCvComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    ReactiveFormsModule,
    HttpClientModule,
    FormsModule,
    NgbModule,
    RecaptchaV3Module,
    LocalStorageModule.forRoot({
      prefix: 'tbb-candidate-portal',
      storageType: 'localStorage'
    }),
    TranslateModule.forRoot({
      loader: {provide: TranslateLoader, useClass: LanguageLoader}
    }),
    FontAwesomeModule
  ],
  providers: [
    {provide: RedirectGuard},
    {provide: HTTP_INTERCEPTORS, useClass: JwtInterceptor, multi: true},
    {provide: HTTP_INTERCEPTORS, useClass: LanguageInterceptor, multi: true},
    {provide: HTTP_INTERCEPTORS, useClass: ErrorInterceptor, multi: true},
    {provide: RECAPTCHA_V3_SITE_KEY, useValue: '6Lc_97cZAAAAAIDqR7gT3h_ROGU6P7Jif-wEk9Vu'},
    {provide: NgbDateAdapter, useClass: CustomDateAdapter},
    {provide: NgbDateParserFormatter, useClass: CustomDateParserFormatter},

  ],
  bootstrap: [AppComponent]
})
export class AppModule {

  constructor(private datepickerConfig: NgbDatepickerConfig, library: FaIconLibrary) {
    this.datepickerConfig.minDate = {year: 1950, month: 1, day: 1};
    library.addIcons(
      faEdit
    );
  }
}
