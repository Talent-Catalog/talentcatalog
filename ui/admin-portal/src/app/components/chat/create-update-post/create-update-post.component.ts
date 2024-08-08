import {
  Component,
  HostListener,
  Input,
  OnInit,
} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {RxStompService} from "../../../services/rx-stomp.service";
import {JobChat, Post} from "../../../model/chat";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {ChatPostService} from "../../../services/chat-post.service";
import Quill from 'quill';
import {FileSelectorComponent} from "../../util/file-selector/file-selector.component";
import {LinkPreview} from "../../../model/link-preview";
import {
  BuildLinkPreviewRequest,
  LinkPreviewService
} from "../../../services/link-preview.service";
import {BuildLinkComponent} from "../../../util/build-link/build-link.component";

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

  public linkBtnSelected: boolean = false;
  public selectedLinkUrl: string;
  public linkTooltipXPosition: number;
  public linkTooltipYPosition: number;
  public linkTooltipVisible: boolean = false;

  constructor(
    private fb: FormBuilder,
    private rxStompService: RxStompService,
    private modalService: NgbModal,
    private chatPostService: ChatPostService,
    private linkPreviewService: LinkPreviewService
  ) {}

  ngOnInit() {
    this.postForm = this.fb.group({
      content: ["", Validators.required]
    });
    this.regexpLink = new RegExp('<a href="(\\S+)"', 'gi');
  }

  editorCreated(quill: Quill) {
    this.quillEditorRef = quill;
    // Overrides Quill's native link functionality, which was allowing users to use invalid URLs.
    this.quillEditorRef.theme.tooltip.show = function () { }
  }

  @HostListener('window:keydown', ['$event'])
  handleKeyDown(event: KeyboardEvent) {
    if ((event.ctrlKey || event.metaKey) && event.key === 'k') {
      this.onLinkBtnClick();
    }
  }

  public onLinkBtnClick() {
    this.linkBtnSelected = true;

    const selectedRange = this.quillEditorRef.getSelection();

    if (selectedRange == null || selectedRange.length === 0) return;

    const selectedText = this.quillEditorRef.getText(selectedRange);

    const selectedTextFormat = this.quillEditorRef.getFormat(selectedRange.index, selectedRange.length)

    // Initiate modal for building the link
    const linkModal = this.modalService.open(BuildLinkComponent, {
      centered: true,
      backdrop: 'static'
    })

    linkModal.componentInstance.selectedText = selectedText; // Prepopulate modal form

    if (selectedTextFormat.link) {
      linkModal.componentInstance.currentUrl = selectedTextFormat.link;
    }

    linkModal.result.then((link) => {
      if (link) {
        // Delete the selection
        this.quillEditorRef.deleteText(selectedRange.index, selectedRange.length);

        // Replace with Link from modal
        this.quillEditorRef.insertText(selectedRange.index, link.placeholder, 'link', link.url)
      }

      // Refocus caret at end of selection
      this.quillEditorRef.setSelection(selectedRange.index + selectedRange.length);

      this.linkBtnSelected = false;
    })
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

  // Toggles the emoji picker on and off using the button on the editor toolbar, refocuses the caret.
  public onClickEmojiBtn() {
    this.emojiPickerVisible = !this.emojiPickerVisible;
    if (!this.emojiPickerVisible) {
      const index: number = this.quillEditorRef.selection.savedRange.index;
      this.quillEditorRef.setSelection(index, 0);
    }
  }

  // Checks editor content for any links
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
      // We only need to run this check if there's something in either array.
      if (liveUrls.length > 0 || this.storedUrls.length > 0) this.compareUrlArrays(liveUrls);
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
      linkPreview => {
        // Checks that valid linkPreview has been returned and pushes to array if so.
        if (linkPreview.domain) this.linkPreviews.push(linkPreview);
      }
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

  // We don't want to save any linkPreviews that the user has blocked.
  private attachLinkPreviews(): LinkPreview[] {
    if (this.linkPreviews.length > 0) {
      return this.linkPreviews.filter(
        linkPreview => !linkPreview.blocked
        )
    }
    return this.linkPreviews;
  }

  disableScroll() {
    document.body.style.overflow = 'hidden';
  }

  enableScroll() {
    document.body.style.overflow = 'auto';
  }

  public checkForLinkAtSelection(event: any) {
    // event.range can be null when the focus moves outside the editor - it's a selection change but
    // doesn't have any data, handy in this instance because we can close the tooltip and end its
    // attendant effects for any click outside the editor.
    if (event.range === null) {
      this.closeTooltip()
    }

    // A Blot is an abstraction in Quill that represents a distinct piece of content or formatting.
    // A Leaf Blot represents atomic pieces of content such as text that don't have nested content
    // of their own. getLeaf() will return the distinct piece of content immediately before or
    // surrounding the given index, and we can then query it's formatting and other properties.
    let leafBlot = this.quillEditorRef.getLeaf(event.range.index);
    if (leafBlot[0].parent.constructor.name === 'Link') { // If it's a link:
      this.linkBtnSelected = true; // Highlight the link button
      this.selectedLinkUrl = leafBlot[0].parent.domNode.href; // Set the URL for the tooltip
      // Set the position of the tooltip
      this.linkTooltipXPosition = leafBlot[0].parent.domNode.getBoundingClientRect().x +
        leafBlot[0].parent.domNode.getBoundingClientRect().width
      this.linkTooltipYPosition = leafBlot[0].parent.domNode.getBoundingClientRect().y +
        leafBlot[0].parent.domNode.getBoundingClientRect().height
      this.linkTooltipVisible = true; // Show the tooltip
      this.disableScroll(); // Disable document scrolling so it stays in position (copied from Slack!)
    } else { // If it's not a link:
      this.closeTooltip()
    }
  }

  private closeTooltip() {
    this.linkBtnSelected = false; // Un-highlight link button
    this.linkTooltipVisible = false; // Hide the tooltip
    this.enableScroll(); // Re-enable scrolling
    return;
  }

}
