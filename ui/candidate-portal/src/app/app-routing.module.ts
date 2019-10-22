import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {LandingComponent} from "./components/landing/landing.component";
import {HomeComponent} from "./components/home/home.component";
import {ResetPasswordComponent} from './components/account/reset-password/reset-password.component';
import {ChangePasswordComponent} from './components/account/change-password/change-password.component';
import {RegisterComponent} from "./components/register/register.component";
import {CandidateProfileComponent} from "./components/profile/candidate-profile.component";

const routes: Routes = [
  {
    path: '',
    component: LandingComponent
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
    path: 'register',
    component: RegisterComponent
  },
  {
    path: 'home',
    component: HomeComponent
  },
  {
    path: 'profile',
    component: CandidateProfileComponent
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
