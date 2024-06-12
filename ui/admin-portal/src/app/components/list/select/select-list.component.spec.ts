/*
 * Copyright (c) 2021 Talent Beyond Boundaries.
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
import {SelectListComponent, TargetListSelection} from "./select-list.component";
import {ComponentFixture, fakeAsync, TestBed, tick} from "@angular/core/testing";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {FormBuilder, FormsModule, ReactiveFormsModule} from "@angular/forms";
import {SavedListService} from "../../../services/saved-list.service";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {MockSavedList} from "../../../MockData/MockSavedList";
import {of, throwError} from "rxjs";
import {CandidateStatus, UpdateCandidateStatusInfo} from "../../../model/candidate";
import {JobNameAndId} from "../../../model/job";
import {NgSelectModule} from "@ng-select/ng-select";
fdescribe('SelectListComponent', () => {
  let component: SelectListComponent;
  let fixture: ComponentFixture<SelectListComponent>;
  let activeModalSpy: jasmine.SpyObj<NgbActiveModal>;
  let savedListServiceSpy: jasmine.SpyObj<SavedListService>;
  let formBuilder: FormBuilder;

  beforeEach(async () => {
    const activeModalSpyObj = jasmine.createSpyObj('NgbActiveModal', ['close', 'dismiss']);
    const savedListServiceSpyObj = jasmine.createSpyObj('SavedListService', ['search']);

    await TestBed.configureTestingModule({
      declarations: [SelectListComponent],
      imports: [HttpClientTestingModule,FormsModule,ReactiveFormsModule,NgSelectModule],
      providers: [
        FormBuilder,
        { provide: NgbActiveModal, useValue: activeModalSpyObj },
        { provide: SavedListService, useValue: savedListServiceSpyObj }
      ]
    }).compileComponents();

    activeModalSpy = TestBed.inject(NgbActiveModal) as jasmine.SpyObj<NgbActiveModal>;
    savedListServiceSpy = TestBed.inject(SavedListService) as jasmine.SpyObj<SavedListService>;
    formBuilder = TestBed.inject(FormBuilder);
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SelectListComponent);
    component = fixture.componentInstance;

    component.excludeList = { ...MockSavedList, id: 1 }; // Add an id to the excludeList
    savedListServiceSpy.search.and.returnValue(of([MockSavedList]));
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form with default values', () => {
    expect(component.form).toBeDefined();
    expect(component.newList).toBeFalse();
    expect(component.replace).toBeFalse();
    expect(component.changeStatuses).toBeFalse();
  });
  //
  it('should load lists on initialization', fakeAsync(() => {
    const mockLists = [MockSavedList];
    savedListServiceSpy.search.and.returnValue(of(mockLists));

    component.ngOnInit();
    tick(); // Wait for observable to resolve

    expect(savedListServiceSpy.search).toHaveBeenCalled();
    expect(component.lists).toEqual(mockLists);
    expect(component.loading).toBeFalse();
  }));

  it('should handle error while loading lists', fakeAsync(() => {
    const errorMessage = 'Error loading lists';
    savedListServiceSpy.search.and.returnValue(throwError(errorMessage));

    component.ngOnInit();
    tick(); // Wait for observable to resolve

    expect(savedListServiceSpy.search).toHaveBeenCalled();
    expect(component.error).toEqual(errorMessage);
    expect(component.loading).toBeFalse();
  }));

  it('should dismiss modal', () => {
    component.dismiss();
    expect(activeModalSpy.dismiss).toHaveBeenCalled();
  });

  it('should select list and close modal', () => {
    component.jobId = 1;
    const selection: TargetListSelection = {
      savedListId: 0,
      newListName:null,
      replace: false,
      jobId:1
    };
    component.form.patchValue({ newList: false }); // Simulate selecting existing list

    component.select();
    expect(activeModalSpy.close).toHaveBeenCalledWith(selection);
  });

  it('should handle job selection', () => {
    const job: JobNameAndId = { id: 1, name: 'Test Job' };
    component.onJobSelection(job);
    expect(component.jobName).toBe('Test Job');
    expect(component.jobId).toBe(1);
    expect(component.newListNameControl.value).toBe('Test Job');
  });

  it('should handle status info update', () => {
    const statusInfo: UpdateCandidateStatusInfo = { status: CandidateStatus.active };
    component.onStatusInfoUpdate(statusInfo);
    expect(component['statusUpdateInfo']).toEqual(statusInfo);
  });

  it('should disable new list', () => {
    component.disableNew();
    expect(component.form.get('newList').disabled).toBeTrue();
  });

  it('should enable new list', () => {
    component.enableNew();
    expect(component.form.get('newList').enabled).toBeTrue();
    expect(component.form.get('savedList').value).toBeNull();
  });

});
