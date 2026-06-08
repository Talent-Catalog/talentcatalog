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

import {ComponentFixture, TestBed} from '@angular/core/testing';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {LoginComponent} from './login.component';
import {ReactiveFormsModule, UntypedFormBuilder} from '@angular/forms';
import {ActivatedRoute, convertToParamMap, Router} from '@angular/router';
import {AuthenticationService} from "../../services/authentication.service";
import {of} from "rxjs";

import {Directive, Input} from '@angular/core';
import {
  provideMockAuthenticationService
} from "../../util/testing/test-authentication.providers.spec";

@Directive({
  selector: "[routerLink]", // Stub directive for routerLink
})
export class RouterLinkStubDirective {
  @Input('routerLink') linkParams: any;
}

describe('LoginComponent', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;
  let authenticationService: AuthenticationService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        HttpClientTestingModule,
        ReactiveFormsModule
      ],
      declarations: [
        LoginComponent,
        RouterLinkStubDirective // Stub directive for routerLink
      ],
      providers: [
        AuthenticationService,
        UntypedFormBuilder,
        {
          provide: ActivatedRoute, useValue: {
            queryParams: of({}),
            snapshot: {queryParamMap: convertToParamMap({authAction: 'login'})},
          }
        },
        {
          provide: Router, useValue: {
            navigateByUrl: jasmine.createSpy('navigateByUrl'),
            parseUrl: jasmine.createSpy('parseUrl').and.returnValue({queryParams: of({})})
          }
        },
        provideMockAuthenticationService()
      ]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    authenticationService = TestBed.inject(AuthenticationService);
    spyOn(component, 'completeLogin').and.stub();
    fixture.detectChanges(); // Trigger change detection
  });

  it('should create', () => {
    // Assert component creation
    expect(component).toBeTruthy();
  });
});
