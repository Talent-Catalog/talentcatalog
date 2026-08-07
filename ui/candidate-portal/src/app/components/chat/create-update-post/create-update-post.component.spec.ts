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

import {UntypedFormBuilder} from '@angular/forms';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {of, throwError} from 'rxjs';

import {JobChat} from '../../../model/chat';
import {LinkPreview} from '../../../model/link-preview';
import {ChatPostService} from '../../../services/chat-post.service';
import {LinkPreviewService} from '../../../services/link-preview.service';
import {RxStompService} from '../../../services/rx-stomp.service';
import {BuildLinkComponent} from '../../../util/build-link/build-link.component';
import {FileSelectorComponent} from '../../util/file-selector/file-selector.component';
import {CreateUpdatePostComponent} from './create-update-post.component';

describe('CreateUpdatePostComponent', () => {
  let component: CreateUpdatePostComponent;
  let rxStompServiceSpy: jasmine.SpyObj<RxStompService>;
  let modalServiceSpy: jasmine.SpyObj<NgbModal>;
  let chatPostServiceSpy: jasmine.SpyObj<ChatPostService>;
  let linkPreviewServiceSpy: jasmine.SpyObj<LinkPreviewService>;
  let quillSpy: any;

  const chat = {id: 42} as JobChat;

  beforeEach(() => {
    rxStompServiceSpy = jasmine.createSpyObj<RxStompService>(
      'RxStompService',
      ['publish']
    );

    modalServiceSpy = jasmine.createSpyObj<NgbModal>(
      'NgbModal',
      ['open']
    );

    chatPostServiceSpy = jasmine.createSpyObj<ChatPostService>(
      'ChatPostService',
      ['uploadFile']
    );

    linkPreviewServiceSpy = jasmine.createSpyObj<LinkPreviewService>(
      'LinkPreviewService',
      ['buildLinkPreview']
    );

    component = new CreateUpdatePostComponent(
      new UntypedFormBuilder(),
      rxStompServiceSpy,
      modalServiceSpy,
      chatPostServiceSpy,
      linkPreviewServiceSpy
    );

    component.chat = chat;
    component.ngOnInit();

    quillSpy = jasmine.createSpyObj('Quill', [
      'insertEmbed',
      'insertText',
      'setSelection',
      'deleteText',
      'removeFormat',
      'getSelection',
      'getFormat',
      'getText',
      'getLeaf',
      'getBounds',
      'hasFocus'
    ]);

    quillSpy.selection = {
      savedRange: {
        index: 3
      }
    };

    quillSpy.theme = {
      tooltip: {
        show: jasmine.createSpy('show')
      }
    };

    quillSpy.hasFocus.and.returnValue(true);
    quillSpy.getSelection.and.returnValue({
      index: 3,
      length: 2
    });
    quillSpy.getFormat.and.returnValue({});
    quillSpy.getText.and.returnValue('selected text');
    quillSpy.getLeaf.and.returnValue([
      {text: 'linked text'},
      0
    ]);
    quillSpy.getBounds.and.returnValue({
      left: 10,
      bottom: 5
    });

    component.editorCreated(quillSpy);
  });

  // Keep the existing test.
  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize the form and link regular expression', () => {
    expect(component.postForm).toBeTruthy();
    expect(component.contentControl.value).toBe('');
    expect(component.contentControl.hasError('required')).toBeTrue();

    const matches = [
      ...'<a href="https://example.com">Example</a>'
      .matchAll(component.regexpLink)
    ];

    expect(matches[0][1]).toBe('https://example.com');
  });

  it('should save the Quill editor and disable its native tooltip', () => {
    expect(component.quillEditorRef).toBe(quillSpy);

    expect(() => {
      component.quillEditorRef.theme.tooltip.show();
    }).not.toThrow();
  });

  it('should send a post and clear the editor content', () => {
    const visiblePreview = createPreview(
      'https://visible.example',
      false
    );

    const blockedPreview = createPreview(
      'https://blocked.example',
      true
    );

    component.linkPreviews = [
      visiblePreview,
      blockedPreview
    ];

    component.contentControl.setValue('Test message');

    component.onSend();

    expect(rxStompServiceSpy.publish)
    .toHaveBeenCalledTimes(1);

    const publication =
      rxStompServiceSpy.publish.calls.mostRecent().args[0];

    expect(publication.destination).toBe('/app/chat/42');

    expect(JSON.parse(publication.body)).toEqual({
      content: 'Test message',
      linkPreviews: [visiblePreview]
    });

    expect(component.contentControl.value).toBeNull();
  });

  it('should send an empty link-preview array', () => {
    component.linkPreviews = [];
    component.contentControl.setValue('No links');

    component.onSend();

    const publication =
      rxStompServiceSpy.publish.calls.mostRecent().args[0];

    expect(JSON.parse(publication.body).linkPreviews)
    .toEqual([]);
  });

  it('should not send a post when chat is missing', () => {
    component.chat = null;
    component.contentControl.setValue('Unsent message');

    component.onSend();

    expect(rxStompServiceSpy.publish).not.toHaveBeenCalled();
    expect(component.contentControl.value)
    .toBe('Unsent message');
  });

  it('should upload an image and insert an image embed', () => {
    const file = new File(
      ['image'],
      'photo.png',
      {type: 'image/png'}
    );

    chatPostServiceSpy.uploadFile.and.returnValue(
      of({url: 'https://files.example/photo.png'})
    );

    (component as any).doUpload(file);

    expect(component.error).toBeNull();
    expect(component.saving).toBeFalse();

    expect(chatPostServiceSpy.uploadFile)
    .toHaveBeenCalledTimes(1);

    const uploadArguments =
      chatPostServiceSpy.uploadFile.calls.mostRecent().args;

    expect(uploadArguments[0]).toBe(42);
    expect((uploadArguments[1] as FormData).get('file'))
    .toBe(file);

    expect(quillSpy.insertEmbed).toHaveBeenCalledOnceWith(
      3,
      'image',
      'https://files.example/photo.png',
      'user'
    );
  });

  it('should upload a document and insert a file link', () => {
    const file = new File(
      ['document'],
      'document.pdf',
      {type: 'application/pdf'}
    );

    chatPostServiceSpy.uploadFile.and.returnValue(
      of({url: 'https://files.example/document.pdf'})
    );

    (component as any).doUpload(file);

    expect(quillSpy.insertText).toHaveBeenCalledOnceWith(
      3,
      'link to file',
      'link',
      'https://files.example/document.pdf',
      'user'
    );

    expect(component.saving).toBeFalse();
  });

  it('should handle file upload errors', () => {
    const file = new File(
      ['document'],
      'document.pdf',
      {type: 'application/pdf'}
    );

    const error = new Error('Upload failed');

    chatPostServiceSpy.uploadFile.and.returnValue(
      throwError(error)
    );

    (component as any).doUpload(file);

    expect(component.error).toBe(error);
    expect(component.saving).toBeFalse();
    expect(quillSpy.insertText).not.toHaveBeenCalled();
    expect(quillSpy.insertEmbed).not.toHaveBeenCalled();
  });

  it('should open the file selector and upload the selected file', async () => {
    const file = new File(
      ['image'],
      'photo.png',
      {type: 'image/png'}
    );

    const modalReference: any = {
      componentInstance: {},
      result: Promise.resolve([file])
    };

    modalServiceSpy.open.and.returnValue(modalReference);

    const uploadSpy = spyOn<any>(
      component,
      'doUpload'
    );

    component.uploadFile();

    await modalReference.result;
    await Promise.resolve();

    expect(modalServiceSpy.open).toHaveBeenCalledOnceWith(
      FileSelectorComponent,
      {
        centered: true,
        backdrop: 'static'
      }
    );

    expect(modalReference.componentInstance.maxFiles).toBe(1);
    expect(modalReference.componentInstance.closeButtonLabel)
    .toBe('Upload');
    expect(modalReference.componentInstance.title)
    .toBe('Select file to upload');

    expect(uploadSpy).toHaveBeenCalledOnceWith(file);
  });

  it('should not upload when no file is selected', async () => {
    const modalReference: any = {
      componentInstance: {},
      result: Promise.resolve([])
    };

    modalServiceSpy.open.and.returnValue(modalReference);

    const uploadSpy = spyOn<any>(
      component,
      'doUpload'
    );

    component.uploadFile();

    await modalReference.result;
    await Promise.resolve();

    expect(uploadSpy).not.toHaveBeenCalled();
  });

  it('should handle dismissal of the file selector', async () => {
    const modalReference: any = {
      componentInstance: {},
      result: Promise.reject('dismissed')
    };

    modalServiceSpy.open.and.returnValue(modalReference);

    const uploadSpy = spyOn<any>(
      component,
      'doUpload'
    );

    component.uploadFile();

    await modalReference.result.catch(() => undefined);
    await Promise.resolve();

    expect(uploadSpy).not.toHaveBeenCalled();
  });

  it('should insert an emoji and move the caret', () => {
    component.emojiPickerVisible = true;

    component.onSelectEmoji({
      emoji: {
        native: '😊'
      }
    });

    expect(component.emojiPickerVisible).toBeFalse();

    expect(quillSpy.insertText).toHaveBeenCalledOnceWith(
      3,
      '😊',
      'user'
    );

    expect(quillSpy.setSelection)
    .toHaveBeenCalledOnceWith(5, 0);
  });

  it('should open and close the emoji picker', () => {
    component.toggleEmojiPicker();

    expect(component.emojiPickerVisible).toBeTrue();
    expect(quillSpy.setSelection).not.toHaveBeenCalled();

    component.toggleEmojiPicker();

    expect(component.emojiPickerVisible).toBeFalse();
    expect(quillSpy.setSelection)
    .toHaveBeenCalledWith(3, 0);
  });

  it('should clear link previews when editor HTML is null', () => {
    component.storedUrls = ['https://example.com'];
    component.linkPreviews = [
      createPreview('https://example.com')
    ];

    component.checkEditorContentForLinks({
      html: null
    });

    expect(component.storedUrls).toEqual([]);
    expect(component.linkPreviews).toEqual([]);
  });

  it('should add previews for links found in editor content', () => {
    linkPreviewServiceSpy.buildLinkPreview.and.callFake(
      (request) => of(createPreview(request.url))
    );

    component.checkEditorContentForLinks({
      html: `
        <a href="https://one.example">One</a>
        <a href="https://two.example">Two</a>
      `
    });

    expect(component.storedUrls).toEqual([
      'https://one.example',
      'https://two.example'
    ]);

    expect(linkPreviewServiceSpy.buildLinkPreview)
    .toHaveBeenCalledTimes(2);

    expect(component.linkPreviews.length).toBe(2);
  });

  it('should not add an invalid link preview without a domain', () => {
    const invalidPreview = {
      ...createPreview('https://invalid.example'),
      domain: ''
    };

    linkPreviewServiceSpy.buildLinkPreview.and.returnValue(
      of(invalidPreview)
    );

    (component as any).addLinkPreview(
      'https://invalid.example'
    );

    expect(component.storedUrls)
    .toContain('https://invalid.example');
    expect(component.linkPreviews).toEqual([]);
  });

  it('should remove deleted URLs and their previews', () => {
    const removedUrl = 'https://removed.example';
    const retainedUrl = 'https://retained.example';

    component.storedUrls = [
      removedUrl,
      retainedUrl
    ];

    component.linkPreviews = [
      createPreview(removedUrl),
      createPreview(retainedUrl)
    ];

    (component as any).compareUrlArrays([retainedUrl]);

    expect(component.storedUrls).toEqual([retainedUrl]);
    expect(component.linkPreviews).toEqual([
      createPreview(retainedUrl)
    ]);
  });

  it('should recheck the selected link when content changes', () => {
    component.linkTooltipVisible = true;

    quillSpy.getSelection.and.returnValue({
      index: 4,
      length: 2
    });

    const checkSelectionSpy = spyOn<any>(
      component,
      'checkForLinkAtSelection'
    );

    const checkLinksSpy = spyOn(
      component,
      'checkEditorContentForLinks'
    );

    const event = {
      html: '<p>Content</p>'
    };

    component.contentChanged(event);

    expect(checkSelectionSpy)
    .toHaveBeenCalledOnceWith(4, 2);
    expect(checkLinksSpy)
    .toHaveBeenCalledOnceWith(event);
  });

  it('should ignore keyboard shortcuts when editor is not focused', () => {
    quillSpy.hasFocus.and.returnValue(false);

    const linkActionSpy = spyOn(
      component,
      'onLinkBtnClickOrKeyShortcut'
    );

    component.handleKeyDown(
      new KeyboardEvent('keydown', {
        key: 'k',
        ctrlKey: true
      })
    );

    expect(linkActionSpy).not.toHaveBeenCalled();
  });

  it('should handle the Ctrl+K link shortcut', () => {
    const linkActionSpy = spyOn(
      component,
      'onLinkBtnClickOrKeyShortcut'
    );

    component.handleKeyDown(
      new KeyboardEvent('keydown', {
        key: 'k',
        ctrlKey: true
      })
    );

    expect(linkActionSpy).toHaveBeenCalledTimes(1);
  });

  it('should recheck link selection after Backspace', () => {
    quillSpy.getSelection.and.returnValue({
      index: 4,
      length: 1
    });

    const checkSpy = spyOn<any>(
      component,
      'checkForLinkAtSelection'
    );

    component.handleKeyDown(
      new KeyboardEvent('keydown', {
        key: 'Backspace'
      })
    );

    expect(checkSpy).toHaveBeenCalledOnceWith(4, 1);
  });

  it('should ignore link action when there is no selection', () => {
    quillSpy.getSelection.and.returnValue(null);

    const modalActionSpy = spyOn<any>(
      component,
      'addOrEditLink'
    );

    component.onLinkBtnClickOrKeyShortcut();

    expect(modalActionSpy).not.toHaveBeenCalled();
  });

  it('should refocus a collapsed selection without opening modal', () => {
    quillSpy.getSelection.and.returnValue({
      index: 4,
      length: 0
    });

    const modalActionSpy = spyOn<any>(
      component,
      'addOrEditLink'
    );

    component.onLinkBtnClickOrKeyShortcut();

    expect(quillSpy.setSelection)
    .toHaveBeenCalledWith(4, 0);
    expect(modalActionSpy).not.toHaveBeenCalled();
  });

  it('should prepare a selected text range for link creation', () => {
    quillSpy.getSelection.and.returnValue({
      index: 4,
      length: 5
    });

    quillSpy.getFormat.and.returnValue({});
    quillSpy.getText.and.returnValue('Talent');

    const modalActionSpy = spyOn<any>(
      component,
      'addOrEditLink'
    );

    component.onLinkBtnClickOrKeyShortcut();

    expect(component.editorSelection).toEqual({
      userSelectionIndex: 4,
      userSelectionLength: 5,
      placeholder: 'Talent',
      linkIndex: 4,
      linkLength: 5,
      linkUrl: null
    });

    expect(modalActionSpy).toHaveBeenCalledTimes(1);
  });

  it('should identify an existing link and open the tooltip', () => {
    quillSpy.getFormat.and.returnValue({
      link: 'https://example.com'
    });

    quillSpy.getLeaf.and.returnValue([
      {text: 'Example'},
      2
    ]);

    quillSpy.getBounds.and.returnValue({
      left: 12,
      bottom: 8
    });

    (component as any).checkForLinkAtSelection(5, 2);

    expect(quillSpy.getLeaf).toHaveBeenCalledWith(6);
    expect(component.linkTooltipLeftOffset).toBe(12);
    expect(component.linkTooltipBottomOffset).toBe(12);
    expect(component.linkBtnSelected).toBeTrue();
    expect(component.linkTooltipVisible).toBeTrue();

    expect(component.editorSelection).toEqual({
      userSelectionIndex: 5,
      userSelectionLength: 2,
      linkIndex: 4,
      linkLength: 7,
      placeholder: 'Example',
      linkUrl: 'https://example.com'
    });
  });

  it('should close the tooltip when selection is not a link', () => {
    component.linkBtnSelected = true;
    component.linkTooltipVisible = true;

    quillSpy.getFormat.and.returnValue({});

    (component as any).checkForLinkAtSelection(5, 2);

    expect(component.linkBtnSelected).toBeFalse();
    expect(component.linkTooltipVisible).toBeFalse();
    expect(quillSpy.setSelection)
    .toHaveBeenCalledWith(5, 2);
  });

  it('should close the tooltip when editor loses selection', () => {
    component.linkBtnSelected = true;
    component.linkTooltipVisible = true;

    component.editorSelectionChanged({
      range: null
    });

    expect(component.linkBtnSelected).toBeFalse();
    expect(component.linkTooltipVisible).toBeFalse();
  });

  it('should remove link formatting from the current selection', () => {
    component.linkBtnSelected = true;
    component.linkTooltipVisible = true;

    component.editorSelection = {
      userSelectionIndex: 5,
      userSelectionLength: 2,
      linkIndex: 3,
      linkLength: 7,
      placeholder: 'Example',
      linkUrl: 'https://example.com'
    };

    component.onRemoveLinkTooltipClick();

    expect(quillSpy.removeFormat)
    .toHaveBeenCalledOnceWith(3, 7);

    expect(quillSpy.setSelection)
    .toHaveBeenCalledWith(5, 2);

    expect(component.linkBtnSelected).toBeFalse();
    expect(component.linkTooltipVisible).toBeFalse();
  });

  it('should apply the link returned by the link modal', async () => {
    component.editorSelection = {
      userSelectionIndex: 4,
      userSelectionLength: 6,
      linkIndex: 4,
      linkLength: 6,
      placeholder: 'Talent',
      linkUrl: 'https://old.example'
    };

    const link = {
      placeholder: 'Talent Catalog',
      url: 'https://talentcatalog.net'
    };

    const modalReference: any = {
      componentInstance: {},
      result: Promise.resolve(link)
    };

    modalServiceSpy.open.and.returnValue(modalReference);

    component.onEditLinkTooltipClick();

    await modalReference.result;
    await Promise.resolve();

    expect(modalServiceSpy.open).toHaveBeenCalledOnceWith(
      BuildLinkComponent,
      {
        centered: true,
        backdrop: 'static'
      }
    );

    expect(modalReference.componentInstance.placeholder)
    .toBe('Talent');

    expect(modalReference.componentInstance.currentUrl)
    .toBe('https://old.example');

    expect(quillSpy.deleteText)
    .toHaveBeenCalledWith(4, 6);

    expect(quillSpy.insertText).toHaveBeenCalledWith(
      4,
      'Talent Catalog',
      'link',
      'https://talentcatalog.net'
    );

    expect(quillSpy.setSelection)
    .toHaveBeenCalledWith(18, 0);
  });

  it('should restore the selection when link modal returns no link', async () => {
    component.editorSelection = {
      userSelectionIndex: 4,
      userSelectionLength: 6,
      linkIndex: 4,
      linkLength: 6,
      placeholder: 'Talent',
      linkUrl: null
    };

    const modalReference: any = {
      componentInstance: {},
      result: Promise.resolve(null)
    };

    modalServiceSpy.open.and.returnValue(modalReference);

    component.onEditLinkTooltipClick();

    await modalReference.result;
    await Promise.resolve();

    expect(quillSpy.deleteText).not.toHaveBeenCalled();
    expect(quillSpy.insertText).not.toHaveBeenCalled();

    expect(quillSpy.setSelection)
    .toHaveBeenCalledWith(4, 6);
  });
});

function createPreview(
  url: string,
  blocked = false
): LinkPreview {
  return {
    url,
    title: 'Example',
    description: 'Example description',
    domain: new URL(url).hostname,
    blocked
  };
}
