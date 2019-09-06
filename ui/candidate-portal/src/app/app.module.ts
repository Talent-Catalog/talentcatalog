import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';

import {AppRoutingModule} from './app-routing.module';
import {AppComponent} from './components/app.component';
import {LandingComponent} from './components/landing/landing.component';
import {ReactiveFormsModule} from "@angular/forms";
import {HttpClientModule} from "@angular/common/http";
import {NgbModule} from "@ng-bootstrap/ng-bootstrap";
import {RegistrationLandingComponent} from './components/register/landing/registration-landing.component';
import {RegistrationContactComponent} from './components/register/contact/registration-contact.component';
import {RegistrationAlternateContactComponent} from './components/register/contact/alternate/registration-alternate-contact.component';
import {RegistrationPersonalComponent} from './components/register/personal/registration-personal.component';
import {RegistrationLocationComponent} from './components/register/location/registration-location.component';
import {RegistrationNationalityComponent} from './components/register/nationality/registration-nationality.component';
import {RegistrationProfessionComponent} from './components/register/profession/registration-profession.component';
import {RegistrationWorkExperienceComponent} from './components/register/work-experience/registration-work-experience.component';
import {RegistrationEducationComponent} from './components/register/education/registration-education.component';
import {RegistrationMastersComponent} from './components/register/masters/registration-masters.component';
import {RegistrationUniversityComponent} from './components/register/university/registration-university.component';
import {RegistrationSchoolComponent} from './components/register/school/registration-school.component';
import {RegistrationLanguageComponent} from './components/register/language/registration-language.component';
import {RegistrationCertificationsComponent} from './components/register/certifications/registration-certifications.component';
import {RegistrationAdditionalInfoComponent} from './components/register/additional-info/registration-additional-info.component';
import {RegistrationAdditionalContactComponent} from './components/register/contact/additional/registration-additional-contact.component';

@NgModule({
  declarations: [
    AppComponent,
    LandingComponent,
    RegistrationLandingComponent,
    RegistrationContactComponent,
    RegistrationAlternateContactComponent,
    RegistrationPersonalComponent,
    RegistrationLocationComponent,
    RegistrationNationalityComponent,
    RegistrationProfessionComponent,
    RegistrationWorkExperienceComponent,
    RegistrationEducationComponent,
    RegistrationMastersComponent,
    RegistrationUniversityComponent,
    RegistrationSchoolComponent,
    RegistrationLanguageComponent,
    RegistrationCertificationsComponent,
    RegistrationAdditionalInfoComponent,
    RegistrationAdditionalContactComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    ReactiveFormsModule,
    HttpClientModule,
    NgbModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
