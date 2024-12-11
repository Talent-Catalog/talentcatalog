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

import {NgbModal, NgbNavModule} from "@ng-bootstrap/ng-bootstrap";
import {AuthenticationService} from "../../../services/authentication.service";
import {CandidateService} from "../../../services/candidate.service";
import {SavedListService} from "../../../services/saved-list.service";
import {ViewCandidateComponent} from "./view-candidate.component";
import {ComponentFixture, TestBed, waitForAsync} from "@angular/core/testing";
import {of, throwError} from "rxjs";
import {ActivatedRoute, convertToParamMap} from "@angular/router";
import {MockCandidate} from "../../../MockData/MockCandidate";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";
import {NgxWigModule} from "ngx-wig";
import {RouterTestingModule} from "@angular/router/testing";
import {MockUser} from "../../../MockData/MockUser";
import {
  CandidateGeneralTabComponent
} from "./tab/candidate-general-tab/candidate-general-tab.component";
import {ViewCandidateLanguageComponent} from "./language/view-candidate-language.component";
import {
  ViewCandidateRegistrationComponent
} from "./registration/view-candidate-registration.component";
import {
  CandidateShareableNotesComponent
} from "../../util/candidate-shareable-notes/candidate-shareable-notes.component";
import {ViewCandidateContactComponent} from "./contact/view-candidate-contact.component";
import {AutosaveStatusComponent} from "../../util/autosave-status/autosave-status.component";
import {ViewCandidateNoteComponent} from "./note/view-candidate-note.component";
import {Candidate} from "../../../model/candidate";
import {SavedList} from "../../../model/saved-list";
import {MockSavedList} from "../../../MockData/MockSavedList";
import {CUSTOM_ELEMENTS_SCHEMA} from "@angular/core";
import {LocalStorageService} from "../../../services/local-storage.service";

describe('ViewCandidateComponent', () => {
  let component: ViewCandidateComponent;
  let fixture: ComponentFixture<ViewCandidateComponent>;
  let mockCandidateService: jasmine.SpyObj<CandidateService>;
  let mockSavedListService: jasmine.SpyObj<SavedListService>;
  let mockActivatedRoute: any;
  let mockModalService: jasmine.SpyObj<NgbModal>;
  let mockLocalStorageService: jasmine.SpyObj<LocalStorageService>;
  let mockAuthenticationService: jasmine.SpyObj<AuthenticationService>;
  const mockCandidate = new MockCandidate();

  beforeEach(waitForAsync(() => {
    const mockCandidateServiceSpy = jasmine.createSpyObj('CandidateService', ['get','getByNumber', 'generateToken','updateCandidate', 'candidateUpdated']);
    mockSavedListService = jasmine.createSpyObj('SavedListService', ['search']);
    mockModalService = jasmine.createSpyObj('NgbModal', ['open']);
    mockLocalStorageService = jasmine.createSpyObj('LocalStorageService', ['get', 'set']);
    mockAuthenticationService = jasmine.createSpyObj('AuthenticationService', ['getLoggedInUser','isEditableCandidate', 'canViewPrivateCandidateInfo', 'canAccessSalesforce']);

    TestBed.configureTestingModule({
      declarations: [ViewCandidateComponent,ViewCandidateNoteComponent,CandidateGeneralTabComponent,CandidateShareableNotesComponent,ViewCandidateContactComponent,AutosaveStatusComponent,ViewCandidateLanguageComponent,ViewCandidateRegistrationComponent],
      imports: [HttpClientTestingModule,FormsModule,NgbNavModule,RouterTestingModule,ReactiveFormsModule, NgSelectModule,NgxWigModule],
      providers: [
        { provide: CandidateService, useValue: mockCandidateServiceSpy },
        { provide: SavedListService, useValue: mockSavedListService },
        { provide: ActivatedRoute, useValue: {
            paramMap: of(convertToParamMap({ candidateNumber: '123' }))
          }
        },        { provide: NgbModal, useValue: mockModalService },
        { provide: LocalStorageService, useValue: mockLocalStorageService },
        { provide: AuthenticationService, useValue: mockAuthenticationService }
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA]
    }).compileComponents();
    mockActivatedRoute = TestBed.inject(ActivatedRoute);
    mockCandidateService = TestBed.inject(CandidateService) as jasmine.SpyObj<CandidateService>;
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ViewCandidateComponent);
    component = fixture.componentInstance;
    mockCandidateService.get.and.returnValue(of(mockCandidate));
    mockAuthenticationService.getLoggedInUser.and.returnValue(new MockUser());
    component.candidate = mockCandidate;
    mockCandidateService.getByNumber.and.returnValue(of(mockCandidate));
    mockCandidateService.generateToken.and.returnValue(of('Token'));
    mockCandidateService.candidateUpdated.and.returnValue(of(mockCandidate));

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should set candidate and title correctly on setCandidate', () => {
    const mockCandidateWithNames: Candidate = { ...mockCandidate, user: { ...mockCandidate.user, firstName: 'Jane', lastName: 'Smith' } };

    spyOn(component['titleService'], 'setTitle').and.stub();

    component.setCandidate(mockCandidateWithNames);

    expect(component.candidate).toEqual(mockCandidateWithNames);
    expect(component['titleService'].setTitle).toHaveBeenCalledWith('Jane Smith 123456');
  });

  it('should set candidate lists correctly on setCandidateLists', () => {
    const mockLists: SavedList[] = [MockSavedList];

    spyOn(component['candidateSavedListService'], 'replace').and.returnValue(of(null));

    component['setCandidateLists'](mockLists);

    expect(component.savingList).toBeFalse();
    expect(component['candidateSavedListService'].replace).toHaveBeenCalled();
  });

  it('should handle loading error when candidate does not exist', () => {
    const errorMessage = 'Candidate not found';
    mockCandidateService.getByNumber.and.returnValue(throwError(errorMessage));

    component.refreshCandidateProfile();

    expect(component.loadingError).toBeTrue();
    expect(component.error).toEqual(`Candidate not found`);
    expect(component.loading).toBeFalse();
  });

});
