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
import {ViewCandidateComponent} from './components/candidates/view/view-candidate.component';
import {AuthGuard} from "./services/auth.guard";
import {LoginComponent} from "./components/login/login.component";
import {SettingsComponent} from "./components/settings/settings.component";
import {HomeComponent} from "./components/candidates/home.component";
import {InfographicComponent} from "./components/infograhics/infographic.component";
import {NotFoundComponent} from "./not-found/not-found.component";
import {RoleGuardService} from "./services/role-guard.service";
import {CandidatesSearchComponent} from "./components/candidates/candidates-search/candidates-search.component";
import {CandidatesListComponent} from "./components/candidates/candidates-list/candidates-list.component";
import {NewJobComponent} from "./components/job/new-job/new-job.component";

const routes: Routes = [
  {
    path: '',
    canActivate: [AuthGuard],
    children: [
      //Redirect old urls that were prefixed by 'candidates'.
      {
        path: 'candidates/:id',
        redirectTo: 'candidate/:id',
      },
      {
        path: 'candidates',
        redirectTo: '',
      },
      {
        path: '',
        pathMatch: 'full',
        component: HomeComponent,
        data: {title: 'TBB Home'}
      },
      {
        path: 'search',
        children: [
          {
            path: '',
            pathMatch: 'full',
            component: CandidatesSearchComponent,
            data: {title: 'TBB Search'}
          },
          {
            path: ':id',
            component: CandidatesSearchComponent,
            data: {title: 'TBB Search'}
          },
        ]
      },
      {
        path: 'job',
        component: NewJobComponent,
        data: {title: 'TBB Job'}
      },
      {
        path: 'list',
        children: [
          {
            path: '',
            pathMatch: 'full',
            component: CandidatesListComponent,
            data: {title: 'TBB List'}
          },
          {
            path: ':id',
            component: CandidatesListComponent,
            data: {title: 'TBB List'}
          },
        ]
      },
      {
        path: 'candidate',
        children: [
          {
            path: ':candidateNumber',
            component: ViewCandidateComponent,
            data: {title: 'TBB Candidate'}
          },
        ]
      },
      {
        path: 'settings',
        canActivate: [RoleGuardService],
        data: {
          expectedRoles: ['admin', 'sourcepartneradmin']
        },
        children: [
          {
            path: '',
            pathMatch: 'full',
            component: SettingsComponent,
            data: {title: 'TBB Settings'}
          },
        ]
      },
      {
        path: 'infographics',
        children: [
          {
            path: '',
            pathMatch: 'full',
            component: InfographicComponent,
            data: {title: 'TBB Infographics'}
          },
          {
            path: ':source/:id',
            component: InfographicComponent,
            data: {title: 'TBB Infographics'}
          },
        ]
      },

    ]
  },
  {
    path: 'login',
    component: LoginComponent,
    data: {
      hideHeader: true
    }
  },
  {
    path: '**',
    component: NotFoundComponent,
    data: {
      hideHeader: true
    }
  }

];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
