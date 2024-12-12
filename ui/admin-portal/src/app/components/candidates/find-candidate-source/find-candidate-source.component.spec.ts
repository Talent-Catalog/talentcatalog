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

import {ComponentFixture, TestBed} from '@angular/core/testing';
import {FindCandidateSourceComponent} from './find-candidate-source.component';
import {CandidateSourceService} from "../../../services/candidate-source.service";
import {of} from "rxjs";
import {MockCandidateSource} from "../../../MockData/MockCandidateSource";
import {FormsModule} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";

describe('FindCandidateSourceComponent', () => {
  let component: FindCandidateSourceComponent;
  let fixture: ComponentFixture<FindCandidateSourceComponent>;
  let candidateSourceService: jasmine.SpyObj<CandidateSourceService>;
  let mockCandidateSource: MockCandidateSource = new MockCandidateSource();

  beforeEach(async () => {
    const candidateSourceServiceSpy = jasmine.createSpyObj('CandidateSourceService',
      ['searchPaged', 'get','searchByIds']);

    await TestBed.configureTestingModule({
      declarations: [ FindCandidateSourceComponent ],
      imports: [
        FormsModule,
        NgSelectModule
      ],
      providers: [
        { provide: CandidateSourceService, useValue: candidateSourceServiceSpy }
      ]
    })
    .compileComponents();

    candidateSourceService = TestBed.inject(CandidateSourceService) as jasmine.SpyObj<CandidateSourceService>;
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(FindCandidateSourceComponent);
    component = fixture.componentInstance;
    candidateSourceService.get.and.returnValue(of(mockCandidateSource));
    candidateSourceService.searchByIds.and.returnValue(of([mockCandidateSource]));

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });


  it('should initialize correctly', () => {
    component.ngOnInit();
    component.single = true;
    component.selectedIds = 1;
    component.ngOnChanges(null);

    expect(component.currentSelection).toEqual(mockCandidateSource);
  });

  it('should emit selection correctly', (done) => {
    component.single = true;

    component.selectionMade.subscribe({
      next: selectedSource => {
        expect(selectedSource).toEqual(mockCandidateSource);
        done();
      }
    });

    component.onChangedSelection(mockCandidateSource);
  });
});
