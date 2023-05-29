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
import {InfographicComponent} from "./components/infographics/infographic.component";
import {NotFoundComponent} from "./not-found/not-found.component";
import {
  CandidatesSearchComponent
} from "./components/candidates/candidates-search/candidates-search.component";
import {
  CandidatesListComponent
} from "./components/candidates/candidates-list/candidates-list.component";
import {NewJobComponent} from "./components/job/new-job/new-job.component";
import {Role} from "./model/user";
import {RoleGuardService} from "./services/role-guard.service";
import {
  ViewJobFromUrlComponent
} from "./components/job/view/view-job-from-url/view-job-from-url.component";
import {
  JobsWithDetailComponent
} from "./components/job/jobs-with-detail/jobs-with-detail.component";
import {
  ViewCandidateOppFromUrlComponent
} from "./components/candidate-opp/view/view-candidate-opp-from-url/view-candidate-opp-from-url.component";

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
        data: {title: 'TC Home'}
      },
      {
        path: 'search',
        children: [
          {
            path: '',
            pathMatch: 'full',
            component: CandidatesSearchComponent,
            data: {title: 'TC Search'}
          },
          {
            path: ':id',
            component: CandidatesSearchComponent,
            data: {title: 'TC Search'}
          },
        ]
      },
      {
        path: 'job',
        children: [
          {
            path: '',
            pathMatch: 'full',
            component: NewJobComponent,
            data: {title: 'TC New Job', prepare: false}
          },
          {
            path: 'prepare',
            pathMatch: 'full',
            component: NewJobComponent,
            data: {title: 'TC New Job', prepare: true}
          },
          {
            path: ':id',
            component: ViewJobFromUrlComponent,
            data: {title: 'TC Job'}
          },
        ]
      },
      {
        path: 'opp',
        children: [
          {
            path: ':id',
            component: ViewCandidateOppFromUrlComponent,
            data: {title: 'TC Opp'}
          },
        ]
      },
      {
        path: 'jobs',
        component: JobsWithDetailComponent,
        data: {title: 'TC Jobs'}
      },
      {
        path: 'list',
        children: [
          {
            path: '',
            pathMatch: 'full',
            component: CandidatesListComponent,
            data: {title: 'TC List'}
          },
          {
            path: ':id',
            component: CandidatesListComponent,
            data: {title: 'TC List'}
          },
        ]
      },
      {
        path: 'candidate',
        children: [
          {
            path: ':candidateNumber',
            component: ViewCandidateComponent,
            data: {title: 'TC Candidate'}
          },
        ]
      },
      {
        path: 'settings',
        canActivate: [RoleGuardService],
        data: {
          expectedRoles: [Role.systemadmin, Role.admin, Role.sourcepartneradmin]
        },
        children: [
          {
            path: '',
            pathMatch: 'full',
            component: SettingsComponent,
            data: {title: 'TC Settings'}
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
            data: {title: 'TC Stats'}
          },
          {
            path: ':source/:id',
            component: InfographicComponent,
            data: {title: 'TC Stats'}
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
