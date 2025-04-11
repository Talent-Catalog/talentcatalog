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

import {AuthenticationService} from "../../../../../services/authentication.service";
import {JobService} from "../../../../../services/job.service";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {JobIntakeTabComponent} from "./job-intake-tab.component";
import {MockJob} from "../../../../../MockData/MockJob";
import {NgbAccordionModule, NgbModule} from "@ng-bootstrap/ng-bootstrap";
import {
  CostCommitEmployerComponent
} from "../../../intake/cost-commit-employer/cost-commit-employer.component";
import {UntypedFormBuilder, FormsModule, ReactiveFormsModule} from "@angular/forms";
import {NgxWigModule} from "ngx-wig";
import {MockPartner} from "../../../../../MockData/MockPartner";
import {AutosaveStatusComponent} from "../../../../util/autosave-status/autosave-status.component";
import {JobOppIntake} from "../../../../../model/job-opp-intake";
import {MockJobOppIntake} from "../../../../../MockData/MockJobOppIntake";
import {HttpClientModule} from "@angular/common/http";
import {TranslateModule} from "@ngx-translate/core";
import {CUSTOM_ELEMENTS_SCHEMA} from "@angular/core";

describe('JobIntakeTabComponent', () => {
  let component: JobIntakeTabComponent;
  let fixture: ComponentFixture<JobIntakeTabComponent>;
  let authServiceSpy: jasmine.SpyObj<AuthenticationService>;
  let jobServiceSpy: jasmine.SpyObj<JobService>;
  beforeEach(async () => {
    const authServiceSpyObj = jasmine.createSpyObj('AuthenticationService', ['getLoggedInUser']);
    const jobServiceObj = jasmine.createSpyObj('JobService', ['updateIntakeData', 'get']);
    await TestBed.configureTestingModule({
      declarations: [ JobIntakeTabComponent,CostCommitEmployerComponent,AutosaveStatusComponent],
      imports:[HttpClientModule,NgbAccordionModule,NgbModule,NgxWigModule,FormsModule,
        ReactiveFormsModule,TranslateModule.forRoot()],
      providers: [
        { provide: AuthenticationService, useValue: authServiceSpyObj },
        { provide: JobService, useValue: jobServiceObj  },
        UntypedFormBuilder
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA]
    })
    .compileComponents();

    authServiceSpy = TestBed.inject(AuthenticationService) as jasmine.SpyObj<AuthenticationService>;
    jobServiceSpy = TestBed.inject(JobService) as jasmine.SpyObj<JobService>; // Inject the spy object
    authServiceSpyObj.getLoggedInUser.and.returnValue(MockPartner);
   });

  beforeEach(() => {
    fixture = TestBed.createComponent(JobIntakeTabComponent);
    component = fixture.componentInstance;
    component.job = MockJob;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
    expect(jobServiceSpy).toBeTruthy(); // Add this line to check if the spy is injected
  });
  it('should display loading state when loading is true', () => {
    component.loading = true;
    fixture.detectChanges();
    const loadingElement = fixture.nativeElement.querySelector('.fa-spinner');
    expect(loadingElement).toBeTruthy();
  });
  it('should display error message when error is set', () => {
    const errorMessage = 'An error occurred.';
    component.error = errorMessage;
    fixture.detectChanges();
    const errorElement = fixture.nativeElement.querySelector('div.error-message');
    expect(errorElement.textContent).toContain(errorMessage);
  });
  it('should emit intakeChanged event when onIntakeChanged is called', () => {
    const jobOppIntake: JobOppIntake = new MockJobOppIntake(); // Add your test data here
    const emitSpy = spyOn(component.intakeChanged, 'emit');
    component.onIntakeChanged(jobOppIntake);
    expect(emitSpy).toHaveBeenCalledWith(jobOppIntake);
  });
});
