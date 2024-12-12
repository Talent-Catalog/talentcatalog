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
import {ViewCandidateSkillComponent} from "./view-candidate-skill.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {MockCandidate} from "../../../../MockData/MockCandidate";
import {HttpClientTestingModule} from "@angular/common/http/testing";

describe('ViewCandidateSkillComponent', () => {
  let component: ViewCandidateSkillComponent;
  let fixture: ComponentFixture<ViewCandidateSkillComponent>;
  const mockCandidate = new MockCandidate();
  beforeEach(async () => {

    await TestBed.configureTestingModule({
      declarations: [ViewCandidateSkillComponent],
      imports: [HttpClientTestingModule],
      providers: [
      ]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ViewCandidateSkillComponent);
    component = fixture.componentInstance;
    component.candidate = mockCandidate;
    component.editable = true;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
