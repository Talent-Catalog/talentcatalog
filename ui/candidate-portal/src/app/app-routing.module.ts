import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {LandingComponent} from "./components/landing/landing.component";
import {HomeComponent} from "./components/home/home.component";
import {ResetPasswordComponent} from './components/account/reset-password/reset-password.component';
import {ChangePasswordComponent} from './components/account/change-password/change-password.component';
import {RegisterComponent} from "./components/register/register.component";
import {CandidateProfileComponent} from "./components/profile/view/candidate-profile.component";
import {AuthGuard} from "./services/auth.guard";
import {EditCandidateComponent} from "./components/profile/edit/edit-candidate.component";

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
    component: HomeComponent,
    canActivate: [AuthGuard]
  },
  {
    path: 'profile',
    component: CandidateProfileComponent,
    canActivate: [AuthGuard]
  },
  {
    path: 'profile/edit/:section',
    component: EditCandidateComponent,
    canActivate: [AuthGuard]
  },
  /* Keep wildcard redirect at the bottom of the array */
  {
    path: '**',
    redirectTo: ''
  },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
