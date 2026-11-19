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

import type {MockedObject} from "vitest";
import {Component, EventEmitter, Input, Output, ChangeDetectionStrategy} from '@angular/core';
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
    template: '',
    changeDetection: ChangeDetectionStrategy.Eager,
    standalone: false
})
class PreviewLinkStubComponent {
    @Input()
    linkPreview: unknown;
    @Input()
    userIsPostAuthor: boolean;
}

@Component({
    selector: 'emoji-mart',
    template: '',
    changeDetection: ChangeDetectionStrategy.Eager,
    standalone: false
})
class EmojiMartStubComponent {
    @Input()
    emoji: string;
    @Input()
    title: string;
    @Input()
    autoFocus: boolean;

    @Output()
    emojiClick = new EventEmitter<unknown>();
}

describe('ViewPostComponent', () => {
    let component: ViewPostComponent;
    let fixture: ComponentFixture<ViewPostComponent>;

    let reactionServiceSpy: MockedObject<ReactionService>;
    let authenticationServiceSpy: MockedObject<AuthenticationService>;

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

    const createPost = (createdBy = loggedInUser): ChatPost => ({
        id: 100,
        content: '<p>Hello</p>',
        createdBy,
        createdDate: new Date(),
        reactions: [],
        linkPreviews: []
    } as ChatPost);

    beforeEach(async () => {
        reactionServiceSpy = {
            addReaction: vi.fn().mockName("ReactionService.addReaction"),
            modifyReaction: vi.fn().mockName("ReactionService.modifyReaction")
        } as unknown as MockedObject<ReactionService>;

        authenticationServiceSpy = {
            getLoggedInUser: vi.fn().mockName("AuthenticationService.getLoggedInUser")
        } as unknown as MockedObject<AuthenticationService>;

        authenticationServiceSpy.getLoggedInUser.mockReturnValue(loggedInUser as any);

        reactionServiceSpy.addReaction.mockReturnValue(of([]));
        reactionServiceSpy.modifyReaction.mockReturnValue(of([]));

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
        expect(component.userIsPostAuthor).toBe(true);
    });

    it('should identify a different user as not being the post author', () => {
        const otherFixture = TestBed.createComponent(ViewPostComponent);
        const otherComponent = otherFixture.componentInstance;

        otherComponent.post = createPost(otherUser);

        otherFixture.detectChanges();

        expect(otherComponent.userIsPostAuthor).toBe(false);
    });

    it('should extract the user abbreviation', () => {
        expect(component.getUserAbbreviation('Ehsan Ehrari (CTI)')).toBe('CTI');
    });

    it('should return null when there is no abbreviation', () => {
        expect(component.getUserAbbreviation('Ehsan Ehrari')).toBeNull();
    });

    it('should show the reaction picker', fakeAsync(() => {
        vi.spyOn(component.thisPost.nativeElement, 'scrollIntoView').mockReturnValue(undefined);

        component.toggleReactionPicker();
        tick();

        expect(component.reactionPickerVisible).toBe(true);
        expect(component.thisPost.nativeElement.scrollIntoView).toHaveBeenCalledWith({
            behavior: 'smooth'
        });
    }));

    it('should hide the reaction picker when toggled again', () => {
        component.reactionPickerVisible = true;

        component.toggleReactionPicker();

        expect(component.reactionPickerVisible).toBe(false);
    });

    it('should not toggle the reaction picker in read-only mode', () => {
        component.readOnly = true;

        component.toggleReactionPicker();

        expect(component.reactionPickerVisible).toBe(false);
    });

    it('should add a selected emoji reaction', () => {
        const updatedReactions = [
            {
                id: 10,
                emoji: '👍',
                users: []
            }
        ] as Reaction[];

        reactionServiceSpy.addReaction.mockReturnValue(of(updatedReactions));

        component.reactionPickerVisible = true;

        component.onSelectEmoji({
            emoji: {
                native: '👍'
            }
        });

        expect(component.reactionPickerVisible).toBe(false);

        expect(reactionServiceSpy.addReaction).toHaveBeenCalledWith(component.post.id, {
            emoji: '👍'
        });

        expect(component.post.reactions).toEqual(updatedReactions);
    });

    it('should not add an emoji reaction in read-only mode', () => {
        component.readOnly = true;

        component.onSelectEmoji({
            emoji: {
                native: '👍'
            }
        });

        expect(reactionServiceSpy.addReaction).not.toHaveBeenCalled();
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

        reactionServiceSpy.modifyReaction.mockReturnValue(of(updatedReactions));

        component.onSelectReaction(reaction);

        expect(reactionServiceSpy.modifyReaction).toHaveBeenCalledWith(component.post.id, reaction.id);

        expect(component.post.reactions).toEqual(updatedReactions);
    });

    it('should open a clicked image in a new tab', () => {
        vi.spyOn(window, 'open').mockReturnValue(undefined);

        const image = document.createElement('img');
        image.setAttribute('src', 'https://example.com/image.jpg');

        component.onClick({
            target: image
        });

        expect(window.open).toHaveBeenCalledWith('https://example.com/image.jpg', '_blank');
    });

    it('should not open a new tab when a non-image element is clicked', () => {
        vi.spyOn(window, 'open').mockReturnValue(undefined);

        const element = document.createElement('div');

        component.onClick({
            target: element
        });

        expect(window.open).not.toHaveBeenCalled();
    });
});
