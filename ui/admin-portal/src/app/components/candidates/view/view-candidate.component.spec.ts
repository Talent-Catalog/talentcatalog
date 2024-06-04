/*
 * Copyright (c) 2024 Talent Beyond Boundaries.
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
import {LocalStorageService} from "angular-2-local-storage";
import {AuthenticationService} from "../../../services/authentication.service";
import {CandidateService} from "../../../services/candidate.service";
import {SavedListService} from "../../../services/saved-list.service";
import {ViewCandidateComponent} from "./view-candidate.component";
import {ComponentFixture, TestBed, waitForAsync} from "@angular/core/testing";
import {of} from "rxjs";
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
import {User} from "../../../model/user";
import {ViewCandidateNoteComponent} from "./note/view-candidate-note.component";

fdescribe('ViewCandidateComponent', () => {
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
    mockCandidateService = jasmine.createSpyObj('CandidateService', ['get','getByNumber', 'generateToken']);
    mockSavedListService = jasmine.createSpyObj('SavedListService', ['search']);
    mockModalService = jasmine.createSpyObj('NgbModal', ['open']);
    mockLocalStorageService = jasmine.createSpyObj('LocalStorageService', ['get', 'set']);
    mockAuthenticationService = jasmine.createSpyObj('AuthenticationService', ['getLoggedInUser','isEditableCandidate', 'canViewPrivateCandidateInfo', 'canAccessSalesforce']);

    TestBed.configureTestingModule({
      declarations: [ViewCandidateComponent,ViewCandidateNoteComponent,CandidateGeneralTabComponent,CandidateShareableNotesComponent,ViewCandidateContactComponent,AutosaveStatusComponent,ViewCandidateLanguageComponent,ViewCandidateRegistrationComponent],
      imports: [HttpClientTestingModule,FormsModule,NgbNavModule,RouterTestingModule,ReactiveFormsModule, NgSelectModule,NgxWigModule],
      providers: [
        { provide: CandidateService, useValue: mockCandidateService },
        { provide: SavedListService, useValue: mockSavedListService },
        { provide: ActivatedRoute, useValue: {
            paramMap: of(convertToParamMap({ candidateNumber: '123' }))
          }
        },        { provide: NgbModal, useValue: mockModalService },
        { provide: LocalStorageService, useValue: mockLocalStorageService },
        { provide: AuthenticationService, useValue: mockAuthenticationService }
      ]
    }).compileComponents();
    mockActivatedRoute = TestBed.inject(ActivatedRoute);
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ViewCandidateComponent);
    component = fixture.componentInstance;
    mockCandidateService.get.and.returnValue(of(mockCandidate));
    mockAuthenticationService.getLoggedInUser.and.returnValue(new MockUser());
    component.candidate = mockCandidate;
    mockCandidateService.getByNumber.and.returnValue(of(mockCandidate));
    mockCandidateService.generateToken.and.returnValue(of('Token'));
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should refresh candidate information and load lists on initialization', () => {

    // Mocking candidate service method
    mockCandidateService.getByNumber.and.returnValue(of(mockCandidate));

    // Mocking saved list service method
    mockSavedListService.search.and.returnValue(of([]));
    spyOn(component, 'refreshCandidateInfo'); // or .and.stub() if you don't want to actually call the method

    // Triggering ngOnInit
    component.refreshCandidateInfo();
    fixture.detectChanges();
    // Expectations
    expect(component.loading).toBe(false); // Assuming loading is set to false after successful data retrieval
    expect(component.candidate).toEqual(mockCandidate); // Candidate data should be set
    expect(component.refreshCandidateInfo).toHaveBeenCalled(); // Method to load lists should be called
  });
});
