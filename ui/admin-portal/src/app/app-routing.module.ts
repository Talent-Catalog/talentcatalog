import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {SearchCandidatesComponent} from './components/candidates/search/search-candidates.component';
import {ViewCandidateComponent} from './components/candidates/view/view-candidate.component';
import {EditCandidateStatusComponent} from './components/candidates/view/status/edit-candidate-status.component';
import {AuthGuard} from "./services/auth.guard";
import {LoginComponent} from "./components/login/login.component";
import {SearchUsersComponent} from "./components/settings/users/search-users.component";
import {SettingsComponent} from "./components/settings/settings.component";
import {SearchSavedSearchesComponent} from "./components/candidates/search/saved-search/search-saved-searches.component";
import {HomeComponent} from "./components/home/home.component";
import {InfographicComponent} from "./components/infograhics/infographic.component";
import {DefineSearchComponent} from "./components/search/define-search/define-search.component";
import {NotFoundComponent} from "./not-found/not-found.component";

const routes: Routes = [
  {
    path: '',
    canActivate: [AuthGuard],
    children: [
      {
        path: '',
        component: HomeComponent
      },
      {
        path: 'search',
        children: [
          {
            path: '',
            pathMatch: 'full',
            component: DefineSearchComponent
          },
          {
            path: ':savedSearchId',
            component: DefineSearchComponent
          },
        ]
      },
      {
        path: 'candidates',
        children: [
          {
            path: '',
            pathMatch: 'full',
            component: SearchCandidatesComponent
          },
          {
            path: 'search/:savedSearchId',
            component: SearchCandidatesComponent
          },
          {
            path: ':candidateId',
            component: ViewCandidateComponent
          },
          {
            path: ':candidateId/edit',
            component: EditCandidateStatusComponent
          },

        ]
      },
      {
        path: 'settings',
        children: [
          {
            path: '',
            pathMatch: 'full',
            component: SettingsComponent
          },
          {
            path: 'users',
            component: SearchUsersComponent
          }
        ]
      },
      {
        path: 'infographics',
        children: [
          {
            path: '',
            pathMatch: 'full',
            component: InfographicComponent
          }
        ]
      }
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
