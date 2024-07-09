import {Component, Input, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {RxStompService} from "../../../services/rx-stomp.service";
import {JobChat, Post} from "../../../model/chat";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {ChatPostService} from "../../../services/chat-post.service";
import Quill from 'quill';
import {FileSelectorComponent} from "../../util/file-selector/file-selector.component";
import {LinkPreview} from "../../../model/link-preview";
import {CreateLinkPreviewRequest, LinkPreviewService} from "../../../services/link-preview.service";

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
  storedMatches: string[][] = [];
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
        this.quillEditorRef.insertText(index, 'link to file', 'link', urlDto.url, 'user');
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
        linkPreviews: this.linkPreviews
        // TODO add html link preview here if it is to be added
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

  // Toggles the emoji picker on and off using the button on the editor toolbar, refocuses the caret.
  public onClickEmojiBtn() {
    this.emojiPickerVisible = !this.emojiPickerVisible;
    if (!this.emojiPickerVisible) {
      const index: number = this.quillEditorRef.selection.savedRange.index;
      this.quillEditorRef.setSelection(index, 0);
    }
  }

  public checkForLinks(event) {
    if (event.html === null) {
      this.clearLinkPreviews()
    } else {
      const editorHtmlContent = event.html
      const liveMatches: string[][] = [...editorHtmlContent.matchAll(this.regexpLink)]
      if (liveMatches.length !== this.storedMatches.length) {
        // The liveMatches and storedMatches have diverged, so we run further checks.

        if (liveMatches.length > this.storedMatches.length) {
          // There are more liveMatches than storedMatches, so we build and add the right linkPreview.
          for (const match of liveMatches) {
            if (!this.storedMatches.includes(match)) {
              this.storedMatches.push(match);
              let request: CreateLinkPreviewRequest = {url: match[1]};
              this.linkPreviewService.buildLinkPreview(request).subscribe(
                linkPreview => this.linkPreviews.push(linkPreview)
              )
            }
          }
        }

        if (this.storedMatches.length > liveMatches.length) {
          // There are more storedMatches than liveMatches, so we remove the right linkPreview.
          for (const match of this.storedMatches) {
            const index = this.storedMatches.indexOf(match);

            if (index > -1) {
              this.storedMatches.splice(index, 1);
              this.linkPreviews.forEach(
                linkPreview => {
                  if (linkPreview.url === match[1]) {
                    const index = this.linkPreviews.indexOf(linkPreview);

                    if (index > -1) {
                      this.linkPreviews.splice(index, 1);
                    }
                  }
                })
            }
          }
        }
      }
    }
  }

  private clearLinkPreviews() {
    this.storedMatches = [];
    this.linkPreviews = [];
  }

}
