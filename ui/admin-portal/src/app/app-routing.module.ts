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
import {ListHomeComponent} from "./components/list/list-home/list-home.component";
import {ResetPasswordComponent} from "./components/account/reset-password/reset-password.component";
import {
  UserChangePasswordComponent
} from "./components/account/user-change-password/user-change-password.component";
import {UnsavedChangesGuard} from "./services/unsaved-changes.guard";
import {IntelligenceComponent} from "./components/intelligence/intelligence.component";
import {DpaGuard} from "./services/dpa.guard";
import {PartnerDpaComponent} from "./components/util/partner-dpa/partner-dpa.component";

const routes: Routes = [
  {
    path: '',
    canActivate: [AuthGuard],
    children: [
      //Redirect old urls that were prefixed by 'candidates'.
      {
        path: 'candidates/:id',
        redirectTo: 'candidate/:id',
        data: { title: 'Redirecting to Candidate' }
      },
      {
        path: 'candidates',
        redirectTo: '',
        data: { title: 'Redirecting to Home' }
      },
      {
        //Default to Jobs
        path: '',
        redirectTo: 'jobs',
        pathMatch: 'full' as const
      },
      {
        path: 'searches',
        pathMatch: 'full' as const,
        component: SearchHomeComponent,
        canActivate: [DpaGuard],
        data: {title: 'TC Searches'}
      },
      {
        path: 'search',
        children: [
          {
            path: '',
            pathMatch: 'full' as const,
            component: CandidatesSearchComponent,
            canActivate: [DpaGuard],
            data: {title: 'TC Search'}
          },
          {
            path: ':id',
            component: CandidatesSearchComponent,
            canDeactivate: [UnsavedChangesGuard],
            canActivate: [DpaGuard],
            data: {title: 'TC Search'},
          },
        ]
      },
      {
        path: 'jobs',
        pathMatch: 'full' as const,
        component: JobHomeComponent,
        canActivate: [DpaGuard],
        data: {title: 'TC Jobs'}
      },
      {
        path: 'job',
        children: [
          {
            path: '',
            pathMatch: 'full' as const,
            component: JobHomeComponent,
            canActivate: [DpaGuard],
            data: {title: 'TC Jobs'}
          },
          {
            path: ':id',
            component: ViewJobFromUrlComponent,
            canActivate: [DpaGuard],
            data: {title: 'TC Job'}
          },
        ]
      },
      {
        path: 'lists',
        pathMatch: 'full' as const,
        component: ListHomeComponent,
        canActivate: [DpaGuard],
        data: {title: 'TC Candidate Lists'}
      },
      {
        path: 'opp',
        children: [
          {
            path: ':id',
            component: ViewCandidateOppFromUrlComponent,
            canActivate: [DpaGuard],
            data: {title: 'TC Opp'}
          },
        ]
      },
      {
        path: 'list',
        children: [
          {
            path: '',
            pathMatch: 'full' as const,
            component: CandidatesListComponent,
            canActivate: [DpaGuard],
            data: {title: 'TC List'}
          },
          {
            path: ':id',
            component: CandidatesListComponent,
            canActivate: [DpaGuard],
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
            canActivate: [DpaGuard],
            data: {title: 'TC Candidate'}
          },
        ]
      },
      {
        path: 'settings',
        canActivate: [RoleGuardService, DpaGuard],
        data: {
          expectedRoles: [Role.systemadmin, Role.admin, Role.partneradmin]
        },
        children: [
          {
            path: '',
            pathMatch: 'full' as const,
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
            pathMatch: 'full' as const,
            component: InfographicComponent,
            canActivate: [DpaGuard],
            data: {title: 'TC Stats'}
          },
          {
            path: ':source/:id',
            component: InfographicComponent,
            canActivate: [DpaGuard],
            data: {title: 'TC Stats'}
          },
        ]
      },
      {
        path: 'intelligence',
        children: [
          {
            path: '',
            pathMatch: 'full' as const,
            component: IntelligenceComponent,
            canActivate: [DpaGuard],
            data: {title: 'TC Intelligence'}
          }
        ]
      },
      {
        path: 'chat',
        children: [
          {
            path: '',
            pathMatch: 'full' as const,
            component: ManageChatsComponent,
            canActivate: [DpaGuard],
            data: {title: 'Chat'}
          },
        ]
      },
      {
        path: 'dpa',
        component: PartnerDpaComponent,
        data: {
          title: 'Data Processing Agreement',
        }
      },
    ]
  },
  {
    path: 'login',
    component: LoginComponent,
    data: {
      hideHeader: true,
      title: 'TC Login'
    }
  },
  {
    path: 'reset-password',
    component: ResetPasswordComponent,
    data: {
      hideHeader: true,
      title: 'TC Reset Password'
    }
  },
  {
    path: 'reset-password/:token',
    component: UserChangePasswordComponent,
    data: {
      hideHeader: true,
      title: 'TC Reset Password'
    }
  },
  {
    path: '**',
    component: NotFoundComponent,
    data: {
      hideHeader: true,
      title: 'Page Not Found'
    }
  }

];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
