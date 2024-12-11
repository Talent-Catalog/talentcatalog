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

import {ViewResponseComponent} from "./view-response.component";
import {ComponentFixture, fakeAsync, TestBed, tick} from "@angular/core/testing";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {NO_ERRORS_SCHEMA} from "@angular/core";
import {By} from "@angular/platform-browser";
import {MockCandidate} from "../../../../../MockData/MockCandidate";

describe('ViewResponseComponent', () => {
  let component: ViewResponseComponent;
  let fixture: ComponentFixture<ViewResponseComponent>;
  let mockActiveModal: NgbActiveModal;
  const mockCandidate = new MockCandidate();
  beforeEach(async () => {
    mockActiveModal = jasmine.createSpyObj('NgbActiveModal', ['close', 'dismiss']);

    await TestBed.configureTestingModule({
      declarations: [ ViewResponseComponent ],
      providers: [
        { provide: NgbActiveModal, useValue: mockActiveModal }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ViewResponseComponent);
    component = fixture.componentInstance;
    // Mock data
    component.taskAssignment = mockCandidate.taskAssignments[0];
    component.loading = false;
    component.error = '';

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should display task display name and answer', () => {
    const strongElement = fixture.debugElement.query(By.css('strong'));
    const pElement = fixture.debugElement.query(By.css('p'));

    expect(strongElement.nativeElement.textContent).toContain('CV Submission');
    expect(pElement.nativeElement.textContent).toContain('Sample Answer');
  });

  it('should show loading spinner when loading is true', fakeAsync(() => {
    component.loading = true;
    fixture.detectChanges();
    tick();

    const spinner = fixture.debugElement.query(By.css('.fa-spinner'));

    expect(spinner).toBeTruthy();
  }));

  it('should display error message when error is set', fakeAsync(() => {
    component.error = 'Sample error message';
    fixture.detectChanges();
    tick();

    const errorAlert = fixture.debugElement.query(By.css('.alert-danger'));

    expect(errorAlert.nativeElement.textContent).toContain('Sample error message');
  }));

  it('should call activeModal.close when close is called', () => {
    component.close();
    expect(mockActiveModal.close).toHaveBeenCalled();
  });

  it('should call activeModal.dismiss when dismiss is called', () => {
    component.dismiss();
    expect(mockActiveModal.dismiss).toHaveBeenCalledWith(false);
  });
});
