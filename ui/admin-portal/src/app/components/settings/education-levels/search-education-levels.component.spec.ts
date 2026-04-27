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

import {SearchEducationLevelsComponent} from "./search-education-levels.component";
import {ComponentFixture, fakeAsync, TestBed, tick} from "@angular/core/testing";
import {NgbModal, NgbModalRef, NgbModule} from "@ng-bootstrap/ng-bootstrap";
import {EducationLevelService} from "../../../services/education-level.service";
import {AuthorizationService} from "../../../services/authorization.service";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";
import {MockUser} from "../../../MockData/MockUser";
import {EducationLevel} from "../../../model/education-level";
import {SearchResults} from "../../../model/search-results";
import {of, throwError} from "rxjs";
import {CreateEducationLevelComponent} from "./create/create-education-level.component";
import {EditEducationLevelComponent} from "./edit/edit-education-level.component";
import {ConfirmationComponent} from "../../util/confirm/confirmation.component";
import {FileSelectorComponent} from "../../util/file-selector/file-selector.component";

describe('SearchEducationLevelsComponent', () => {
  let component: SearchEducationLevelsComponent;
  let fixture: ComponentFixture<SearchEducationLevelsComponent>;
  let modalService: NgbModal;
  let educationLevelService: jasmine.SpyObj<EducationLevelService>;
  let authService: jasmine.SpyObj<AuthorizationService>;
  let activeModalSpy: jasmine.SpyObj<NgbModalRef>;
    const searchResults: SearchResults<EducationLevel> = {
      first: false,
      last: false,
      number: 0,
      size: 0,
      totalPages: 0,
      totalElements: 1, content: [{ id: 1, level: 1, name: 'Primary', status: 'active' }] };
  beforeEach(async () => {
    const educationLevelServiceSpy = jasmine.createSpyObj('EducationLevelService', ['search', 'delete', 'addSystemLanguageTranslations']);
    const authServiceSpy = jasmine.createSpyObj('AuthorizationService', ['isAnAdmin']);

    await TestBed.configureTestingModule({
      imports: [FormsModule, ReactiveFormsModule, NgbModule,NgSelectModule],
      declarations: [SearchEducationLevelsComponent],
      providers: [
        { provide: EducationLevelService, useValue: educationLevelServiceSpy },
        { provide: AuthorizationService, useValue: authServiceSpy },
        NgbModal
      ]
    }).compileComponents();

    educationLevelService = TestBed.inject(EducationLevelService) as jasmine.SpyObj<EducationLevelService>;
    authService = TestBed.inject(AuthorizationService) as jasmine.SpyObj<AuthorizationService>;
    modalService = TestBed.inject(NgbModal);
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SearchEducationLevelsComponent);
    component = fixture.componentInstance;
    activeModalSpy = jasmine.createSpyObj('NgbModalRef', ['close', 'dismiss']);
    component.loggedInUser = new MockUser();
    educationLevelService.search.and.returnValue(of(searchResults));

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should search for education levels on form changes', fakeAsync(() => {
    component.searchForm.controls['keyword'].setValue('Primary');
    tick(400);
    expect(educationLevelService.search).toHaveBeenCalled();
    expect(component.results).toEqual(searchResults);
  }));

  it('should open add education level modal and refresh list on success', fakeAsync(() => {
    spyOn(modalService, 'open').and.returnValue(activeModalSpy);
    activeModalSpy.result = Promise.resolve('added education level');

    educationLevelService.search.and.returnValue(of(searchResults));

    component.addEducationLevel();
    tick();

    expect(modalService.open).toHaveBeenCalledWith(CreateEducationLevelComponent, jasmine.any(Object));
    expect(educationLevelService.search).toHaveBeenCalledTimes(2);
  }));

  it('should open edit education level modal and refresh list on success', fakeAsync(() => {
    const mockEducationLevel: EducationLevel = { id: 1, level: 1, name: 'Primary', status: 'active' };
    const mockModalRef: Partial<NgbModalRef> = {
      componentInstance: { educationLevelId: undefined },
      result: Promise.resolve(mockEducationLevel)
    };
    spyOn(modalService, 'open').and.returnValue(mockModalRef as NgbModalRef);

    component.editEducationLevel(mockEducationLevel);
    tick();

    expect(modalService.open).toHaveBeenCalledWith(EditEducationLevelComponent, jasmine.any(Object));
    expect(mockModalRef.componentInstance.educationLevelId).toBe(mockEducationLevel.id);
    expect(educationLevelService.search).toHaveBeenCalledTimes(2);
  }));

  it('should open confirmation modal and delete education level on confirm', fakeAsync(() => {
    const mockEducationLevel: EducationLevel = { id: 1, level: 1, name: 'Primary', status: 'active' };
    const mockModalRef: Partial<NgbModalRef> = {
      componentInstance: { message: "" },
      result: Promise.resolve(true)
    };
    spyOn(modalService, 'open').and.returnValue(mockModalRef as NgbModalRef);
    educationLevelService.delete.and.returnValue(of(true));
    component.deleteEducationLevel(mockEducationLevel);
    tick();

    expect(modalService.open).toHaveBeenCalledWith(ConfirmationComponent, jasmine.any(Object));
    expect(educationLevelService.delete).toHaveBeenCalledWith(mockEducationLevel.id);
    expect(educationLevelService.search).toHaveBeenCalledTimes(3);
  }));

  it('should import translations and refresh list on success', fakeAsync(() => {
    const mockFile = new File(["mock content"], "mockfile.txt", { type: "text/plain" });
    const mockModalRef: Partial<NgbModalRef> = {
      componentInstance: {
        validExtensions: undefined,
        maxFiles: undefined,
        closeButtonLabel: undefined,
        title: undefined,
        instructions: undefined
      },
      result: Promise.resolve([mockFile])
    };
    spyOn(modalService, 'open').and.returnValue(mockModalRef as NgbModalRef);
    educationLevelService.addSystemLanguageTranslations.and.returnValue(of());
    component.importForm.controls['langCode'].setValue('en');
    component.importTranslations();
    tick();

    expect(modalService.open).toHaveBeenCalledWith(FileSelectorComponent, jasmine.any(Object));
    expect(educationLevelService.addSystemLanguageTranslations).toHaveBeenCalledWith('en', mockFile);
    expect(educationLevelService.search).toHaveBeenCalledTimes(1);
  }));

  it('should handle errors during translation import', fakeAsync(() => {
    const mockFile = new File(["mock content"], "mockfile.txt", { type: "text/plain" });
    const mockModalRef: Partial<NgbModalRef> = {
      componentInstance: {
        validExtensions: undefined,
        maxFiles: undefined,
        closeButtonLabel: undefined,
        title: undefined,
        instructions: undefined
      },
      result: Promise.resolve([mockFile])
    };
    spyOn(modalService, 'open').and.returnValue(mockModalRef as NgbModalRef);
    educationLevelService.addSystemLanguageTranslations.and.returnValue(throwError('Error occurred'));

    component.importForm.controls['langCode'].setValue('en');
    component.importTranslations();
    tick();

    expect(modalService.open).toHaveBeenCalledWith(FileSelectorComponent, jasmine.any(Object));
    expect(educationLevelService.addSystemLanguageTranslations).toHaveBeenCalledWith('en', mockFile);
    expect(component.error).toBe('Error occurred');
  }));

  it('should handle errors during deletion', fakeAsync(() => {
    const mockEducationLevel: EducationLevel = { id: 1, level: 1, name: 'Primary', status: 'active' };
    const mockModalRef: Partial<NgbModalRef> = {
      componentInstance: { message: "" },
      result: Promise.resolve(true)
    };
    spyOn(modalService, 'open').and.returnValue(mockModalRef as NgbModalRef);
    educationLevelService.delete.and.returnValue(throwError('Error occurred'));

    component.deleteEducationLevel(mockEducationLevel);
    tick();

    expect(modalService.open).toHaveBeenCalledWith(ConfirmationComponent, jasmine.any(Object));
    expect(educationLevelService.delete).toHaveBeenCalledWith(mockEducationLevel.id);
    expect(component.error).toBe('Error occurred');
  }));

});
