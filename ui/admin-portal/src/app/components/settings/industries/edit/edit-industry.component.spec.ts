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
import {IndustryService} from "../../../../services/industry.service";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {UntypedFormBuilder, ReactiveFormsModule} from "@angular/forms";
import {EditIndustryComponent} from "./edit-industry.component";
import {ComponentFixture, fakeAsync, TestBed, tick} from "@angular/core/testing";
import {NgSelectModule} from "@ng-select/ng-select";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {of, throwError} from "rxjs";
import {Industry} from "../../../../model/industry";

describe('EditIndustryComponent', () => {
  let component: EditIndustryComponent;
  let fixture: ComponentFixture<EditIndustryComponent>;
  let industryServiceSpy: jasmine.SpyObj<IndustryService>;
  let ngbActiveModalSpy: jasmine.SpyObj<NgbActiveModal>;
  let formBuilder: UntypedFormBuilder;
  // @ts-expect-error
  const industry: Industry = {name: 'Test Industry', status: 'active' };

  beforeEach(async () => {
    const industryServiceSpyObj = jasmine.createSpyObj('IndustryService', ['get', 'update']);
    const ngbActiveModalSpyObj = jasmine.createSpyObj('NgbActiveModal', ['close', 'dismiss']);

    await TestBed.configureTestingModule({
      declarations: [EditIndustryComponent],
      imports: [ReactiveFormsModule,NgSelectModule,HttpClientTestingModule],
      providers: [
        { provide: IndustryService, useValue: industryServiceSpyObj },
        { provide: NgbActiveModal, useValue: ngbActiveModalSpyObj }
      ]
    }).compileComponents();

    industryServiceSpy = TestBed.inject(IndustryService) as jasmine.SpyObj<IndustryService>;
    ngbActiveModalSpy = TestBed.inject(NgbActiveModal) as jasmine.SpyObj<NgbActiveModal>;
    formBuilder = TestBed.inject(UntypedFormBuilder);
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(EditIndustryComponent);
    component = fixture.componentInstance;
    component.industryId = 1; // Set industryId for testing
    industryServiceSpy.get.and.returnValue(of(industry));
    industryServiceSpy.update.and.returnValue(of(industry));

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form with industry data', fakeAsync(() => {

    component.ngOnInit();
    tick(); // Waiting for async operation to complete

    expect(industryServiceSpy.get).toHaveBeenCalledWith(1);
    expect(component.industryForm.value).toEqual(industry);
    expect(component.loading).toBeFalse();
  }));

  it('should call onSave and close modal when industry is successfully updated', fakeAsync(() => {
    component.industryForm.patchValue({ name: 'Updated Industry', status: 'inactive' });

    component.onSave();
    tick(); // Waiting for async operation to complete

    expect(industryServiceSpy.update).toHaveBeenCalledWith(1, { name: 'Updated Industry', status: 'inactive' });
    expect(ngbActiveModalSpy.close).toHaveBeenCalledWith(industry);
    expect(component.saving).toBeFalse();
  }));

  it('should handle error when updating industry fails', fakeAsync(() => {
    const errorResponse = { status: 500, message: 'Internal Server Error' };
    industryServiceSpy.update.and.returnValue(throwError(errorResponse));

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
