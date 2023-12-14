import {Component, Input, OnInit} from '@angular/core';
import {FormBuilder, FormGroup} from "@angular/forms";
import {RxStompService} from "../../../services/rx-stomp.service";
import {JobChat, Post} from "../../../model/chat";
import {FileSelectorComponent} from "../../util/file-selector/file-selector.component";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {ChatPostService} from "../../../services/chat-post.service";

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

  moduleOptions = {};

  constructor(
    private fb: FormBuilder,
    private rxStompService: RxStompService,
    private modalService: NgbModal,
    private chatPostService: ChatPostService
  ) {
    // Doc: https://quilljs.com/docs/modules/toolbar/#handlers
    this.moduleOptions = {
      toolbar: {
        container: [
          { 'list': 'ordered'},
          { 'list': 'bullet' },
          'bold',
          'italic',
          'underline',
          'link',
          'image',
          ],
        handlers: {
          'image':  () => {
            this.uploadImage();
          }
        }
      },
    }
  }

  ngOnInit() {
    this.postForm = this.fb.group({
      content: []
    });
  }

  uploadImage() {
    const fileSelectorModal = this.modalService.open(FileSelectorComponent, {
      centered: true,
      backdrop: 'static'
    })

    fileSelectorModal.componentInstance.maxFiles = 1;
    fileSelectorModal.componentInstance.closeButtonLabel = "Upload";
    fileSelectorModal.componentInstance.title = "Upload image to post";

    fileSelectorModal.result
    .then((selectedFiles: File[]) => {
      if (selectedFiles.length > 0) {
        this.doUpload(selectedFiles[0]);
      }
    })
    .catch(() => {});
  }

  private doUpload(file: File) {
    const formData: FormData = new FormData();
    formData.append('file', file);

    this.error = null;
    this.saving = true;
    // Upload image to the job's Google Drive folder (subfolder: ChatUploads).
    // Then return the link to display in the content in an <img> tag for display.
    this.chatPostService.uploadFile(this.chat.id, formData).subscribe(
      linkToFile => {
        this.contentControl.patchValue(this.contentControl.value + ' <img src="' +  linkToFile + '">');
        this.saving = false;
      },
      (error) => {
        this.error = error
        this.saving = false;
      }
    );
  }

  get contentControl() { return this.postForm.get('content'); }

  onSend() {
    if (this.chat) {
      const post: Post = {
        content: this.contentControl.value
      }
      const body = JSON.stringify(post);
      //todo See retryIfDisconnected in publish doc
      this.rxStompService.publish({ destination: '/app/chat/' + this.chat.id, body: body });

      //Clear content.
      this.contentControl.patchValue(null);
    }
  }
}
