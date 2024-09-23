import {ViewCandidateOppComponent} from "./view-candidate-opp.component";
import {ComponentFixture, fakeAsync, TestBed, tick} from "@angular/core/testing";
import {
  NgbModal,
  NgbNavChangeEvent,
  NgbNavModule
} from "@ng-bootstrap/ng-bootstrap";
import {FileSelectorComponent} from "../../util/file-selector/file-selector.component";
import {CandidateOpportunityService} from "../../../services/candidate-opportunity.service";
import {AuthenticationService} from "../../../services/authentication.service";
import {ChatService} from "../../../services/chat.service";
import {of} from "rxjs";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {LocalStorageModule} from "angular-2-local-storage";
import {mockCandidateOpportunity} from "../../../MockData/MockCandidateOpportunity";
import {RouterLinkStubDirective} from "../../login/login.component.spec";
import {MockPartner} from "../../../MockData/MockPartner";
import {
  OpportunityStageNextStepComponent
} from "../../util/opportunity-stage-next-step/opportunity-stage-next-step.component";
import {MockJobChat} from "../../../MockData/MockJobChat";
import {ChatReadStatusComponent} from "../../chat/chat-read-status/chat-read-status.component";
import {CUSTOM_ELEMENTS_SCHEMA} from "@angular/core";
describe('ViewCandidateOppComponent', () => {
  let component: ViewCandidateOppComponent;
  let fixture: ComponentFixture<ViewCandidateOppComponent>;
  let mockModalService: any;
  let mockCandidateOpportunityService: jasmine.SpyObj<CandidateOpportunityService>;;
  let mockAuthService: any;
  let chatService: jasmine.SpyObj<ChatService>;
  beforeEach(async () => {
    mockModalService = jasmine.createSpyObj('NgbModal', ['open']);
    mockCandidateOpportunityService = jasmine.createSpyObj('CandidateOpportunityService', ['uploadOffer']);
    mockAuthService = jasmine.createSpyObj('AuthenticationService', ['getLoggedInUser']);
    chatService = jasmine.createSpyObj('ChatService', ['getOrCreate','getChatIsRead$']);
    chatService.getOrCreate.and.callThrough();
    mockCandidateOpportunityService.uploadOffer.and.callThrough();

    await TestBed.configureTestingModule({
      declarations: [ViewCandidateOppComponent,ChatReadStatusComponent,RouterLinkStubDirective,OpportunityStageNextStepComponent],
      imports: [HttpClientTestingModule,NgbNavModule, LocalStorageModule.forRoot({})],
      providers: [
        { provide: NgbModal, useValue: mockModalService },
        { provide: CandidateOpportunityService, useValue: mockCandidateOpportunityService },
        { provide: AuthenticationService, useValue: mockAuthService },
        { provide: ChatService, useValue: chatService }
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA]
    }).compileComponents();
    mockAuthService.getLoggedInUser.and.returnValue({ partner: new MockPartner()});
    chatService.getOrCreate.and.returnValue(of(new MockJobChat()));
    mockCandidateOpportunityService.uploadOffer.and.returnValue(of(mockCandidateOpportunity));
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ViewCandidateOppComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should fetch chats when opp input changes', fakeAsync(() => {
    const changes = {
      opp: {
        currentValue: mockCandidateOpportunity,
        previousValue: null,
        firstChange: true,
        isFirstChange: () => true
      }
    };
    component.opp = mockCandidateOpportunity;
    component.ngOnChanges(changes);
    tick();
    // Manually trigger change detection
    fixture.detectChanges();
    expect(component.loading).toBeFalsy();
  }));
  it('should initialize with default values and fetch chats', fakeAsync(() => {
    const changes = {
      opp: {
        currentValue: mockCandidateOpportunity,
        previousValue: null,
        firstChange: true,
        isFirstChange: () => true
      }
    };
    component.opp = mockCandidateOpportunity;
    component.ngOnChanges(changes);
    // Wait for asynchronous operations to complete
    tick();
    // Expectations
    expect(component.error).toBe(null);
    expect(component.candidateChat).toEqual(new MockJobChat());
    expect(component.candidateRecruitingChat).toEqual(new MockJobChat());
  }));

  it('should upload job offer successfully', fakeAsync(() => {
    // Mock file
    const mockFile = new File([''], 'job_offer.pdf');
    component.opp = mockCandidateOpportunity;
    const modalRef = {
      componentInstance: {
        maxFiles: 1,
      },
      result: Promise.resolve([mockFile]) // Mock result property to return a resolved promise
    };
    mockModalService.open.and.returnValue(modalRef);

    // Call the component method
    component.uploadOffer();
    tick();

    // Expectations
    expect(mockModalService.open).toHaveBeenCalledWith(FileSelectorComponent, jasmine.any(Object));
    expect(mockCandidateOpportunityService.uploadOffer).toHaveBeenCalledWith(mockCandidateOpportunity.id, jasmine.any(FormData));
    expect(component.saving).toBe(false);
  }));

  it('should update active tab ID when the user changes tabs', () => {
    const mockNavChangeEvent: NgbNavChangeEvent = { activeId:1, nextId: 'Progress', preventDefault: () => {} };
    // Trigger tab change
    component.onTabChanged(mockNavChangeEvent);
    // Expectations
    expect(component.activeTabId).toBe(mockNavChangeEvent.nextId);
  });
});
