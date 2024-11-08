/*
 * Copyright (c) 2021 Talent Beyond Boundaries.
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
import {ChangePasswordComponent} from './components/account/change-password/change-password.component';
import {RegisterComponent} from './components/register/register.component';
import {AuthGuard} from './services/auth.guard';
import {EditCandidateComponent} from './components/profile/edit/edit-candidate.component';
import {ViewCandidateComponent} from "./components/profile/view/view-candidate.component";

const routes: Routes = [
  {
    path: '',
    component: LandingComponent,
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
    path: 'login',
    component: LandingComponent
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
    component: ViewCandidateComponent,
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
  imports: [RouterModule.forRoot(routes, {})],
  exports: [RouterModule]
})
export class AppRoutingModule { }
