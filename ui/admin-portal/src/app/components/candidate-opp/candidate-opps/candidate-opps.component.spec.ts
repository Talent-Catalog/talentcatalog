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

import {CandidateOppsComponent} from "./candidate-opps.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {ChatService} from "../../../services/chat.service";
import {AuthorizationService} from "../../../services/authorization.service";
import {CandidateOpportunityService} from "../../../services/candidate-opportunity.service";
import {SalesforceService} from "../../../services/salesforce.service";
import {CandidateOpportunity} from "../../../model/candidate-opportunity";
import {UntypedFormBuilder, FormsModule, ReactiveFormsModule} from "@angular/forms";
import {LOCALE_ID, SimpleChange} from "@angular/core";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {CommonModule} from "@angular/common";
import {mockCandidateOpportunity} from "../../../MockData/MockCandidateOpportunity";
import {LocalStorageService} from "../../../services/local-storage.service";

describe('CandidateOppsComponent', () => {
  let component: CandidateOppsComponent;
  let fixture: ComponentFixture<CandidateOppsComponent>;
  let mockChatService: jasmine.SpyObj<ChatService>;
  let mockAuthService: jasmine.SpyObj<AuthorizationService>;
  let mockLocalStorageService: jasmine.SpyObj<LocalStorageService>;
  let mockOppService: jasmine.SpyObj<CandidateOpportunityService>;
  let mockSalesforceService: jasmine.SpyObj<SalesforceService>;
  let formBuilder: UntypedFormBuilder;

  beforeEach(async () => {
    const chatServiceSpy = jasmine.createSpyObj('ChatService', ['getOrCreate']);
    const authServiceSpy = jasmine.createSpyObj('AuthorizationService', ['']);
    const localStorageServiceSpy = jasmine.createSpyObj('LocalStorageService', ['get']);
    const oppServiceSpy = jasmine.createSpyObj('CandidateOpportunityService', ['']);
    const salesforceServiceSpy = jasmine.createSpyObj('SalesforceService', ['']);
    await TestBed.configureTestingModule({
      declarations: [CandidateOppsComponent],
      imports: [HttpClientTestingModule,ReactiveFormsModule,FormsModule,CommonModule],
      providers: [
        UntypedFormBuilder,
        { provide: ChatService, useValue: chatServiceSpy },
        { provide: AuthorizationService, useValue: authServiceSpy },
        { provide: LocalStorageService, useValue: localStorageServiceSpy },
        { provide: CandidateOpportunityService, useValue: oppServiceSpy },
        { provide: SalesforceService, useValue: salesforceServiceSpy },
        { provide: LOCALE_ID, useValue: 'en-US' }
      ]
    }).compileComponents();

    mockChatService = TestBed.inject(ChatService) as jasmine.SpyObj<ChatService>;
    mockAuthService = TestBed.inject(AuthorizationService) as jasmine.SpyObj<AuthorizationService>;
    mockLocalStorageService = TestBed.inject(LocalStorageService) as jasmine.SpyObj<LocalStorageService>;
    mockOppService = TestBed.inject(CandidateOpportunityService) as jasmine.SpyObj<CandidateOpportunityService>;
    mockSalesforceService = TestBed.inject(SalesforceService) as jasmine.SpyObj<SalesforceService>;
    formBuilder = TestBed.inject(UntypedFormBuilder); // Inject FormBuilder
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CandidateOppsComponent);
    component = fixture.componentInstance;

    // Initialize form group
    component.searchForm = formBuilder.group({
      keyword: [''], // Initialize with default value
      myOppsOnly: [false],
      showClosedOpps: [false],
      showInactiveOpps:[false],
      overdueOppsOnly:[false],
      withUnreadMessages:[false],
      selectedStages:[[]]
      // Add more form controls here
    });

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
  //
  it('should initialize form with default values and controls', () => {
    // Act
    const formControls = component.searchForm.controls;
    // Check if form is defined
    expect(component.searchForm).toBeDefined();
    // Check if form controls are initialized
    expect(formControls.keyword).toBeDefined();
    expect(formControls.myOppsOnly).toBeDefined();
    expect(formControls.showClosedOpps).toBeDefined();
    expect(formControls.showInactiveOpps).toBeDefined();
    expect(formControls.overdueOppsOnly).toBeDefined();
    expect(formControls.withUnreadMessages).toBeDefined();
    expect(formControls.selectedStages).toBeDefined();

    // Check if form controls have default values
    expect(formControls.keyword.value).toBe('');
    expect(formControls.myOppsOnly.value).toBe(false);
    expect(formControls.showClosedOpps.value).toBe(false);
    expect(formControls.showInactiveOpps.value).toBe(false);
    expect(formControls.overdueOppsOnly.value).toBe(false);
    expect(formControls.withUnreadMessages.value).toBe(false);
    expect(formControls.selectedStages.value).toEqual([]);
  });
  it('should update opps and fetch chats when candidateOpps change', () => {
    const opps: CandidateOpportunity[] = [mockCandidateOpportunity];
    component.candidateOpps = opps;

    spyOn(component as any, 'fetchChats'); // Explicitly type fetchChats as any
    const simpleChange: SimpleChange = {
      previousValue: opps,
      currentValue: opps,
      firstChange: false,
      isFirstChange: () => true,
    };
    component.ngOnChanges({ candidateOpps: simpleChange });
    expect(component.opps).toEqual(opps);
    expect(component['fetchChats']).toHaveBeenCalled();
  });
  it('should correctly identify if an opportunity is overdue', () => {
    const opp: CandidateOpportunity = mockCandidateOpportunity;

    const isOverdue = component.isOverdue(opp);
    expect(isOverdue).toBeTrue();
  });
});
