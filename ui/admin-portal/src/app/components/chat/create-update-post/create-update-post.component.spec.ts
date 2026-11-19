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
import {NO_ERRORS_SCHEMA} from '@angular/core';
import {ComponentFixture, fakeAsync, TestBed, tick} from '@angular/core/testing';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {NgbModal, NgbModalRef} from '@ng-bootstrap/ng-bootstrap';
import {of, throwError} from 'rxjs';

import {CreateUpdatePostComponent} from './create-update-post.component';
import {RxStompService} from '../../../services/rx-stomp.service';
import {ChatPostService} from '../../../services/chat-post.service';
import {LinkPreviewService} from '../../../services/link-preview.service';
import {FileSelectorComponent} from '../../util/file-selector/file-selector.component';
import {BuildLinkComponent} from '../../../util/build-link/build-link.component';
import {JobChatType} from '../../../model/chat';
import {QuillModule} from "ngx-quill";
import {TranslateModule} from "@ngx-translate/core";
import {expect, vi} from 'vitest';


describe('CreateUpdatePostComponent', () => {
    let component: CreateUpdatePostComponent;
    let fixture: ComponentFixture<CreateUpdatePostComponent>;
    let modalService: MockedObject<NgbModal>;
    let rxStompService: MockedObject<RxStompService>;
    let chatPostService: MockedObject<ChatPostService>;
    let linkPreviewService: MockedObject<LinkPreviewService>;

    const createQuillMock = () => ({
        selection: { savedRange: { index: 3 } },
        theme: { tooltip: { show: vi.fn().mockName('show') } },
        insertText: vi.fn().mockName('insertText'),
        setSelection: vi.fn().mockName('setSelection'),
        getSelection: vi.fn().mockName('getSelection').mockReturnValue({ index: 2, length: 4 }),
        getFormat: vi.fn().mockName('getFormat').mockReturnValue({}),
        getText: vi.fn().mockName('getText').mockReturnValue('text'),
        deleteText: vi.fn().mockName('deleteText'),
        getLeaf: vi.fn().mockName('getLeaf').mockReturnValue([{ text: 'linked text' }, 2]),
        getBounds: vi.fn().mockName('getBounds').mockReturnValue({ left: 10, bottom: 5 }),
        removeFormat: vi.fn().mockName('removeFormat'),
        hasFocus: vi.fn().mockName('hasFocus').mockReturnValue(true)
    }) as any;

    const modalRef = (result: Promise<any>): NgbModalRef => ({
        componentInstance: {},
        result,
        close: vi.fn().mockName('close'),
        dismiss: vi.fn().mockName('dismiss')
    } as any);

    beforeEach(async () => {
        modalService = ({
            open: vi.fn().mockName("NgbModal.open")
        } as unknown as typeof modalService);
        rxStompService = ({
            publish: vi.fn().mockName("RxStompService.publish")
        } as unknown as typeof rxStompService);
        chatPostService = ({
            uploadFile: vi.fn().mockName("ChatPostService.uploadFile")
        } as unknown as typeof chatPostService);
        linkPreviewService = ({
            buildLinkPreview: vi.fn().mockName("LinkPreviewService.buildLinkPreview")
        } as unknown as typeof linkPreviewService);

        await TestBed.configureTestingModule({
            declarations: [CreateUpdatePostComponent],
            imports: [
                ReactiveFormsModule,
                FormsModule,
                QuillModule.forRoot(),
                TranslateModule.forRoot()
            ],
            providers: [
                { provide: NgbModal, useValue: modalService },
                { provide: RxStompService, useValue: rxStompService },
                { provide: ChatPostService, useValue: chatPostService },
                { provide: LinkPreviewService, useValue: linkPreviewService }
            ],
            schemas: [NO_ERRORS_SCHEMA]
        }).compileComponents();

        fixture = TestBed.createComponent(CreateUpdatePostComponent);
        component = fixture.componentInstance;
        component.quillEditorRef = createQuillMock();
        fixture.detectChanges();
    });

    it('should create and initialise the form and link regexp', () => {
        expect(component).toBeTruthy();
        expect(component.contentControl).toBeTruthy();
        expect(component.contentControl.invalid).toBe(true);
        expect(component.regexpLink).toBeDefined();

        component.contentControl.setValue('Hello');
        expect(component.contentControl.valid).toBe(true);
    });

    it('should store the editor and disable the native Quill tooltip', () => {
        const quill = createQuillMock();
        const originalShow = quill.theme.tooltip.show;

        component.editorCreated(quill);

        expect(component.quillEditorRef).toBe(quill);
        expect(quill.theme.tooltip.show).not.toBe(originalShow);
        expect(() => quill.theme.tooltip.show()).not.toThrow();
    });

    it('should not send when no chat is supplied', () => {
        component.chat = null;
        component.contentControl.setValue('content');

        component.onSend();

        expect(rxStompService.publish).not.toHaveBeenCalled();
        expect(component.contentControl.value).toBe('content');
    });

    it('should send a post, exclude blocked previews and clear the form', () => {
        component.chat = { id: 7, type: JobChatType.CandidateProspect } as any;
        component.contentControl.setValue('content');
        component.linkPreviews = [
            { url: 'https://one.test', domain: 'one.test', blocked: false },
            { url: 'https://two.test', domain: 'two.test', blocked: true }
        ] as any;

        component.onSend();

        expect(rxStompService.publish).toHaveBeenCalledWith({
            destination: '/app/chat/7',
            body: JSON.stringify({
                content: 'content',
                linkPreviews: [component.linkPreviews[0]]
            })
        });
        expect(component.contentControl.value).toBeNull();
    });

    it('should send an empty preview array unchanged', () => {
        component.chat = { id: 8 } as any;
        component.contentControl.setValue('hello');
        component.linkPreviews = [];

        component.onSend();

        expect(rxStompService.publish).toHaveBeenCalledWith(expect.objectContaining({
            body: JSON.stringify({ content: 'hello', linkPreviews: [] })
        }));
    });

    it('should configure the file modal and upload the first selected file', fakeAsync(() => {
        const file = new File(['abc'], 'test.txt');
        const ref = modalRef(Promise.resolve([file]));
        modalService.open.mockReturnValue(ref);
        chatPostService.uploadFile.mockReturnValue(of({ url: 'https://file.test' } as any));
        component.chat = { id: 4 } as any;

        component.uploadFile();
        tick();

        expect(modalService.open).toHaveBeenCalledWith(FileSelectorComponent, {
            centered: true,
            backdrop: 'static'
        });
        expect(ref.componentInstance.maxFiles).toBe(1);
        expect(ref.componentInstance.closeButtonLabel).toBe('Upload');
        expect(ref.componentInstance.title).toBe('Select file to upload');
        expect(chatPostService.uploadFile).toHaveBeenCalledWith(4, expect.any(FormData));
        expect((component.quillEditorRef as any).insertText)
            .toHaveBeenCalledWith(3, 'link to file', 'link', 'https://file.test', 'user');
        expect(component.error).toBeNull();
        expect(component.saving).toBe(false);
    }));

    it('should not upload when the file modal returns no files', fakeAsync(() => {
        modalService.open.mockReturnValue(modalRef(Promise.resolve([])));

        component.uploadFile();
        tick();

        expect(chatPostService.uploadFile).not.toHaveBeenCalled();
    }));

    it('should ignore dismissal of the file modal', fakeAsync(() => {
        modalService.open.mockReturnValue(modalRef(Promise.reject('dismissed')));

        component.uploadFile();
        tick();

        expect(chatPostService.uploadFile).not.toHaveBeenCalled();
    }));

    it('should expose upload errors and clear saving state', fakeAsync(() => {
        const error = new Error('upload failed');
        const file = new File(['abc'], 'test.txt');

        modalService.open.mockReturnValue(modalRef(Promise.resolve([file])));

        chatPostService.uploadFile.mockReturnValue(throwError(error));

        component.chat = { id: 5 } as any;

        component.uploadFile();
        tick();

        expect(component.error).toBe(error);
        expect(component.saving).toBe(false);
    }));
    it('should insert an emoji and place the caret after it', () => {
        component.emojiPickerVisible = true;

        component.onSelectEmoji({ emoji: { native: '😊' } });

        expect(component.emojiPickerVisible).toBe(false);
        expect((component.quillEditorRef as any).insertText).toHaveBeenCalledWith(3, '😊', 'user');
        expect((component.quillEditorRef as any).setSelection).toHaveBeenCalledWith(5, 0);
    });

    it('should toggle the emoji picker and only refocus when closing', () => {
        component.emojiPickerVisible = false;

        component.onClickEmojiBtn();
        expect(component.emojiPickerVisible).toBe(true);
        expect((component.quillEditorRef as any).setSelection).not.toHaveBeenCalled();

        component.onClickEmojiBtn();
        expect(component.emojiPickerVisible).toBe(false);
        expect((component.quillEditorRef as any).setSelection).toHaveBeenCalledWith(3, 0);
    });

    it('should recheck selected links when content changes while tooltip is visible', () => {
        component.linkTooltipVisible = true;
        (component.quillEditorRef as any).getSelection.mockReturnValue({ index: 6, length: 2 });
        const checkSpy = vi.spyOn((component as any), 'checkForLinkAtSelection').mockReturnValue(undefined);
        const editorSpy = vi.spyOn(component, 'checkEditorContentForLinks').mockReturnValue(undefined);
        const event = { html: '<p>text</p>' };

        component.contentChanged(event);

        expect(checkSpy).toHaveBeenCalledWith(6, 2);
        expect(editorSpy).toHaveBeenCalledWith(event);
    });

    it('should only inspect editor content when tooltip is hidden', () => {
        component.linkTooltipVisible = false;
        const checkSpy = vi.spyOn((component as any), 'checkForLinkAtSelection').mockReturnValue(undefined);
        const editorSpy = vi.spyOn(component, 'checkEditorContentForLinks').mockReturnValue(undefined);
        const event = { html: '<p>text</p>' };

        component.contentChanged(event);

        expect(checkSpy).not.toHaveBeenCalled();
        expect(editorSpy).toHaveBeenCalledWith(event);
    });

    it('should clear previews when editor HTML is null', () => {
        component.storedUrls = ['https://one.test'];
        component.linkPreviews = [{ url: 'https://one.test' }] as any;

        component.checkEditorContentForLinks({ html: null });

        expect(component.storedUrls).toEqual([]);
        expect(component.linkPreviews).toEqual([]);
    });

    it('should add previews for new links found in editor HTML', () => {
        linkPreviewService.buildLinkPreview.mockReturnValue(of({
            url: 'https://one.test',
            domain: 'one.test'
        } as any));

        component.checkEditorContentForLinks({
            html: '<p><a href="https://one.test">One</a></p>'
        });

        expect(component.storedUrls).toEqual(['https://one.test']);
        expect(linkPreviewService.buildLinkPreview).toHaveBeenCalledWith({ url: 'https://one.test' });
        expect(component.linkPreviews.length).toBe(1);
    });

    it('should store a URL but not add an invalid link preview without a domain', () => {
        linkPreviewService.buildLinkPreview.mockReturnValue(of({
            url: 'https://one.test',
            domain: null
        } as any));

        component.checkEditorContentForLinks({
            html: '<a href="https://one.test">One</a>'
        });

        expect(component.storedUrls).toEqual(['https://one.test']);
        expect(component.linkPreviews).toEqual([]);
    });

    it('should remove URLs and matching previews no longer present in editor HTML', () => {
        component.storedUrls = ['https://old.test'];
        component.linkPreviews = [
            { url: 'https://old.test', domain: 'old.test' },
            { url: 'https://keep.test', domain: 'keep.test' }
        ] as any;

        component.checkEditorContentForLinks({ html: '<p>No links</p>' });

        expect(component.storedUrls).toEqual([]);
        expect(component.linkPreviews).toEqual([
            expect.objectContaining({ url: 'https://keep.test' })
        ] as any);
    });

    it('should do nothing when editor and stored URL arrays are both empty', () => {
        const compareSpy = vi.spyOn((component as any), 'compareUrlArrays').mockReturnValue(undefined);

        component.checkEditorContentForLinks({ html: '<p>No links</p>' });

        expect(compareSpy).not.toHaveBeenCalled();
    });

    it('should ignore keyboard events when editor is not focused', () => {
        (component.quillEditorRef as any).hasFocus.mockReturnValue(false);
        const linkSpy = vi.spyOn(component, 'onLinkBtnClickOrKeyShortcut').mockReturnValue(undefined);
        const checkSpy = vi.spyOn((component as any), 'checkForLinkAtSelection').mockReturnValue(undefined);

        component.handleKeyDown(new KeyboardEvent('keydown', { key: 'k', ctrlKey: true }));

        expect(linkSpy).not.toHaveBeenCalled();
        expect(checkSpy).not.toHaveBeenCalled();
    });

    it('should trigger link handling for Ctrl+K and Cmd+K', () => {
        const linkSpy = vi.spyOn(component, 'onLinkBtnClickOrKeyShortcut').mockReturnValue(undefined);

        component.handleKeyDown(new KeyboardEvent('keydown', { key: 'k', ctrlKey: true }));
        component.handleKeyDown(new KeyboardEvent('keydown', { key: 'k', metaKey: true }));

        expect(linkSpy).toHaveBeenCalledTimes(2);
    });

    it('should recheck links after Backspace, Delete and undo', () => {
        (component.quillEditorRef as any).getSelection.mockReturnValue({ index: 9, length: 1 });
        const checkSpy = vi.spyOn((component as any), 'checkForLinkAtSelection').mockReturnValue(undefined);

        component.handleKeyDown(new KeyboardEvent('keydown', { key: 'Backspace' }));
        component.handleKeyDown(new KeyboardEvent('keydown', { key: 'Delete' }));
        component.handleKeyDown(new KeyboardEvent('keydown', { key: 'z', ctrlKey: true }));
        component.handleKeyDown(new KeyboardEvent('keydown', { key: 'z', metaKey: true }));

        expect(checkSpy).toHaveBeenCalledTimes(4);
        expect(checkSpy).toHaveBeenCalledWith(9, 1);
    });

    it('should ignore unrelated focused keyboard events', () => {
        const linkSpy = vi.spyOn(component, 'onLinkBtnClickOrKeyShortcut').mockReturnValue(undefined);
        const checkSpy = vi.spyOn((component as any), 'checkForLinkAtSelection').mockReturnValue(undefined);

        component.handleKeyDown(new KeyboardEvent('keydown', { key: 'a' }));

        expect(linkSpy).not.toHaveBeenCalled();
        expect(checkSpy).not.toHaveBeenCalled();
    });

    it('should remove an active link when link action is triggered with tooltip open', () => {
        component.linkTooltipVisible = true;
        component.editorSelection = {
            userSelectionIndex: 1,
            userSelectionLength: 2,
            linkIndex: 0,
            linkLength: 5,
            placeholder: 'hello',
            linkUrl: 'https://test'
        };

        component.onLinkBtnClickOrKeyShortcut();

        expect((component.quillEditorRef as any).removeFormat).toHaveBeenCalledWith(0, 5);
        expect(component.linkTooltipVisible).toBe(false);
        expect(component.linkBtnSelected).toBe(false);
        expect((component.quillEditorRef as any).setSelection).toHaveBeenCalledWith(1, 2);
    });

    it('should return when link action is triggered without an editor selection', () => {
        (component.quillEditorRef as any).getSelection.mockReturnValue(null);

        component.onLinkBtnClickOrKeyShortcut();

        expect(modalService.open).not.toHaveBeenCalled();
    });

    it('should refocus and return when link action has a zero-length selection', () => {
        (component.quillEditorRef as any).getSelection.mockReturnValue({ index: 4, length: 0 });

        component.onLinkBtnClickOrKeyShortcut();

        expect((component.quillEditorRef as any).setSelection).toHaveBeenCalledWith(4, 0);
        expect(modalService.open).not.toHaveBeenCalled();
    });

    it('should build editor selection and create a new link', fakeAsync(() => {
        const ref = modalRef(Promise.resolve({ placeholder: 'New title', url: 'https://new.test' }));
        modalService.open.mockReturnValue(ref);
        (component.quillEditorRef as any).getSelection.mockReturnValue({ index: 2, length: 4 });
        (component.quillEditorRef as any).getFormat.mockReturnValue({});
        (component.quillEditorRef as any).getText.mockReturnValue('text');

        component.onLinkBtnClickOrKeyShortcut();
        tick();

        expect(component.linkBtnSelected).toBe(true);
        expect(component.editorSelection).toEqual({
            userSelectionIndex: 2,
            userSelectionLength: 4,
            placeholder: 'text',
            linkIndex: 2,
            linkLength: 4,
            linkUrl: null
        });
        expect(modalService.open).toHaveBeenCalledWith(BuildLinkComponent, {
            centered: true,
            backdrop: 'static'
        });
        expect(ref.componentInstance.placeholder).toBe('text');
        expect((component.quillEditorRef as any).deleteText).toHaveBeenCalledWith(2, 4);
        expect((component.quillEditorRef as any).insertText)
            .toHaveBeenCalledWith(2, 'New title', 'link', 'https://new.test');
        expect((component.quillEditorRef as any).setSelection).toHaveBeenCalledWith(11, 0);
    }));

    it('should prepopulate current URL when editing an existing link', fakeAsync(() => {
        const ref = modalRef(Promise.resolve(null));
        modalService.open.mockReturnValue(ref);
        (component.quillEditorRef as any).getSelection.mockReturnValue({ index: 1, length: 3 });
        (component.quillEditorRef as any).getFormat.mockReturnValue({ link: 'https://old.test' });
        (component.quillEditorRef as any).getText.mockReturnValue('old');

        component.onLinkBtnClickOrKeyShortcut();
        tick();

        expect(ref.componentInstance.currentUrl).toBe('https://old.test');
        expect((component.quillEditorRef as any).deleteText).not.toHaveBeenCalled();
        expect((component.quillEditorRef as any).setSelection).toHaveBeenCalledWith(1, 3);
        expect(component.linkBtnSelected).toBe(false);
    }));

    it('should restore selection when link modal returns no link', fakeAsync(() => {
        component.editorSelection = {
            userSelectionIndex: 7,
            userSelectionLength: 2,
            linkIndex: 7,
            linkLength: 2,
            placeholder: 'ab',
            linkUrl: null
        };
        component.linkBtnSelected = true;
        modalService.open.mockReturnValue(modalRef(Promise.resolve(undefined)));

        component.onEditLinkTooltipClick();
        tick();

        expect((component.quillEditorRef as any).setSelection).toHaveBeenCalledWith(7, 2);
        expect(component.linkBtnSelected).toBe(false);
    }));

    it('should close tooltip when editor loses selection', () => {
        component.linkTooltipVisible = true;
        component.linkBtnSelected = true;

        component.editorSelectionChanged({ range: null });

        expect(component.linkTooltipVisible).toBe(false);
        expect(component.linkBtnSelected).toBe(false);
    });

    it('should check links when editor selection changes', () => {
        const checkSpy = vi.spyOn((component as any), 'checkForLinkAtSelection').mockReturnValue(undefined);

        component.editorSelectionChanged({ range: { index: 4, length: 2 } });

        expect(checkSpy).toHaveBeenCalledWith(4, 2);
    });

    it('should open tooltip and calculate link selection for a highlighted link', () => {
        (component.quillEditorRef as any).getFormat.mockReturnValue({ link: 'https://test' });
        (component.quillEditorRef as any).getLeaf.mockReturnValue([{ text: 'linked' }, 2]);
        (component.quillEditorRef as any).getBounds.mockReturnValue({ left: 15, bottom: 8 });

        (component as any).checkForLinkAtSelection(5, 3);

        expect((component.quillEditorRef as any).getLeaf).toHaveBeenCalledWith(6);
        expect(component.linkTooltipLeftOffset).toBe(15);
        expect(component.linkTooltipBottomOffset).toBe(12);
        expect(component.editorSelection).toEqual({
            userSelectionIndex: 5,
            userSelectionLength: 3,
            linkIndex: 4,
            linkLength: 6,
            placeholder: 'linked',
            linkUrl: 'https://test'
        });
        expect(component.linkTooltipVisible).toBe(true);
        expect(component.linkBtnSelected).toBe(true);
    });

    it('should calculate link selection for a caret inside a link', () => {
        (component.quillEditorRef as any).getFormat.mockReturnValue({ link: 'https://test' });
        (component.quillEditorRef as any).getLeaf.mockReturnValue([{ text: 'linked' }, 2]);

        (component as any).checkForLinkAtSelection(5, 0);

        expect((component.quillEditorRef as any).getLeaf).toHaveBeenCalledWith(5);
        expect(component.editorSelection.linkIndex).toBe(3);
    });

    it('should close an existing tooltip and refocus when selection is not a link', () => {
        component.linkTooltipVisible = true;
        component.linkBtnSelected = true;
        (component.quillEditorRef as any).getFormat.mockReturnValue({});

        (component as any).checkForLinkAtSelection(6, 1);

        expect(component.linkTooltipVisible).toBe(false);
        expect(component.linkBtnSelected).toBe(false);
        expect((component.quillEditorRef as any).setSelection).toHaveBeenCalledWith(6, 1);
    });

    it('should simply refocus when selection is not a link and tooltip is already closed', () => {
        component.linkTooltipVisible = false;
        (component.quillEditorRef as any).getFormat.mockReturnValue({});

        (component as any).checkForLinkAtSelection(2, 0);

        expect((component.quillEditorRef as any).setSelection).toHaveBeenCalledWith(2, 0);
        expect(component.linkTooltipVisible).toBe(false);
    });

    it('should edit a link from the tooltip', () => {
        const addOrEditSpy = vi.spyOn((component as any), 'addOrEditLink').mockReturnValue(undefined);

        component.onEditLinkTooltipClick();

        expect(addOrEditSpy).toHaveBeenCalled();
    });

    it('should remove a link from the tooltip', () => {
        component.editorSelection = {
            userSelectionIndex: 3,
            userSelectionLength: 1,
            linkIndex: 2,
            linkLength: 4,
            placeholder: 'link',
            linkUrl: 'https://test'
        };
        component.linkTooltipVisible = true;
        component.linkBtnSelected = true;

        component.onRemoveLinkTooltipClick();

        expect((component.quillEditorRef as any).removeFormat).toHaveBeenCalledWith(2, 4);
        expect(component.linkTooltipVisible).toBe(false);
        expect(component.linkBtnSelected).toBe(false);
        expect((component.quillEditorRef as any).setSelection).toHaveBeenCalledWith(3, 1);
    });
});
