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
  ViewCandidateOppFromUrlComponent
} from "./components/candidate-opp/view-candidate-opp-from-url/view-candidate-opp-from-url.component";
import {ManageChatsComponent} from "./components/chat/manage-chats/manage-chats.component";
import {SearchHomeComponent} from "./components/search/search-home/search-home.component";
import {JobHomeComponent} from "./components/job/job-home/job-home.component";
import {
  CandidatesHomeComponent
} from "./components/candidates/candidates-home/candidates-home.component";

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
        //Redirect old home to search-home
        path: '',
        redirectTo: 'searches',
        pathMatch: 'full'
      },
      {
        path: 'searches',
        pathMatch: 'full',
        component: SearchHomeComponent,
        data: {title: 'TC Searches'}
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
        path: 'jobs',
        pathMatch: 'full',
        component: JobHomeComponent,
        data: {title: 'TC Jobs'}
      },
      {
        path: 'job',
        children: [
          {
            path: '',
            pathMatch: 'full',
            component: NewJobComponent,
            data: {title: 'TC New Job'}
          },
          {
            path: ':id',
            component: ViewJobFromUrlComponent,
            data: {title: 'TC Job'}
          },
        ]
      },
      {
        path: 'lists',
        pathMatch: 'full',
        component: CandidatesHomeComponent,
        data: {title: 'TC Candidate Lists'}
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
          expectedRoles: [Role.systemadmin, Role.admin, Role.partneradmin]
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
      {
        path: 'chat',
        children: [
          {
            path: '',
            pathMatch: 'full',
            component: ManageChatsComponent,
            data: {title: 'Chat'}
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
