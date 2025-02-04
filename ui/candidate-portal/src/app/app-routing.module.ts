/*
 * Copyright (c) 2024 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {LandingComponent} from './components/landing/landing.component';
import {HomeComponent} from './components/home/home.component';
import {ResetPasswordComponent} from './components/account/reset-password/reset-password.component';
import {
  ChangePasswordComponent
} from './components/account/change-password/change-password.component';
import {RegisterComponent} from './components/register/register.component';
import {AuthGuard} from './services/auth.guard';
import {EditCandidateComponent} from './components/profile/edit/edit-candidate.component';
import {ViewCandidateComponent} from "./components/profile/view/view-candidate.component";
import { VerifyEmailComponent } from './components/account/verify-email/verify-email.component';

const routes: Routes = [
  {
    path: '',
    component: LandingComponent,
    title: 'Landing Page'
  },
  {
    path: 'reset-password',
    component: ResetPasswordComponent,
    title: 'Reset Password'
  },
  {
    path: 'reset-password/:token',
    component: ChangePasswordComponent,
    title: 'Change Password'
  },
  {
    path: 'login',
    component: LandingComponent,
    title: 'Login'
  },
  {
    path: 'register',
    component: RegisterComponent,
    title: 'Register'
  },
  {
    path: 'home',
    component: HomeComponent,
    canActivate: [AuthGuard],
    title: 'Home'
  },
  {
    path: 'profile',
    component: ViewCandidateComponent,
    canActivate: [AuthGuard],
    title: 'Profile'
  },
  {
    path: 'profile/edit/:section',
    component: EditCandidateComponent,
    canActivate: [AuthGuard],
    title: 'Edit Profile'
  },
  {
    path: 'verify-email',
    component: VerifyEmailComponent,
    title: 'Verify Email'
  },
  /* Keep wildcard redirect at the bottom of the array */
  {
    path: '**',
    redirectTo: '',
    title: 'Redirecting...'
  },
];

@NgModule({
  imports: [RouterModule.forRoot(routes, {})],
  exports: [RouterModule]
})
export class AppRoutingModule { }
