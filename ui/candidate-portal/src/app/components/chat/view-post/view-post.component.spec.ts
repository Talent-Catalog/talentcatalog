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

import {Component, EventEmitter, Input, Output} from '@angular/core';
import {ComponentFixture, fakeAsync, TestBed, tick} from '@angular/core/testing';
import {NgbTooltipModule} from '@ng-bootstrap/ng-bootstrap';
import {of} from 'rxjs';

import {ViewPostComponent} from './view-post.component';
import {ReactionService} from '../../../services/reaction.service';
import {AuthenticationService} from '../../../services/authentication.service';
import {ChatPost} from '../../../model/chat';
import {Reaction} from '../../../model/reaction';

@Component({
  selector: 'app-preview-link',
  template: ''
})
class PreviewLinkStubComponent {
  @Input() linkPreview: unknown;
  @Input() userIsPostAuthor: boolean;
}

@Component({
  selector: 'emoji-mart',
  template: ''
})
class EmojiMartStubComponent {
  @Input() emoji: string;
  @Input() title: string;
  @Input() autoFocus: boolean;

  @Output() emojiClick = new EventEmitter<unknown>();
}

describe('ViewPostComponent', () => {
  let component: ViewPostComponent;
  let fixture: ComponentFixture<ViewPostComponent>;

  let reactionServiceSpy: jasmine.SpyObj<ReactionService>;
  let authenticationServiceSpy: jasmine.SpyObj<AuthenticationService>;

  const loggedInUser = {
    id: 1,
    firstName: 'Ehsan',
    lastName: 'Ehrari'
  };

  const otherUser = {
    id: 2,
    firstName: 'Jane',
    lastName: 'Doe'
  };

  const createPost = (
    createdBy = loggedInUser
  ): ChatPost => ({
    id: 100,
    content: '<p>Hello</p>',
    createdBy,
    createdDate: new Date(),
    reactions: [],
    linkPreviews: []
  } as ChatPost);

  beforeEach(async () => {
    reactionServiceSpy = jasmine.createSpyObj<ReactionService>(
      'ReactionService',
      [
        'addReaction',
        'modifyReaction'
      ]
    );

    authenticationServiceSpy =
      jasmine.createSpyObj<AuthenticationService>(
        'AuthenticationService',
        ['getLoggedInUser']
      );

    authenticationServiceSpy.getLoggedInUser.and.returnValue(
      loggedInUser as any
    );

    reactionServiceSpy.addReaction.and.returnValue(of([]));
    reactionServiceSpy.modifyReaction.and.returnValue(of([]));

    await TestBed.configureTestingModule({
      declarations: [
        ViewPostComponent,
        PreviewLinkStubComponent,
        EmojiMartStubComponent
      ],
      imports: [
        NgbTooltipModule
      ],
      providers: [
        {
          provide: ReactionService,
          useValue: reactionServiceSpy
        },
        {
          provide: AuthenticationService,
          useValue: authenticationServiceSpy
        }
      ]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ViewPostComponent);
    component = fixture.componentInstance;

    component.post = createPost();

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should identify the logged-in user as the post author', () => {
    expect(component.userIsPostAuthor).toBeTrue();
  });

  it('should identify a different user as not being the post author', () => {
    const otherFixture = TestBed.createComponent(ViewPostComponent);
    const otherComponent = otherFixture.componentInstance;

    otherComponent.post = createPost(otherUser);

    otherFixture.detectChanges();

    expect(otherComponent.userIsPostAuthor).toBeFalse();
  });

  it('should extract the user abbreviation', () => {
    expect(
      component.getUserAbbreviation('Ehsan Ehrari (CTI)')
    ).toBe('CTI');
  });

  it('should return null when there is no abbreviation', () => {
    expect(
      component.getUserAbbreviation('Ehsan Ehrari')
    ).toBeNull();
  });

  it('should show the reaction picker', fakeAsync(() => {
    spyOn(
      component.thisPost.nativeElement,
      'scrollIntoView'
    );

    component.toggleReactionPicker();
    tick();

    expect(component.reactionPickerVisible).toBeTrue();
    expect(
      component.thisPost.nativeElement.scrollIntoView
    ).toHaveBeenCalledWith({
      behavior: 'smooth'
    });
  }));

  it('should hide the reaction picker when toggled again', () => {
    component.reactionPickerVisible = true;

    component.toggleReactionPicker();

    expect(component.reactionPickerVisible).toBeFalse();
  });

  it('should not toggle the reaction picker in read-only mode', () => {
    component.readOnly = true;

    component.toggleReactionPicker();

    expect(component.reactionPickerVisible).toBeFalse();
  });

  it('should add a selected emoji reaction', () => {
    const updatedReactions = [
      {
        id: 10,
        emoji: '👍',
        users: []
      }
    ] as Reaction[];

    reactionServiceSpy.addReaction.and.returnValue(
      of(updatedReactions)
    );

    component.reactionPickerVisible = true;

    component.onSelectEmoji({
      emoji: {
        native: '👍'
      }
    });

    expect(component.reactionPickerVisible).toBeFalse();

    expect(
      reactionServiceSpy.addReaction
    ).toHaveBeenCalledWith(
      component.post.id,
      {
        emoji: '👍'
      }
    );

    expect(component.post.reactions).toEqual(updatedReactions);
  });

  it('should not add an emoji reaction in read-only mode', () => {
    component.readOnly = true;

    component.onSelectEmoji({
      emoji: {
        native: '👍'
      }
    });

    expect(
      reactionServiceSpy.addReaction
    ).not.toHaveBeenCalled();
  });

  it('should modify an existing reaction', () => {
    const reaction = {
      id: 20,
      emoji: '❤️',
      users: []
    } as Reaction;

    const updatedReactions = [
      reaction
    ];

    reactionServiceSpy.modifyReaction.and.returnValue(
      of(updatedReactions)
    );

    component.onSelectReaction(reaction);

    expect(
      reactionServiceSpy.modifyReaction
    ).toHaveBeenCalledWith(
      component.post.id,
      reaction.id
    );

    expect(component.post.reactions).toEqual(updatedReactions);
  });

  it('should open a clicked image in a new tab', () => {
    spyOn(window, 'open');

    const image = document.createElement('img');
    image.setAttribute('src', 'https://example.com/image.jpg');

    component.onClick({
      target: image
    });

    expect(window.open).toHaveBeenCalledWith(
      'https://example.com/image.jpg',
      '_blank'
    );
  });

  it('should not open a new tab when a non-image element is clicked', () => {
    spyOn(window, 'open');

    const element = document.createElement('div');

    component.onClick({
      target: element
    });

    expect(window.open).not.toHaveBeenCalled();
  });
});
