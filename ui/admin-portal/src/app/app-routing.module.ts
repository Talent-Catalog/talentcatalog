import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {SearchCandidatesComponent} from './components/candidates/search/search-candidates.component';
import {HomeComponent} from './components/home/home.component';
import {CreateCandidateComponent} from './components/candidates/create/create-candidate.component';
import {ViewCandidateComponent} from './components/candidates/view/view-candidate.component';
import {EditCandidateComponent} from './components/candidates/edit/edit-candidate.component';
import {AuthGuard} from "./services/auth.guard";
import {LoginComponent} from "./components/login/login.component";
import {SearchUsersComponent} from "./components/settings/users/search-users.component";
import {SettingsComponent} from "./components/settings/settings.component";
import {SearchSavedSearchesComponent} from "./components/candidates/search/saved-search/search-saved-searches.component";

const routes: Routes = [
  {
    path: '',
    canActivate: [AuthGuard],
    children: [
      {
        path: '',
        pathMatch: 'full',
        component: HomeComponent
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
            path: 'create',
            component: CreateCandidateComponent
          },
          {
            path: ':candidateId',
            component: ViewCandidateComponent
          },
          {
            path: ':candidateId/edit',
            component: EditCandidateComponent
          },
          {
            path: ':savedSearchId',
            component: SearchCandidatesComponent
          },
          {
            path: 'saved-search',
            component: SearchSavedSearchesComponent
          },
        ]
      },
      {
        path:  'settings',
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
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
