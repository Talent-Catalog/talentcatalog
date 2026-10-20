/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

// This file is required by karma.conf.js and loads recursively all the .spec and framework files

import 'zone.js/testing';
import {NgModule, NO_ERRORS_SCHEMA, provideZoneChangeDetection} from '@angular/core';
import {getTestBed, TestBed} from '@angular/core/testing';
import {
  BrowserDynamicTestingModule,
  platformBrowserDynamicTesting
} from '@angular/platform-browser-dynamic/testing';
import {provideHttpClientTesting} from '@angular/common/http/testing';
import {provideHttpClient, withInterceptorsFromDi, withXhr} from '@angular/common/http';
import {RouterTestingModule} from '@angular/router/testing';
import {TranslateModule} from '@ngx-translate/core';

// TestBed defaults to zoneless change detection from Angular 21. The app still uses zone-based
// change detection (see main.ts), so tests do too.
@NgModule({providers: [provideZoneChangeDetection()]})
class ZoneChangeDetectionTestingModule {}

// First, initialize the Angular testing environment.
getTestBed().initTestEnvironment(
  [BrowserDynamicTestingModule, ZoneChangeDetectionTestingModule],
  platformBrowserDynamicTesting(), {
    teardown: { destroyAfterEach: false }
}
);

beforeEach(() => {
  TestBed.configureTestingModule({
    imports: [RouterTestingModule, TranslateModule.forRoot()],
    providers: [provideHttpClient(withXhr(), withInterceptorsFromDi()), provideHttpClientTesting()],
    schemas: [NO_ERRORS_SCHEMA]
  });
});
