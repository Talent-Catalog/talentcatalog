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

import {ViewPostComponent} from "./view-post.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {AddReactionRequest, ReactionService} from "../../../services/reaction.service";
import {ChatPost} from "../../../model/chat";
import {MockChatPost} from "../../../MockData/MockChatPost";
import {of} from "rxjs";
import {Reaction} from "../../../model/reaction";
import {MOCK_REACTIONS} from "../../../MockData/MockReactions";
import {By} from "@angular/platform-browser";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {AuthenticationService} from "../../../services/authentication.service";
import {MockUser} from "../../../MockData/MockUser";
import {NgbTooltipModule} from "@ng-bootstrap/ng-bootstrap";

describe('ViewPostComponent', () => {
  let component: ViewPostComponent;
  let fixture: ComponentFixture<ViewPostComponent>;
  let reactionServiceSpy: jasmine.SpyObj<ReactionService>;
  let authenticationService: jasmine.SpyObj<AuthenticationService>;

  beforeEach(async () => {
    const spy = jasmine.createSpyObj('ReactionService', ['addReaction', 'modifyReaction']);
    const authSpy = jasmine.createSpyObj('AuthenticationService', ['getLoggedInUser']);

    await TestBed.configureTestingModule({
      declarations: [ ViewPostComponent ],
      imports: [HttpClientTestingModule,NgbTooltipModule],
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


  it('should handle emoji selection and update reactions', () => {
    const event = { emoji: { native: '😊' } };


    reactionServiceSpy.addReaction.and.returnValue(of(MOCK_REACTIONS));

    component.onSelectEmoji(event);

    expect(component.reactionPickerVisible).toBeFalse();
    expect(reactionServiceSpy.addReaction).toHaveBeenCalledWith(component.post.id, { emoji: '😊' } as AddReactionRequest);
    expect(component.post.reactions.length).toBe(3);
    expect(component.post.reactions[0].emoji).toBe('😊');
  });

  it('should update reaction when a reaction button is clicked', () => {
    const reaction: Reaction = MOCK_REACTIONS[0];
    reactionServiceSpy.modifyReaction.and.returnValue(of(MOCK_REACTIONS));
    component.post = { id: 2 } as ChatPost;;

    component.onSelectReaction(reaction);

    expect(reactionServiceSpy.modifyReaction).toHaveBeenCalledWith(component.post.id, reaction.id);
    expect(component.post.reactions.length).toBe(3);
    expect(component.post.reactions[0].users.length).toBe(2);
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

    const content = fixture.debugElement.query(By.css('.message-text.html'));
    expect(content).toBeTruthy();

    component.post.content = 'This is plain text content.';
    fixture.detectChanges();

    const textContent = fixture.debugElement.query(By.css('.message-text.text'));
    expect(textContent).toBeTruthy();
  });

  it('should return correct createdBy string', () => {
    const createdByString = component.createdBy;
    expect(createdByString).toBe('John Doe (MP)');
  });
});
