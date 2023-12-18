import {Component, Input, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {RxStompService} from "../../../services/rx-stomp.service";
import {JobChat, Post} from "../../../model/chat";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {ChatPostService} from "../../../services/chat-post.service";
import Quill from 'quill';
import {ImageHandler, Options} from 'ngx-quill-upload';

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
  moduleOptions = {};

  constructor(
    private fb: FormBuilder,
    private rxStompService: RxStompService,
    private modalService: NgbModal,
    private chatPostService: ChatPostService
  ) {
    Quill.register('modules/imageHandler', ImageHandler);
    this.moduleOptions = {
      // Doc for setting module toolbar options: https://quilljs.com/docs/modules/toolbar/
      toolbar: [
        { 'list': 'ordered'},
        { 'list': 'bullet' },
        'bold',
        'italic',
        'underline',
        'link',
        'image',
      ],
      // Doc for setting up image handler: https://www.npmjs.com/package/ngx-quill-upload
      imageHandler: {
        upload: (file) => {
          return this.doUpload(file);// your uploaded image URL as Promise<string>
        },
        accepts: ['png', 'jpg', 'jpeg'] // Extensions to allow for images (Optional) | Default - ['jpg', 'jpeg', 'png']
      } as Options,
    }
  }

  ngOnInit() {
    this.postForm = this.fb.group({
      content: ['', Validators.required]
    });
  }

  editorCreated(quill: Quill) {
    this.quillEditorRef = quill;
  }

  // Note: The image handler must return a Promise<String> so we need to convert our http request observable to a promise.
  // The doc suggests using the toPromise() method on the subscription, however this is to be deprecated in new versions of Rxjs.
  // Alternative option is create a new promise and resolve with the url string.
  private doUpload(file: File): Promise<String> {
    return new Promise ((resolve, reject) => {
      const formData: FormData = new FormData();
      formData.append('file', file);

      this.error = null;
      this.saving = true;
      // Upload image to the job's Google Drive folder (subfolder: ChatUploads).
      // The url string will then be returned through the Promise, and embedded into the editor.
      this.chatPostService.uploadFile(this.chat.id, formData).subscribe(
        urlDto => {
          const range = this.quillEditorRef.getSelection();
          this.quillEditorRef.insertEmbed(urlDto)
          this.saving = false;
          resolve(urlDto.url);
        },
        (error) => {
          reject(error)
          this.error = error
          this.saving = false;
        });
    })
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
