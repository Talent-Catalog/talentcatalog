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

import {CreateUpdatePostComponent} from "./create-update-post.component";
import {NgbModal, NgbModalRef} from "@ng-bootstrap/ng-bootstrap";
import {RxStompService} from "../../../services/rx-stomp.service";
import {ChatPostService} from "../../../services/chat-post.service";
import {ComponentFixture, fakeAsync, TestBed, tick, waitForAsync} from "@angular/core/testing";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {QuillModule} from "ngx-quill";
import {of} from "rxjs";
import {FileSelectorComponent} from "../../util/file-selector/file-selector.component";
import {TranslateModule} from "@ngx-translate/core";
import {JobChatType} from "../../../model/chat";

describe('CreateUpdatePostComponent', () => {
  let component: CreateUpdatePostComponent;
  let fixture: ComponentFixture<CreateUpdatePostComponent>;
  let modalService: jasmine.SpyObj<NgbModal>;
  let rxStompService: jasmine.SpyObj<RxStompService>;
  let chatPostService: jasmine.SpyObj<ChatPostService>;

  beforeEach(waitForAsync(() => {
    const modalServiceSpy = jasmine.createSpyObj('NgbModal', ['open']);
    const rxStompServiceSpy = jasmine.createSpyObj('RxStompService', ['publish']);
    const chatPostServiceSpy = jasmine.createSpyObj('ChatPostService', ['uploadFile']);

    TestBed.configureTestingModule({
      declarations: [CreateUpdatePostComponent],
      imports: [HttpClientTestingModule,ReactiveFormsModule,FormsModule,QuillModule,
        TranslateModule.forRoot({})],
      providers: [
        { provide: NgbModal, useValue: modalServiceSpy },
        { provide: RxStompService, useValue: rxStompServiceSpy },
        { provide: ChatPostService, useValue: chatPostServiceSpy }
      ]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CreateUpdatePostComponent);
    component = fixture.componentInstance;
    modalService = TestBed.inject(NgbModal) as jasmine.SpyObj<NgbModal>;
    rxStompService = TestBed.inject(RxStompService) as jasmine.SpyObj<RxStompService>;
    chatPostService = TestBed.inject(ChatPostService) as jasmine.SpyObj<ChatPostService>;
    // Mock quillEditorRef with selection property
    component.quillEditorRef = {
      selection: {
        savedRange: {
          index: 0
        }
      },
      insertText: jasmine.createSpy('insertText')
    };
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize postForm with content control', () => {
    expect(component.postForm).toBeDefined();
    expect(component.postForm.get('content')).toBeTruthy();
  });

  it('should be invalid if content is empty', () => {
    const contentControl = component.postForm.get('content');
    expect(contentControl.valid).toBeFalsy();
  });

  it('should be valid if content is not empty', () => {
    const contentControl = component.postForm.get('content');
    contentControl.setValue('Test content');
    expect(contentControl.valid).toBeTruthy();
  });

  it('should send a post', () => {
    component.chat = { id: 1, type: JobChatType.CandidateProspect }; // Mock chat object
    const content = 'Test content';
    const expectedBody = {
      content,
      linkPreviews: []  // Ensure this matches what is actually sent
    };
    component.postForm.get('content').setValue(content);
    component.onSend();
    expect(rxStompService.publish).toHaveBeenCalledWith({ destination: '/app/chat/1', body: JSON.stringify(expectedBody) });
    expect(component.postForm.get('content').value).toBeNull(); // Should clear content after sending
  });

  it('should upload a file', fakeAsync(() => {
    const file = new File(['file content'], 'test.txt');

    // Mock NgbModalRef with result property
    const modalRef: Partial<NgbModalRef> = {
      close: jasmine.createSpy('close'),
      componentInstance: {
        maxFiles: 1,
        closeButtonLabel: "Upload",
        title: "Select file to upload"
      },
      result: Promise.resolve([file])
    }; // Mock NgbModalRef

    modalService.open.and.returnValue(modalRef as NgbModalRef);
    chatPostService.uploadFile.and.returnValue(of({ url: 'file_url' }));

    // Initialize the chat object
    component.chat = { id: 1, type: JobChatType.CandidateProspect };


    component.uploadFile();
    tick(); // Flush pending asynchronous operations

    expect(modalService.open).toHaveBeenCalledWith(FileSelectorComponent, jasmine.any(Object));

    tick(); // Ensure promise resolution

    expect(chatPostService.uploadFile).toHaveBeenCalled();
    expect(component.error).toBeNull();
    expect(component.saving).toBeFalsy();
  }));


  it('should handle emoji selection', () => {
    const quillEditorRefSpy = jasmine.createSpyObj('Quill', ['insertText', 'setSelection'],['savedRange']);
    quillEditorRefSpy.selection = {
      savedRange: {
        index: 0
      }
    };

    component.quillEditorRef = quillEditorRefSpy;

    const event = { emoji: { native: '😊' } };
    const index: number = component.quillEditorRef.selection.savedRange.index;
    const emojiLength = 2;
    component.onSelectEmoji(event);
    expect(component.emojiPickerVisible).toBe(false); // Emoji picker should be closed after selection
    expect(quillEditorRefSpy.insertText).toHaveBeenCalledWith(jasmine.any(Number), '😊', 'user');
    expect(quillEditorRefSpy.setSelection).toHaveBeenCalledWith(index + emojiLength, 0); // Concrete values here
  });

  it('should toggle emoji picker', () => {
    const quillEditorRefSpy = jasmine.createSpyObj('Quill', ['setSelection']);
    quillEditorRefSpy.selection = {
      savedRange: {
        index: 0
      }
    };
    component.quillEditorRef = quillEditorRefSpy;

    component.emojiPickerVisible = false;
    component.onClickEmojiBtn();
    expect(component.emojiPickerVisible).toBe(true);
    expect(quillEditorRefSpy.setSelection).not.toHaveBeenCalled(); // Should not refocus caret if picker is opening

    component.onClickEmojiBtn();
    expect(component.emojiPickerVisible).toBe(false);
    expect(quillEditorRefSpy.setSelection).toHaveBeenCalled(); // Should refocus caret if picker is closing
  });
});
