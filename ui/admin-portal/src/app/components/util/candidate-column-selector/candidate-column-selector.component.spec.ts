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

import {By} from '@angular/platform-browser';
import {CandidateColumnSelectorComponent} from "./candidate-column-selector.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {CandidateFieldService} from "../../../services/candidate-field.service";
import {CandidateSourceService} from "../../../services/candidate-source.service";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {DragulaModule, DragulaService, DrakeFactory} from "ng2-dragula";


describe('CandidateColumnSelectorComponent', () => {
  let component: CandidateColumnSelectorComponent;
  let fixture: ComponentFixture<CandidateColumnSelectorComponent>;
  let candidateFieldService: jasmine.SpyObj<CandidateFieldService>;
  let candidateSourceService: jasmine.SpyObj<CandidateSourceService>;
  let activeModal: jasmine.SpyObj<NgbActiveModal>;

  beforeEach(async () => {
    const candidateFieldServiceSpy = jasmine.createSpyObj('CandidateFieldService', ['getCandidateSourceFields', 'displayableFieldsMap']);
    const candidateSourceServiceSpy = jasmine.createSpyObj('CandidateSourceService', ['updateDisplayedFieldPaths']);
    const activeModalSpy = jasmine.createSpyObj('NgbActiveModal', ['close', 'dismiss']);

    await TestBed.configureTestingModule({
      imports: [DragulaModule],
      declarations: [CandidateColumnSelectorComponent],
      providers: [
        { provide: DragulaService, useValue: new DragulaService(null) },
        { provide: CandidateFieldService, useValue: candidateFieldServiceSpy },
        { provide: CandidateSourceService, useValue: candidateSourceServiceSpy },
        { provide: NgbActiveModal, useValue: activeModalSpy }
      ]
    }).compileComponents();

    candidateFieldService = TestBed.inject(CandidateFieldService) as jasmine.SpyObj<CandidateFieldService>;
    candidateSourceService = TestBed.inject(CandidateSourceService) as jasmine.SpyObj<CandidateSourceService>;
    activeModal = TestBed.inject(NgbActiveModal) as jasmine.SpyObj<NgbActiveModal>;
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CandidateColumnSelectorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should display an error message if error is set', () => {
    component.error = 'An error occurred';
    fixture.detectChanges();

    const errorMessage = fixture.debugElement.query(By.css('.alert-danger'));
    expect(errorMessage.nativeElement.textContent).toContain('An error occurred');
  });

  it('should display updating spinner when updating is true', () => {
    component.updating = true;
    fixture.detectChanges();

    const updatingSpinner = fixture.debugElement.query(By.css('.fa-spinner'));
    expect(updatingSpinner).toBeTruthy();
  });

  it('should call dismiss on activeModal when dismiss is called', () => {
    component.dismiss();
    expect(activeModal.dismiss).toHaveBeenCalledWith(false);
  });

  it('should initialize dragula group if not already present', () => {
    const dragulaService = TestBed.inject(DragulaService);
    spyOn(dragulaService, 'find').and.returnValue(null);
    spyOn(dragulaService, 'createGroup');

    component.ngOnInit();
    expect(dragulaService.createGroup).toHaveBeenCalledWith(component.dragulaGroupName, {});
  });
});
