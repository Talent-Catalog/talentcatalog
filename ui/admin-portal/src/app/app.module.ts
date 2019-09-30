import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { NgMultiSelectDropDownModule } from 'ng-multiselect-dropdown';


import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './components/app.component';
import { HeaderComponent } from './components/header/header.component';
import { SearchCandidatesComponent } from './components/candidates/search/search-candidates.component';
import { HomeComponent } from './components/home/home.component';
import { HttpClientModule } from '@angular/common/http';
import { CreateCandidateComponent } from './components/candidates/create/create-candidate.component';
import { ReactiveFormsModule } from '@angular/forms';
import { ViewCandidateComponent } from './components/candidates/view/view-candidate.component';
import { EditCandidateComponent } from './components/candidates/edit/edit-candidate.component';
import { DeleteCandidateComponent } from './components/candidates/delete/delete-candidate.component';
import { InfiniteScrollModule } from 'ngx-infinite-scroll';

@NgModule({
  declarations: [
    AppComponent,
    HeaderComponent,
    SearchCandidatesComponent,
    HomeComponent,
    CreateCandidateComponent,
    ViewCandidateComponent,
    EditCandidateComponent,
    DeleteCandidateComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule,
    ReactiveFormsModule,
    NgbModule,
    InfiniteScrollModule,
    NgMultiSelectDropDownModule
  ],
  entryComponents: [
    DeleteCandidateComponent
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule {
}
