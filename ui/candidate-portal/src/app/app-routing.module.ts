import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {LandingComponent} from './components/landing/landing.component';
import {HomeComponent} from './components/home/home.component';
import {ResetPasswordComponent} from './components/account/reset-password/reset-password.component';
import {ChangePasswordComponent} from './components/account/change-password/change-password.component';
import {RegisterComponent} from './components/register/register.component';
import {CandidateProfileComponent} from './components/profile/view/candidate-profile.component';
import {AuthGuard} from './services/auth.guard';
import {EditCandidateComponent} from './components/profile/edit/edit-candidate.component';
import {RedirectGuard} from './services/redirect.guard';
import {TranslateLoader, TranslateModule} from "@ngx-translate/core";
import {createTranslateLoader} from "./services/language.service";
import {HttpClient} from "@angular/common/http";

const routes: Routes = [
  {
    path: '',
    canActivate: [RedirectGuard],
    component: RedirectGuard,
    data: {
      externalUrl: 'https://www.talentbeyondboundaries.org/talentcatalog/'
    }
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
  imports: [RouterModule.forRoot(routes),
    TranslateModule.forRoot({
      loader: {
        provide: TranslateLoader,
        useFactory: (createTranslateLoader),
        deps: [HttpClient]
      }
    }),
  ],
  exports: [RouterModule]
})
export class AppRoutingModule { }
