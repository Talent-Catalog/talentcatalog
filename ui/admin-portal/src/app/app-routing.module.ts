import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { SearchCandidatesComponent } from './components/candidates/search/search-candidates.component';
import { HomeComponent } from './components/home/home.component';

const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    component: HomeComponent
  },
  {
    path: 'candidates',
    component: SearchCandidatesComponent
  },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
