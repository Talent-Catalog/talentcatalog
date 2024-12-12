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
import {CreateExternalLinkComponent} from "./create-external-link.component";
import {SavedListService} from "../../../../services/saved-list.service";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {UntypedFormBuilder, ReactiveFormsModule} from "@angular/forms";
import {ComponentFixture, fakeAsync, TestBed, tick} from "@angular/core/testing";
import {NgSelectModule} from "@ng-select/ng-select";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {SavedList, SearchSavedListRequest} from "../../../../model/saved-list";
import {of, throwError} from "rxjs";
import {MockSavedList} from "../../../../MockData/MockSavedList";

describe('CreateExternalLinkComponent', () => {
  let component: CreateExternalLinkComponent;
  let fixture: ComponentFixture<CreateExternalLinkComponent>;
  let savedListServiceSpy: jasmine.SpyObj<SavedListService>;
  let ngbActiveModalSpy: jasmine.SpyObj<NgbActiveModal>;
  let formBuilder: UntypedFormBuilder;
  const savedLists: SavedList[] =  [MockSavedList];
  const link: SavedList = {
    fixed: false,
    global: false,
    id: 1, name: 'Test List', fileJdLink:"link" };
  beforeEach(async () => {
    const savedListServiceSpyObj = jasmine.createSpyObj('SavedListService', ['search', 'updateShortName']);
    const ngbActiveModalSpyObj = jasmine.createSpyObj('NgbActiveModal', ['close', 'dismiss']);

    await TestBed.configureTestingModule({
      declarations: [CreateExternalLinkComponent],
      imports: [ReactiveFormsModule,NgSelectModule,HttpClientTestingModule],
      providers: [
        { provide: SavedListService, useValue: savedListServiceSpyObj },
        { provide: NgbActiveModal, useValue: ngbActiveModalSpyObj }
      ]
    }).compileComponents();

    savedListServiceSpy = TestBed.inject(SavedListService) as jasmine.SpyObj<SavedListService>;
    ngbActiveModalSpy = TestBed.inject(NgbActiveModal) as jasmine.SpyObj<NgbActiveModal>;
    formBuilder = TestBed.inject(UntypedFormBuilder);
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CreateExternalLinkComponent);
    component = fixture.componentInstance;
    savedListServiceSpy.search.and.returnValue(of(savedLists));

    savedListServiceSpy.updateShortName.and.returnValue(of(link));

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form with required fields', () => {
    expect(component.form).toBeDefined();
    expect(component.form.get('savedListId')).toBeTruthy();
    expect(component.form.get('tbbShortName')).toBeTruthy();
  });

  it('should load saved lists on initialization', fakeAsync(() => {
    const request: SearchSavedListRequest = {
      owned: true,
      shared: true,
      global: true,
      shortName: false
    };

    component.ngOnInit();
    tick(); // Waiting for async operation to complete

    expect(savedListServiceSpy.search).toHaveBeenCalledWith(request);
    expect(component.savedLists).toEqual(savedLists);
  }));

  it('should call onSave and close modal when external link is successfully created', fakeAsync(() => {
    const savedListId = 1;
    const shortName = 'test-link';

    component.form.patchValue({ savedListId, tbbShortName: shortName });

    component.onSave();
    tick(); // Waiting for async operation to complete

    expect(savedListServiceSpy.updateShortName).toHaveBeenCalledWith({ savedListId, tbbShortName: shortName });
    expect(ngbActiveModalSpy.close).toHaveBeenCalledWith(link);
    expect(component.saving).toBeFalse();
  }));

  it('should handle error when creating external link fails', fakeAsync(() => {
    const errorResponse = { status: 500, message: 'Internal Server Error' };
    savedListServiceSpy.updateShortName.and.returnValue(throwError(errorResponse));

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
