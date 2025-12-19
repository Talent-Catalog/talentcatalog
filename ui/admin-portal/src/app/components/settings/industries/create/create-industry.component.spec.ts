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
import {CreateIndustryComponent} from "./create-industry.component";
import {ComponentFixture, fakeAsync, TestBed, tick} from "@angular/core/testing";
import {IndustryService} from "../../../../services/industry.service";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {UntypedFormBuilder, ReactiveFormsModule} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {Industry} from "../../../../model/industry";
import {of, throwError} from "rxjs";

describe('CreateIndustryComponent', () => {
  let component: CreateIndustryComponent;
  let fixture: ComponentFixture<CreateIndustryComponent>;
  let industryServiceSpy: jasmine.SpyObj<IndustryService>;
  let ngbActiveModalSpy: jasmine.SpyObj<NgbActiveModal>;
  let formBuilder: UntypedFormBuilder;
  const industry: Industry = { id: 1, name: 'Test Industry', status: 'active' };

  beforeEach(async () => {
    const industryServiceSpyObj = jasmine.createSpyObj('IndustryService', ['create']);
    const ngbActiveModalSpyObj = jasmine.createSpyObj('NgbActiveModal', ['close', 'dismiss']);

    await TestBed.configureTestingModule({
      declarations: [CreateIndustryComponent],
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
    fixture = TestBed.createComponent(CreateIndustryComponent);
    component = fixture.componentInstance;
    industryServiceSpy.create.and.returnValue(of(industry));

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form with empty values', () => {
    expect(component.industryForm).toBeDefined();
    expect(component.industryForm.get('name').value).toBeNull();
    expect(component.industryForm.get('status').value).toBeNull();
  });

  it('should call onSave and close modal when industry is successfully created', fakeAsync(() => {
    component.industryForm.patchValue({ name: 'Test Industry', status: 'active' });

    component.onSave();
    tick(); // Waiting for async operation to complete

    expect(industryServiceSpy.create).toHaveBeenCalledWith({ name: 'Test Industry', status: 'active' });
    expect(ngbActiveModalSpy.close).toHaveBeenCalledWith(industry);
    expect(component.saving).toBeFalse();
  }));

  it('should handle error when creating industry fails', fakeAsync(() => {
    const errorResponse = { status: 500, message: 'Internal Server Error' };
    industryServiceSpy.create.and.returnValue(throwError(errorResponse));

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
