import {
  Component,
  HostListener,
  Input,
  OnInit
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
import {EditorSelection} from "../../../model/base";

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

  // Component properties used for link functionality
  public linkBtnSelected: boolean = false;
  public linkTooltipLeftOffset: number;
  public linkTooltipBottomOffset: number;
  public linkTooltipVisible: boolean = false;
  public editorSelection: EditorSelection;

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
    // Overrides Quill's native link functionality, which was allowing users to enter invalid URLs
    // in its link info/editor tooltip.
    this.quillEditorRef.theme.tooltip.show = function () { }
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
    // When closing, focus the caret in editor.
    if(!this.emojiPickerVisible) {
      const index: number = this.quillEditorRef.selection.savedRange.index;
      this.quillEditorRef.setSelection(index, 0);
    }
  }

  public contentChanged(event: any) {
    // This opening block catches when a user has selected a link but then hits delete or space -
    // these actions don't register as selection changes from the editor's event emitter, so we
    // check whether a link is still selected when the tooltip is visible.
    if (this.linkTooltipVisible) {
      const selectedRange = this.quillEditorRef.getSelection();
      this.checkForLinkAtSelection(selectedRange.index, selectedRange.length)
    }

    this.checkEditorContentForLinks(event)
  }

  /**
   * Checks all the current contents of the editor for link-formatted text.
   * @param event contains content the editor's contents
   */
  public checkEditorContentForLinks(event) {
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

  /**
   * LINK FUNCTIONALITY - adding or editing links in the Post editor
   */

  /**
   * Handles typical keyboard shortcut to add/edit link in text editor (ctrl + k / cmd + k).
   * @param event keyboard event
   */
  @HostListener('window:keydown', ['$event'])
  handleKeyDown(event: KeyboardEvent) {
    if ((event.ctrlKey || event.metaKey) && event.key === 'k') {
      this.onLinkBtnClick();
    }
  }

  public onLinkBtnClick() {
    const currentSelection = this.quillEditorRef.getSelection();

    if (currentSelection == null) { // User is not focused on the editor at all
      return;
    } else if (currentSelection.length === 0) { // The selection is not a range
      // Refocus previous selection
      this.quillEditorRef.setSelection(currentSelection.index, currentSelection.length)
      return;
    }

    this.linkBtnSelected = true; // Highlight the button

    // Check the format of the selected range
    const selectedTextFormat =
      this.quillEditorRef.getFormat(currentSelection.index, currentSelection.length)

    // Set the component's EditorSelection properties
    this.editorSelection = {
      index: currentSelection.index,
      length: currentSelection.length,
      highlightedText: this.quillEditorRef.getText(currentSelection),
      url: selectedTextFormat.link ? selectedTextFormat.link : null
    }

    // Keep focus at current editor selection
    this.quillEditorRef.setSelection(currentSelection.index, currentSelection.length);

    this.addOrEditLink();
  }

  /**
   * Formats a text segment as a new link if there is no URL in the component instance
   * of EditorSelection, or edits an existing one if there is.
   */
  private addOrEditLink() {
    const currentSelection = this.quillEditorRef.getSelection();

    // Initiate link modal
    const linkModal = this.modalService.open(BuildLinkComponent, {
      centered: true,
      backdrop: 'static'
    })

    // Prepopulate modal form
    linkModal.componentInstance.selectedText = this.editorSelection.highlightedText;
    if (this.editorSelection.url) {
      linkModal.componentInstance.currentUrl = this.editorSelection.url;
    }

    // Handle the modal results
    linkModal.result.then((link) => {
      if (link) { // Modal has returned a valid link:
        // Delete the text selection
        this.quillEditorRef.deleteText(this.editorSelection.index, this.editorSelection.length);

        console.log(this.quillEditorRef.getSelection())

        // Replace it with the link-formatted text
        this.quillEditorRef.insertText(
          this.editorSelection.index, link.placeholder, 'link', link.url
        )

        // Refocus at end of placeholder, un-highlight button
        this.quillEditorRef.setSelection(this.editorSelection.index + link.placeholder.length, 0);
        this.linkBtnSelected = false;
        return
      }

      // Refocus previous selection, un-highlight button
      this.quillEditorRef.setSelection(currentSelection.index, currentSelection.length);
      this.linkBtnSelected = false;
    })
  }

  public editorSelectionChanged(event: any) {
    if (!event.range) { // User is not focused on the editor at all
      this.closeTooltip()
      return;
    }

    this.checkForLinkAtSelection(event.range.index, event.range.length)
  }

  /**
   * Checks for link format at current user selection in the editor. If link is present, it sets the
   * properties of the components {@link EditorSelection} object and opens the link tooltip, which
   * enables the user to navigate to the linked URL or edit/remove it.
   * @param selectionIndex index of editor selection
   * @param selectionLength no. of characters selected from index position
   */
  private checkForLinkAtSelection(selectionIndex: number, selectionLength: number) {

    if (this.quillEditorRef.getFormat(selectionIndex, selectionLength).link) { // It's a link:
      // A Blot is an abstraction in Quill that represents a distinct piece of content or formatting.
      // getLeaf() returns the distinct piece of content immediately before or surrounding the given
      // index, and we can then query it's formatting and other properties.
      let blot = this.quillEditorRef.getLeaf(selectionIndex);

      // Set the position of the tooltip: bounds returns the position relative to the editor and
      // the tooltip is offset relative to the toolbar.
      let bounds = this.quillEditorRef.getBounds(selectionIndex, selectionLength)
      this.linkTooltipLeftOffset = bounds.left;
      this.linkTooltipBottomOffset = -bounds.bottom + 20;

      // Set link parameters so user can edit/remove link if desired
      this.editorSelection = {
        // (Index of selection) - (no. of characters to beginning of blot)
        // Sets index to beginning of blot, which is what's needed for subsequent formatting.
        index: selectionIndex - blot[1],
        length: blot[0].text.length,
        highlightedText: blot[0].text,
        url: this.quillEditorRef.getFormat(selectionIndex, selectionLength).link
      }

      this.openTooltip();

    } else { // It's not a link:
      if (this.linkTooltipVisible) this.closeTooltip();
    }

    // Refocus at current selection
    this.quillEditorRef.setSelection(selectionIndex, selectionLength);
  }

  private openTooltip() {
    this.linkBtnSelected = true; // Highlight the link button
    this.linkTooltipVisible = true; // Show the tooltip
  }

  private closeTooltip() {
    this.linkBtnSelected = false; // Un-highlight link button
    this.linkTooltipVisible = false; // Hide the tooltip
  }

  public onEditLinkTooltipClick() {
    this.addOrEditLink();
  }

  public onRemoveLinkTooltipClick() {
    this.removeLink()
  }

  /**
   * Removes the link-formatting from the selection defined by the current properties of component's
   * {@link EditorSelection} instance.
   */
  private removeLink() {
    this.quillEditorRef.removeFormat(this.editorSelection.index, this.editorSelection.length);
  }

}
