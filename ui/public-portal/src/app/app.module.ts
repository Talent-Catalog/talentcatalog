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

import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';
import {AppRoutingModule} from './app-routing.module';
import {AppComponent} from './app.component';
import {CvLandingComponent} from './components/cv-landing/cv-landing.component';
import {CvDisplayComponent} from './components/cv-display/cv-display.component';
import {ErrorInterceptor} from './services/error.interceptor';
import {HTTP_INTERCEPTORS, HttpClientModule} from "@angular/common/http";
import {DatePipe} from "@angular/common";
import {ExtendDatePipe} from "./util/date-pipe";

@NgModule({
  declarations: [
    AppComponent,
    CvLandingComponent,
    CvDisplayComponent,
    ExtendDatePipe
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule,

  ],
  providers: [
    {provide: HTTP_INTERCEPTORS, useClass: ErrorInterceptor, multi: true},
    DatePipe
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
