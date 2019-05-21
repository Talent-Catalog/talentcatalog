import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { SearchCandidatesComponent } from './components/candidates/search/search-candidates.component';
import { HomeComponent } from './components/home/home.component';
import { CreateCandidateComponent } from './components/candidates/create/create-candidate.component';

const routes: Routes = [
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
      }
    ]
  },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
