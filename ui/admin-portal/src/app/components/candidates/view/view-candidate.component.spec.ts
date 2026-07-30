/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

import {NgbModal, NgbNavModule} from "@ng-bootstrap/ng-bootstrap";
import {AuthenticationService} from "../../../services/authentication.service";
import {CandidateService} from "../../../services/candidate.service";
import {CandidateSavedListService} from "../../../services/candidate-saved-list.service";
import {SavedListService} from "../../../services/saved-list.service";
import {ViewCandidateComponent} from "./view-candidate.component";
import {ComponentFixture, fakeAsync, TestBed, tick, waitForAsync} from "@angular/core/testing";
import {of, Subject, throwError} from "rxjs";
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
import {Candidate, CandidateStatus, UpdateCandidateStatusInfo} from "../../../model/candidate";
import {SavedList} from "../../../model/saved-list";
import {MockSavedList} from "../../../MockData/MockSavedList";
import {CUSTOM_ELEMENTS_SCHEMA} from "@angular/core";
import {LocalStorageService} from "../../../services/local-storage.service";
import {AuthorizationService} from "../../../services/authorization.service";
import {EditCandidateStatusComponent} from "./status/edit-candidate-status.component";

describe('ViewCandidateComponent', () => {
  let component: ViewCandidateComponent;
  let fixture: ComponentFixture<ViewCandidateComponent>;
  let mockCandidateService: jasmine.SpyObj<CandidateService>;
  let mockSavedListService: jasmine.SpyObj<SavedListService>;
  let mockCandidateSavedListService: jasmine.SpyObj<CandidateSavedListService>;
  let mockActivatedRoute: any;
  let mockModalService: jasmine.SpyObj<NgbModal>;
  let mockLocalStorageService: jasmine.SpyObj<LocalStorageService>;
  let mockAuthenticationService: jasmine.SpyObj<AuthenticationService>;
  let authorizationServiceSpy: jasmine.SpyObj<AuthorizationService>;
  const mockCandidate = new MockCandidate();
  mockCandidate.folderlink = 'https://localhost:8080/folder';

  beforeEach(waitForAsync(() => {
    const mockCandidateServiceSpy = jasmine.createSpyObj('CandidateService', ['get','getByNumber', 'generateToken','updateCandidate', 'candidateUpdated', 'updateStatus']);
    mockSavedListService = jasmine.createSpyObj('SavedListService', ['search']);
    mockCandidateSavedListService = jasmine.createSpyObj('CandidateSavedListService', ['search', 'replace']);
    mockCandidateSavedListService.search.and.returnValue(of([]));
    mockCandidateSavedListService.replace.and.returnValue(of(null));
    mockModalService = jasmine.createSpyObj('NgbModal', ['open']);
    mockLocalStorageService = jasmine.createSpyObj('LocalStorageService', ['get', 'set']);
    mockAuthenticationService = jasmine.createSpyObj('AuthenticationService', ['getLoggedInUser','isGrnInstance'], { loggedInUser$: new Subject<any>() });
    const authorizationSpy = jasmine.createSpyObj('AuthorizationService', [
      'isEditableCandidate',
      'canViewPrivateCandidateInfo',
      'canAccessSalesforce',
      'canAccessGoogleDrive',
      'canSeeGlobalLists',
      'canViewCandidateCV',
      'canViewChats',
      'canSeeJobDetails',
      'isAnAdmin',
      'isReadOnly',
      'canEraseCandidateData'
    ]);

    TestBed.configureTestingModule({
      declarations: [ViewCandidateComponent,ViewCandidateNoteComponent,CandidateGeneralTabComponent,CandidateShareableNotesComponent,ViewCandidateContactComponent,AutosaveStatusComponent,ViewCandidateLanguageComponent,ViewCandidateRegistrationComponent],
      imports: [HttpClientTestingModule,FormsModule,NgbNavModule,RouterTestingModule,ReactiveFormsModule, NgSelectModule,NgxWigModule],
      providers: [
        { provide: CandidateService, useValue: mockCandidateServiceSpy },
        { provide: SavedListService, useValue: mockSavedListService },
        { provide: CandidateSavedListService, useValue: mockCandidateSavedListService },
        { provide: ActivatedRoute, useValue: {
            paramMap: of(convertToParamMap({ candidateNumber: '123' }))
          }
        },        { provide: NgbModal, useValue: mockModalService },
        { provide: LocalStorageService, useValue: mockLocalStorageService },
        { provide: AuthenticationService, useValue: mockAuthenticationService },
        { provide: AuthorizationService, useValue: authorizationSpy }
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA]
    }).compileComponents();
    mockActivatedRoute = TestBed.inject(ActivatedRoute);
    mockCandidateService = TestBed.inject(CandidateService) as jasmine.SpyObj<CandidateService>;
    authorizationServiceSpy = TestBed.inject(AuthorizationService) as jasmine.SpyObj<AuthorizationService>;
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

    component['setCandidateLists'](mockLists);

    expect(component.savingList).toBeFalse();
    expect(mockCandidateSavedListService.replace).toHaveBeenCalled();
  });

  it('should handle loading error when candidate does not exist', () => {
    const errorMessage = 'Candidate not found';
    mockCandidateService.getByNumber.and.returnValue(throwError(errorMessage));

    component.refreshCandidateProfile();

    expect(component.loadingError).toBeTrue();
    expect(component.error).toEqual(`Candidate not found`);
    expect(component.loading).toBeFalse();
  });

  it('should call canAccessGoogleDrive()', () => {
    expect(authorizationServiceSpy.canAccessGoogleDrive).toHaveBeenCalled();
  })

  it('should not show Google Drive icon if user is not meant to have access', () => {
    authorizationServiceSpy.canAccessGoogleDrive.and.returnValue(false);
    fixture.detectChanges();
    const googleDriveIcon = fixture.nativeElement.querySelector('.fa-google-drive');
    expect(googleDriveIcon).toBeNull();
  })

  it('should show Google Drive icon if user is meant to have access', () => {
    authorizationServiceSpy.canAccessGoogleDrive.and.returnValue(true);
    fixture.detectChanges();
    const googleDriveIcon = fixture.nativeElement.querySelector('.fa-google-drive');
    expect(googleDriveIcon).toBeTruthy();
  })

  it('should not show Delete candidate option if user has not got editable access', () => {
    authorizationServiceSpy.isEditableCandidate.and.returnValue(false);
    fixture.detectChanges();
    const deleteIcon = fixture.nativeElement.querySelector('.fa-user-xmark');
    expect(deleteIcon).toBeNull();
  })

  it('should show Delete candidate option if user has editable access', () => {
    authorizationServiceSpy.isEditableCandidate.and.returnValue(true);
    fixture.detectChanges();
    const deleteIcon = fixture.nativeElement.querySelector('.fa-user-xmark');
    expect(deleteIcon).toBeTruthy();
  })

  describe('deleteCandidate', () => {
    it('should open EditCandidateStatusComponent modal with status set to deleted', () => {
      mockModalService.open.and.returnValue({
        componentInstance: {},
        result: new Promise(() => {})
      } as any);

      component.deleteCandidate();

      expect(mockModalService.open).toHaveBeenCalledWith(EditCandidateStatusComponent);
      const modal = mockModalService.open.calls.mostRecent().returnValue;
      expect(modal.componentInstance.candidateStatus).toEqual(CandidateStatus.deleted);
    });

    it('should update the candidate status when the modal resolves', fakeAsync(() => {
      const info: UpdateCandidateStatusInfo = { status: CandidateStatus.deleted } as UpdateCandidateStatusInfo;
      mockModalService.open.and.returnValue({
        componentInstance: {},
        result: Promise.resolve(info)
      } as any);
      mockCandidateService.updateStatus.and.returnValue(of(null));
      mockCandidateService.getByNumber.and.returnValue(of(mockCandidate));

      component.deleteCandidate();
      tick();

      expect(mockCandidateService.updateStatus).toHaveBeenCalledWith({
        candidateIds: [mockCandidate.id],
        info: info
      });
    }));

    it('should not throw when the modal is dismissed', fakeAsync(() => {
      mockModalService.open.and.returnValue({
        componentInstance: {},
        result: Promise.reject('dismissed')
      } as any);

      expect(() => component.deleteCandidate()).not.toThrow();
      tick();
    }));
  });

  describe('editCandidateStatus', () => {
    it('should open EditCandidateStatusComponent modal with the candidate\'s current status', () => {
      component.candidate = { ...mockCandidate, status: CandidateStatus.pending };
      mockModalService.open.and.returnValue({
        componentInstance: {},
        result: new Promise(() => {})
      } as any);

      component.editCandidateStatus();

      expect(mockModalService.open).toHaveBeenCalledWith(EditCandidateStatusComponent);
      const modal = mockModalService.open.calls.mostRecent().returnValue;
      expect(modal.componentInstance.candidateStatus).toEqual(CandidateStatus.pending);
    });
  });

});
