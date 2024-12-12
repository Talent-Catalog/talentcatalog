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
import {HomeLocationComponent} from "./home-location.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {NgSelectModule} from "@ng-select/ng-select";
import {UntypedFormBuilder, FormsModule, ReactiveFormsModule} from "@angular/forms";
import {CandidateService} from "../../../../services/candidate.service";
import {AutosaveStatusComponent} from "../../../util/autosave-status/autosave-status.component";

describe('HomeLocationComponent', () => {
  let component: HomeLocationComponent;
  let fixture: ComponentFixture<HomeLocationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HttpClientTestingModule,NgSelectModule,FormsModule,ReactiveFormsModule],
      declarations: [HomeLocationComponent,AutosaveStatusComponent],
      providers: [
        UntypedFormBuilder,
        { provide: CandidateService }
      ]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(HomeLocationComponent);
    component = fixture.componentInstance;
    component.candidateIntakeData = {
      homeLocation: 'Test Location'
    };
    fixture.detectChanges(); // Trigger initial data binding
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize the form with candidate data', () => {
    expect(component.form.get('homeLocation').value).toBe('Test Location');
  });
});
