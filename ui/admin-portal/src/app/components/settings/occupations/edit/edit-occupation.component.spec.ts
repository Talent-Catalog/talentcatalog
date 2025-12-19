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
import {EditOccupationComponent} from "./edit-occupation.component";
import {ComponentFixture, fakeAsync, TestBed, tick} from "@angular/core/testing";
import {OccupationService} from "../../../../services/occupation.service";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {UntypedFormBuilder, ReactiveFormsModule} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {Occupation} from "../../../../model/occupation";
import {of, throwError} from "rxjs";

describe('EditOccupationComponent', () => {
  let component: EditOccupationComponent;
  let fixture: ComponentFixture<EditOccupationComponent>;
  let occupationServiceSpy: jasmine.SpyObj<OccupationService>;
  let ngbActiveModalSpy: jasmine.SpyObj<NgbActiveModal>;
  let formBuilder: UntypedFormBuilder;
  // @ts-expect-error
  const occupationData: Occupation = { id: 1, name: 'Engineer', status: 'active' };
  // @ts-expect-error
  const updatedOccupation: Occupation = { id: 1, name: 'Scientist', status: 'active' };

  beforeEach(async () => {
    const occupationServiceSpyObj = jasmine.createSpyObj('OccupationService', ['get', 'update']);
    const ngbActiveModalSpyObj = jasmine.createSpyObj('NgbActiveModal', ['close', 'dismiss']);

    await TestBed.configureTestingModule({
      declarations: [EditOccupationComponent],
      imports: [ReactiveFormsModule,NgSelectModule,HttpClientTestingModule],
      providers: [
        { provide: OccupationService, useValue: occupationServiceSpyObj },
        { provide: NgbActiveModal, useValue: ngbActiveModalSpyObj }
      ]
    }).compileComponents();

    occupationServiceSpy = TestBed.inject(OccupationService) as jasmine.SpyObj<OccupationService>;
    ngbActiveModalSpy = TestBed.inject(NgbActiveModal) as jasmine.SpyObj<NgbActiveModal>;
    formBuilder = TestBed.inject(UntypedFormBuilder);
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(EditOccupationComponent);
    component = fixture.componentInstance;
    component.occupationId = 1; // Assuming an occupation ID is provided
    occupationServiceSpy.get.and.returnValue(of(occupationData));
    occupationServiceSpy.update.and.returnValue(of(updatedOccupation));
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load occupation data on initialization', fakeAsync(() => {

    component.ngOnInit();
    tick(); // Waiting for async operation to complete

    expect(occupationServiceSpy.get).toHaveBeenCalledWith(1);
    expect(component.occupationForm.value).toEqual({ name: 'Engineer', status: 'active' });
    expect(component.loading).toBeFalse();
  }));

  it('should call onSave and close modal when occupation is successfully updated', fakeAsync(() => {
    component.occupationForm.patchValue({ name: 'Scientist', status: 'active' });

    component.onSave();
    tick(); // Waiting for async operation to complete

    expect(occupationServiceSpy.update).toHaveBeenCalledWith(1, { name: 'Scientist', status: 'active' });
    expect(ngbActiveModalSpy.close).toHaveBeenCalledWith(updatedOccupation);
    expect(component.saving).toBeFalse();
  }));

  it('should handle error when updating occupation fails', fakeAsync(() => {
    const errorResponse = { status: 500, message: 'Internal Server Error' };
    occupationServiceSpy.update.and.returnValue(throwError(errorResponse));

    component.onSave();
    tick(); // Waiting for async operation to complete

    expect(component.error).toEqual(errorResponse);
    expect(component.saving).toBeFalse();
  }));

  it('should dismiss modal when dismiss is called', () => {
    component.dismiss();
    expect(ngbActiveModalSpy.dismiss).toHaveBeenCalledWith(false);
  });
});
