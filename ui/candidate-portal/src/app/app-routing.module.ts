import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {LandingComponent} from "./components/landing/landing.component";
import {RegistrationLandingComponent} from "./components/register/landing/registration-landing.component";
import {RegistrationContactComponent} from "./components/register/contact/registration-contact.component";
import {RegistrationAlternateContactComponent} from "./components/register/contact/alternate/registration-alternate-contact.component";
import {RegistrationPersonalComponent} from "./components/register/personal/registration-personal.component";
import {RegistrationLocationComponent} from "./components/register/location/registration-location.component";
import {RegistrationNationalityComponent} from "./components/register/nationality/registration-nationality.component";
import {RegistrationProfessionComponent} from "./components/register/profession/registration-profession.component";
import {RegistrationWorkExperienceComponent} from "./components/register/work-experience/registration-work-experience.component";
import {RegistrationEducationComponent} from "./components/register/education/registration-education.component";
import {RegistrationMastersComponent} from "./components/register/masters/registration-masters.component";
import {RegistrationUniversityComponent} from "./components/register/university/registration-university.component";
import {RegistrationSchoolComponent} from "./components/register/school/registration-school.component";
import {RegistrationLanguageComponent} from "./components/register/language/registration-language.component";
import {RegistrationCertificationsComponent} from "./components/register/certifications/registration-certifications.component";
import {RegistrationAdditionalInfoComponent} from "./components/register/additional-info/registration-additional-info.component";
import {RegistrationAdditionalContactComponent} from "./components/register/contact/additional/registration-additional-contact.component";

const routes: Routes = [
  {
    path: '',
    component: LandingComponent
  },
  {
    path: 'register',
    component: RegistrationLandingComponent
  },
  {
    path: 'register/contact',
    component: RegistrationContactComponent
  },
  {
    path: 'register/contact/alternate',
    component: RegistrationAlternateContactComponent
  },
  {
    path: 'register/contact/additional',
    component: RegistrationAdditionalContactComponent
  },
  {
    path: 'register/personal',
    component: RegistrationPersonalComponent
  },
  {
    path: 'register/location',
    component: RegistrationLocationComponent
  },
  {
    path: 'register/nationality',
    component: RegistrationNationalityComponent
  },
  {
    path: 'register/profession',
    component: RegistrationProfessionComponent
  },
  {
    path: 'register/experience',
    component: RegistrationWorkExperienceComponent
  },
  {
    path: 'register/education',
    component: RegistrationEducationComponent
  },
  {
    path: 'register/education/masters',
    component: RegistrationMastersComponent
  },
  {
    path: 'register/education/university',
    component: RegistrationUniversityComponent
  },
  {
    path: 'register/education/school',
    component: RegistrationSchoolComponent
  },
  {
    path: 'register/language',
    component: RegistrationLanguageComponent
  },
  {
    path: 'register/certifications',
    component: RegistrationCertificationsComponent
  },
  {
    path: 'register/additional-information',
    component: RegistrationAdditionalInfoComponent
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
