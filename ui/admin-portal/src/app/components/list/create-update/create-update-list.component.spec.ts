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
import {CreateUpdateListComponent} from "./create-update-list.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {SavedListService} from "../../../services/saved-list.service";
import {NgbActiveModal, NgbTypeaheadModule} from "@ng-bootstrap/ng-bootstrap";
import {SalesforceService} from "../../../services/salesforce.service";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {MockSavedList} from "../../../MockData/MockSavedList";
import {of, throwError} from "rxjs";
import {JoblinkComponent} from "../../util/joblink/joblink.component";
import {JobNameAndId} from "../../../model/job";

describe('CreateUpdateListComponent', () => {
  let component: CreateUpdateListComponent;
  let fixture: ComponentFixture<CreateUpdateListComponent>;
  let savedListServiceSpy: jasmine.SpyObj<SavedListService>;
  let activeModalSpy: jasmine.SpyObj<NgbActiveModal>;
  let salesforceServiceSpy: jasmine.SpyObj<SalesforceService>;

  beforeEach(async () => {
    const savedListServiceSpyObj = jasmine.createSpyObj('SavedListService', ['create', 'update']);
    const activeModalSpyObj = jasmine.createSpyObj('NgbActiveModal', ['close', 'dismiss']);
    const salesforceServiceSpyObj = jasmine.createSpyObj('SalesforceService', ['fetchJob']);

    await TestBed.configureTestingModule({
      declarations: [CreateUpdateListComponent,JoblinkComponent],
      imports: [HttpClientTestingModule,FormsModule,ReactiveFormsModule,NgbTypeaheadModule],
      providers: [
        { provide: SavedListService, useValue: savedListServiceSpyObj },
        { provide: NgbActiveModal, useValue: activeModalSpyObj },
        { provide: SalesforceService, useValue: salesforceServiceSpyObj }
      ]
    }).compileComponents();

    savedListServiceSpy = TestBed.inject(SavedListService) as jasmine.SpyObj<SavedListService>;
    activeModalSpy = TestBed.inject(NgbActiveModal) as jasmine.SpyObj<NgbActiveModal>;
    salesforceServiceSpy = TestBed.inject(SalesforceService) as jasmine.SpyObj<SalesforceService>;
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CreateUpdateListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form with savedList data', () => {
    component.savedList = MockSavedList;
    component.ngOnInit();
    expect(component.form.value.name).toBe(MockSavedList.name);
    expect(component.form.value.fixed).toBe(MockSavedList.fixed);
  });

  it('should close modal when save is successful', () => {
    savedListServiceSpy.create.and.returnValue(of(MockSavedList));
    component.save();
    expect(savedListServiceSpy.create).toHaveBeenCalled();
    expect(activeModalSpy.close).toHaveBeenCalled();
  });

  it('should save fails when error throw', () => {
    savedListServiceSpy.create.and.returnValue(throwError('Error saving list'));
    component.save();
    expect(savedListServiceSpy.create).toHaveBeenCalled();
    expect(component.saving).toBeFalsy();
  });

  it('should close modal when closeModal is called', () => {
    component.closeModal(MockSavedList);
    expect(activeModalSpy.close).toHaveBeenCalled();
  });

  it('should dismiss modal when dismiss is called', () => {
    component.dismiss();
    expect(activeModalSpy.dismiss).toHaveBeenCalled();
  });

  it('should update nameControl value when job is selected', () => {
    const job: JobNameAndId = { id: 1, name: 'Test Job' };
    component.onJobSelection(job);
    expect(component.nameControl.value).toBe('Test Job');
  });

  it('should set jobId to -1 when null job is selected', () => {
    component.onJobSelection(null);
    expect(component.jobId).toBe(-1);
  });

});
