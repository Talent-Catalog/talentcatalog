import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {LandingComponent} from "./components/landing/landing.component";
import {RegistrationContactComponent} from "./components/register/contact/registration-contact.component";
import {RegistrationAlternateContactComponent} from "./components/register/contact/alternate/registration-alternate-contact.component";
import {RegistrationPersonalComponent} from "./components/register/personal/registration-personal.component";
import {RegistrationLocationComponent} from "./components/register/location/registration-location.component";
import {RegistrationNationalityComponent} from "./components/register/nationality/registration-nationality.component";
import {RegistrationCandidateOccupationComponent} from "./components/register/candidate-occupation/registration-candidate-occupation.component";
import {RegistrationJobExperienceComponent} from "./components/register/job-experience/registration-job-experience.component";
import {RegistrationEducationComponent} from "./components/register/education/registration-education.component";
import {RegistrationMastersComponent} from "./components/register/masters/registration-masters.component";
import {RegistrationUniversityComponent} from "./components/register/university/registration-university.component";
import {RegistrationSchoolComponent} from "./components/register/school/registration-school.component";
import {RegistrationLanguageComponent} from "./components/register/language/registration-language.component";
import {RegistrationCertificationsComponent} from "./components/register/certifications/registration-certifications.component";
import {RegistrationAdditionalInfoComponent} from "./components/register/additional-info/registration-additional-info.component";
import {RegistrationAdditionalContactComponent} from "./components/register/contact/additional/registration-additional-contact.component";
import {LoginComponent} from "./components/login/login.component";
import {HomeComponent} from "./components/home/home.component";
import {ResetPasswordComponent} from './components/reset-password/reset-password.component';
import {ChangePasswordComponent} from './components/change-password/change-password.component';
import {RegisterComponent} from "./components/register/register.component";

const routes: Routes = [
  {
    path: '',
    component: LandingComponent
  },
  {
    path: 'login',
    component: LoginComponent,
    data: {
      hideHeader: true
    }
  },
  {
    path: 'reset-password',
    component: ResetPasswordComponent,
  },
  {
    path: 'reset-password/:token',
    component: ChangePasswordComponent
  },
  {
    path: 'home',
    component: HomeComponent
  },
  {
    path: 'register',
    component: RegisterComponent
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
    path: 'register/candidateOccupation',
    component: RegistrationCandidateOccupationComponent
  },
  {
    path: 'register/experience',
    component: RegistrationJobExperienceComponent
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
