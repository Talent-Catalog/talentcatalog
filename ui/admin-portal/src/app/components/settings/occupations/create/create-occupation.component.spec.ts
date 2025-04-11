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
import {CreateOccupationComponent} from "./create-occupation.component";
import {OccupationService} from "../../../../services/occupation.service";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {UntypedFormBuilder, ReactiveFormsModule} from "@angular/forms";
import {ComponentFixture, fakeAsync, TestBed, tick} from "@angular/core/testing";
import {NgSelectModule} from "@ng-select/ng-select";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {Occupation} from "../../../../model/occupation";
import {of, throwError} from "rxjs";

describe('CreateOccupationComponent', () => {
  let component: CreateOccupationComponent;
  let fixture: ComponentFixture<CreateOccupationComponent>;
  let occupationServiceSpy: jasmine.SpyObj<OccupationService>;
  let ngbActiveModalSpy: jasmine.SpyObj<NgbActiveModal>;
  let formBuilder: UntypedFormBuilder;
  // @ts-expect-error
  const createdOccupation: Occupation = { id: 1, name: 'Engineer', status: 'active' };

  beforeEach(async () => {
    const occupationServiceSpyObj = jasmine.createSpyObj('OccupationService', ['create']);
    const ngbActiveModalSpyObj = jasmine.createSpyObj('NgbActiveModal', ['close', 'dismiss']);

    await TestBed.configureTestingModule({
      declarations: [CreateOccupationComponent],
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
    fixture = TestBed.createComponent(CreateOccupationComponent);
    component = fixture.componentInstance;
    occupationServiceSpy.create.and.returnValue(of(createdOccupation));
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should call onSave and close modal when occupation is successfully created', fakeAsync(() => {
    component.occupationForm.patchValue({ name: 'Engineer', status: 'active' });

    component.onSave();
    tick(); // Waiting for async operation to complete

    expect(occupationServiceSpy.create).toHaveBeenCalledWith({ name: 'Engineer', status: 'active' });
    expect(ngbActiveModalSpy.close).toHaveBeenCalledWith(createdOccupation);
    expect(component.saving).toBeFalse();
  }));

  it('should handle error when creating occupation fails', fakeAsync(() => {
    const errorResponse = { status: 500, message: 'Internal Server Error' };
    occupationServiceSpy.create.and.returnValue(throwError(errorResponse));

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
