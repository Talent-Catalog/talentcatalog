import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { CvLandingComponent } from './components/cv-landing/cv-landing.component';
import { CvDisplayComponent } from './components/cv-display/cv-display.component';
import {ErrorInterceptor} from './services/error.interceptor';
import {HTTP_INTERCEPTORS, HttpClientModule} from "@angular/common/http";

@NgModule({
  declarations: [
    AppComponent,
    CvLandingComponent,
    CvDisplayComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule,

  ],
  providers: [
    {provide: HTTP_INTERCEPTORS, useClass: ErrorInterceptor, multi: true}

  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
