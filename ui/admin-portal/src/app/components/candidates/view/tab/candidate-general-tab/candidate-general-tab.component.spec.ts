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
import {CandidateGeneralTabComponent} from "./candidate-general-tab.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {MockCandidate} from "../../../../../MockData/MockCandidate";
import {ViewCandidateLanguageComponent} from "../../language/view-candidate-language.component";
import {
  ViewCandidateRegistrationComponent
} from "../../registration/view-candidate-registration.component";
import {ViewCandidateContactComponent} from "../../contact/view-candidate-contact.component";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";
import {ViewCandidateAccountComponent} from "../../account/view-candidate-account.component";
import {CUSTOM_ELEMENTS_SCHEMA} from "@angular/core";
import {RouterTestingModule} from "@angular/router/testing";

describe('CandidateGeneralTabComponent', () => {
  let component: CandidateGeneralTabComponent;
  let fixture: ComponentFixture<CandidateGeneralTabComponent>;
  const mockCandidate = new MockCandidate();
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HttpClientTestingModule,FormsModule,ReactiveFormsModule, NgSelectModule,RouterTestingModule],
      declarations: [ CandidateGeneralTabComponent,ViewCandidateLanguageComponent,ViewCandidateAccountComponent,ViewCandidateRegistrationComponent,ViewCandidateContactComponent ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CandidateGeneralTabComponent);
    component = fixture.componentInstance;
    component.candidate=mockCandidate;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize with null error', () => {
    expect(component.error).toBeNull();
  });

  it('should not display additional functionalities for non-admin users', () => {
    component.adminUser = false;
    fixture.detectChanges();

    const additionalFunctionalityElement = fixture.nativeElement.querySelector('app-view-candidate-account');
    expect(additionalFunctionalityElement).toBeNull();
  });

  it('should display additional functionalities for admin users', () => {
    component.adminUser = true;
    fixture.detectChanges();

    const additionalFunctionalityElement = fixture.nativeElement.querySelector('app-view-candidate-account');
    expect(additionalFunctionalityElement).not.toBeNull();
  });

  it('should not change loading state when candidate data changes', () => {
    const candidate = mockCandidate;

    component.loading = false;
    component.ngOnChanges({
      candidate:
        {
          previousValue: null,
          currentValue: candidate,
          firstChange: true,
          isFirstChange: () => true
        }
    });
    expect(component.loading).toBe(false);
  });

  it('should update result with candidate data when data changes', () => {
    const candidate = mockCandidate;

    component.ngOnChanges({
      candidate:
        {
          previousValue: null,
          currentValue: candidate,
          firstChange: true,
          isFirstChange: () => true
        }
    });

    expect(component.result).toEqual(candidate);
  });
});
