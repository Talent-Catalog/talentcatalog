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

const routes: Routes = [
  {
    path: '',
    canActivate: [AuthGuard],
    children: [
      //Redirect old urls that were prefixed by 'candidates'.
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
            data: {title: 'TBB Admin - Candidate'}
          },
        ]
      },
      {
        path: 'settings',
        canActivate: [RoleGuardService],
        data: {
          expectedRole: 'admin'
        },
        children: [
          {
            path: '',
            pathMatch: 'full',
            component: SettingsComponent,
            data: {title: 'TBB Admin - Settings'}
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
            data: {title: 'TBB Admin - Infographics'}
          }
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
