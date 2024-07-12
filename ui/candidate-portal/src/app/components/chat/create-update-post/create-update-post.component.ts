/*
 * Copyright (c) 2023 Talent Beyond Boundaries.
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

import {Component, Input, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {RxStompService} from "../../../services/rx-stomp.service";
import {JobChat, Post} from "../../../model/chat";
import Quill from 'quill';
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {ChatPostService} from "../../../services/chat-post.service";
import {FileSelectorComponent} from "../../util/file-selector/file-selector.component";
import {LinkPreview} from "../../../model/link-preview";
import {
  BuildLinkPreviewRequest,
  LinkPreviewService
} from "../../../services/link-preview.service";

@Component({
  selector: 'app-create-update-post',
  templateUrl: './create-update-post.component.html',
  styleUrls: ['./create-update-post.component.scss']
})
export class CreateUpdatePostComponent implements OnInit {
  @Input() chat: JobChat;

  error: any;
  saving: any;
  postForm: FormGroup;
  quillEditorRef: Quill;
  public emojiPickerVisible: boolean = false;
  regexpLink: RegExp;
  storedUrls: string[] = [];
  linkPreviews: LinkPreview[] = [];

  constructor(
    private fb: FormBuilder,
    private rxStompService: RxStompService,
    private modalService: NgbModal,
    private chatPostService: ChatPostService,
    private linkPreviewService: LinkPreviewService,
  ) {}

  ngOnInit() {
    this.postForm = this.fb.group({
      content: ["", Validators.required]
    });
    this.regexpLink = new RegExp('<a href="(\\S+)"', 'gi');
  }

  editorCreated(quill: Quill) {
    this.quillEditorRef = quill;
  }

  private doUpload(file: File) {
    const formData: FormData = new FormData();
    formData.append('file', file);

    this.error = null;
    this.saving = true;
    // Upload image to the job's Google Drive folder (subfolder: ChatUploads).
    // The url string will then be returned and embedded into the editor.
    this.chatPostService.uploadFile(this.chat.id, formData).subscribe(
      urlDto => {
        const index: number = this.quillEditorRef.selection.savedRange.index;
        if (file.type.startsWith("image")) {
          this.quillEditorRef.insertEmbed(index, 'image', urlDto.url, 'user');
        } else {
          this.quillEditorRef.insertText(index, 'link to file', 'link', urlDto.url, 'user');
        }
        this.saving = false;
      },
      (error) => {
        this.error = error
        this.saving = false;
      });
  }

  get contentControl() { return this.postForm.controls.content; }

  onSend() {
    if (this.chat) {
      const post: Post = {
        content: this.contentControl.value,
        linkPreviews: this.attachLinkPreviews()
      }
      const body = JSON.stringify(post);
      //todo See retryIfDisconnected in publish doc
      this.rxStompService.publish({ destination: '/app/chat/' + this.chat.id, body: body });

      //Clear content.
      this.contentControl.patchValue(null);
    }
  }

  uploadFile() {
    const fileSelectorModal = this.modalService.open(FileSelectorComponent, {
      centered: true,
      backdrop: 'static'
    })

    fileSelectorModal.componentInstance.maxFiles = 1;
    fileSelectorModal.componentInstance.closeButtonLabel = "Upload";
    fileSelectorModal.componentInstance.title = "Select file to upload";

    fileSelectorModal.result
      .then((selectedFiles: File[]) => {
        if (selectedFiles.length > 0) {
          this.doUpload(selectedFiles[0]);
        }
      })
      .catch(() => {
      });
  }

  // Adds an emoji to the text editor and focuses the caret directly after it.
  public onSelectEmoji(event) {
    this.emojiPickerVisible = false;
    const index: number = this.quillEditorRef.selection.savedRange.index;
    this.quillEditorRef.insertText(index, `${event.emoji.native}`, 'user');
    this.quillEditorRef.setSelection(index + 2, 0);
  }

  // Toggles the emoji picker on or off and focuses the caret.
  public toggleEmojiPicker() {
    this.emojiPickerVisible = !this.emojiPickerVisible;
    // When closing, focus the caret in editor.
    if(!this.emojiPickerVisible) {
      const index: number = this.quillEditorRef.selection.savedRange.index;
      this.quillEditorRef.setSelection(index, 0);
    }
  }

  public checkForLinks(event) {
    if (event.html === null) {
      this.clearLinkPreviews()
    } else {
      const editorHtmlContent = event.html
      const liveUrls: string[] = [];
      const liveMatches: string[][] = [...editorHtmlContent.matchAll(this.regexpLink)]
      if (liveMatches.length > 0) {
        // More useful to have just the URLs vs the entire regex match array.
        liveMatches.forEach(match => {
          liveUrls.push(match[1]);
        })
      }
      this.compareUrlArrays(liveUrls);
    }
  }

  private compareUrlArrays(liveUrls: string[]) {
    for (const url of liveUrls) {
      if (!this.storedUrls.includes(url)) {
        this.addLinkPreview(url)
      }
    }

    for (const url of this.storedUrls) {
      if (!liveUrls.includes(url)) {
        this.removeLinkPreview(url)
      }
    }
  }

  private addLinkPreview(url: string) {
    // Add to storedUrls array
    this.storedUrls.push(url);

    // Build and include its linkPreview
    let request: BuildLinkPreviewRequest = {url: url};
    this.linkPreviewService.buildLinkPreview(request).subscribe(
      linkPreview => this.linkPreviews.push(linkPreview)
    )
  }

  private removeLinkPreview(url: string) {
    // Remove from storedUrls array
    this.storedUrls.splice(this.storedUrls.indexOf(url), 1);

    // Remove its linkPreview
    this.linkPreviews.forEach(linkPreview => {
      if (linkPreview.url === url) {
        this.linkPreviews.splice(this.linkPreviews.indexOf(linkPreview), 1);
      }
    })
  }

  private clearLinkPreviews() {
    this.storedUrls = [];
    this.linkPreviews = [];
  }

  // We don't want to save any linkPreviews that the user has blocked
  private attachLinkPreviews(): LinkPreview[] {
    if (this.linkPreviews.length > 0) {
      return this.linkPreviews.filter(
        linkPreview => !linkPreview.blocked
        )
    }
    return this.linkPreviews;
  }

}
