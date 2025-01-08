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
import {CandidateTaskTabComponent} from "./candidate-task-tab.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {ViewCandidateTasksComponent} from "../../tasks/view-candidate-tasks.component";
import {MockCandidate} from "../../../../../MockData/MockCandidate";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";
import {NgbPopoverModule} from "@ng-bootstrap/ng-bootstrap";

describe('CandidateTaskTabComponent', () => {
  let component: CandidateTaskTabComponent;
  let fixture: ComponentFixture<CandidateTaskTabComponent>;
  const mockCandidate = new MockCandidate();
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HttpClientTestingModule,FormsModule,ReactiveFormsModule, NgSelectModule, NgbPopoverModule],
      declarations: [
        CandidateTaskTabComponent,
        ViewCandidateTasksComponent
      ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CandidateTaskTabComponent);
    component = fixture.componentInstance;
    component.candidate = mockCandidate;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should display tasks correctly based on candidate data', () => {
    const candidateData = mockCandidate;
    component.candidate = candidateData;
    component.editable = true;
    fixture.detectChanges();

    const mockViewCandidateTasksComponent = fixture.debugElement.children[0].componentInstance;
    expect(mockViewCandidateTasksComponent.candidate).toBe(candidateData);
    expect(mockViewCandidateTasksComponent.editable).toBeTrue();
  });
});
