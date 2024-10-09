import {ViewPostComponent} from "./view-post.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {AddReactionRequest, ReactionService} from "../../../services/reaction.service";
import {ChatPost} from "../../../model/chat";
import {MockChatPost} from "../../../MockData/MockChatPost";
import {of, throwError} from "rxjs";
import {Reaction} from "../../../model/reaction";
import {MOCK_REACTIONS} from "../../../MockData/MockReactions";
import {By} from "@angular/platform-browser";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {LocalStorageModule} from "angular-2-local-storage";
import {AuthenticationService} from "../../../services/authentication.service";
import {MockUser} from "../../../MockData/MockUser";
import {NgbTooltipModule} from "@ng-bootstrap/ng-bootstrap";

describe('ViewPostComponent', () => {
  let component: ViewPostComponent;
  let fixture: ComponentFixture<ViewPostComponent>;
  let reactionServiceSpy: jasmine.SpyObj<ReactionService>;
  let authenticationService: jasmine.SpyObj<AuthenticationService>;

  beforeEach(async () => {
    const spy = jasmine.createSpyObj('ReactionService', ['addReaction', 'modifyReaction','subscribeToReactions']);
    const authSpy = jasmine.createSpyObj('AuthenticationService', ['getLoggedInUser']);

    await TestBed.configureTestingModule({
      declarations: [ ViewPostComponent ],
      imports: [HttpClientTestingModule,NgbTooltipModule,
        LocalStorageModule.forRoot({}),
      ],
      providers: [
        { provide: ReactionService, useValue: spy },
        { provide: AuthenticationService, useValue: authSpy }
      ]
    })
    .compileComponents();

    reactionServiceSpy = TestBed.inject(ReactionService) as jasmine.SpyObj<ReactionService>;
    authenticationService = TestBed.inject(AuthenticationService) as jasmine.SpyObj<AuthenticationService>;
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ViewPostComponent);
    component = fixture.componentInstance;
    component.post = new MockChatPost();
    authenticationService.getLoggedInUser.and.returnValue((new MockUser()));
    reactionServiceSpy.subscribeToReactions.and.returnValue(of(MOCK_REACTIONS));
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should toggle reaction picker visibility and set position on reaction button click', () => {
    const event = { clientY: 300, clientX: 500 } as MouseEvent;
    component.onClickReactionBtn(event);

    // Calculate the expected position based on the provided client position
    let expectedXPos = event.clientX - 370;
    let expectedYPos = event.clientY;

    // Adjust Y position based on the conditions in onClickReactionBtn method
    if (event.clientY < 510 && window.innerHeight - event.clientY < 425) {
      expectedYPos = event.clientY - 213;
    } else if (window.innerHeight - event.clientY < 425 && event.clientY > 510) {
      expectedYPos = event.clientY - 425;
    }

    expect(component.reactionPickerVisible).toBeTrue();
    expect(component.reactionPickerXPos).toBe(expectedXPos);
    expect(component.reactionPickerYPos).toBe(expectedYPos);

    // Simulate another click to toggle off
    component.onClickReactionBtn(event);
    expect(component.reactionPickerVisible).toBeFalse();
  });


  it('should handle emoji selection and call addReaction', () => {
    const event = { emoji: { native: '😊' } };

    component.onSelectEmoji(event);

    expect(component.reactionPickerVisible).toBeFalse();
    expect(reactionServiceSpy.addReaction).toHaveBeenCalledWith(component.post.id, { emoji: '😊' } as AddReactionRequest);
  });

  it('should handle reaction selection and call modifyReaction', () => {
    const reaction: Reaction = MOCK_REACTIONS[0];

    component.onSelectReaction(reaction);

    expect(reactionServiceSpy.modifyReaction).toHaveBeenCalledWith(component.post.id, reaction.id);
  });

  it('should subscribe to reaction updates and update reactions on new data', () => {
    component.ngOnInit();

    expect(reactionServiceSpy.subscribeToReactions).toHaveBeenCalledWith(component.post.id);
    expect(component.post.reactions.length).toBe(3);
    expect(component.post.reactions[0].users.length).toBe(2);
  });

  it('should handle error when subscribing to reaction updates', () => {
    const error = new Error('WebSocket error');
    reactionServiceSpy.subscribeToReactions.and.returnValue(throwError((error)));
    spyOn(console, 'error');

    component.ngOnInit();

    expect(console.error).toHaveBeenCalledWith('Error receiving reaction updates:', error);
  });

  it('should close reaction picker when current post changes', () => {
    const newPost = { id: 2 } as ChatPost;
    component.currentPost = newPost;
    fixture.detectChanges();

    component.ngOnChanges({
      currentPost: {
        currentValue: newPost,
        previousValue: component.post,
        firstChange: false,
        isFirstChange: () => false
      }
    });

    expect(component.reactionPickerVisible).toBeFalse();
    expect(component.isCurrentPost).toBeFalse();
  });


  it('should render post content as HTML or text based on isHtml function', () => {
    component.post.content = '<p>This is <strong>HTML</strong> content.</p>';
    fixture.detectChanges();

    const content = fixture.debugElement.query(By.css('.content.html'));
    expect(content).toBeTruthy();

    component.post.content = 'This is plain text content.';
    fixture.detectChanges();

    const textContent = fixture.debugElement.query(By.css('.content.text'));
    expect(textContent).toBeTruthy();
  });

  it('should return correct createdBy string', () => {
    const createdByString = component.createdBy;
    expect(createdByString).toBe('John Doe (MP)');
  });
});
