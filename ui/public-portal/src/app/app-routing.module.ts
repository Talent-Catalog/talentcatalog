import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import {CvLandingComponent} from "./components/cv-landing/cv-landing.component";

const routes: Routes = [
  {
    path: '',
    children: [
      {
        path: 'cv',
        children: [
          {
            path: ':token',
            component: CvLandingComponent,
            data: {title: 'TC CV'}
          }
        ]
      },
    ]
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
