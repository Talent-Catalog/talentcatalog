import {Component, Input, OnInit} from '@angular/core';
import {FormBuilder, FormGroup} from "@angular/forms";
import {RxStompService} from "../../../services/rx-stomp.service";
import {JobChat, Post} from "../../../model/chat";

@Component({
  selector: 'app-create-update-post',
  templateUrl: './create-update-post.component.html',
  styleUrls: ['./create-update-post.component.scss']
})
export class CreateUpdatePostComponent implements OnInit {
  @Input() chat: JobChat;

  error: any;
  postForm: FormGroup;

  toolbarOptions = {};

  constructor(
    private fb: FormBuilder,
    private rxStompService: RxStompService
  ) {
    this.toolbarOptions = {
      toolbar: [[{ 'list': 'ordered'}, { 'list': 'bullet' }],['bold', 'italic', 'underline'], ['link', 'image'] ]
    };
  }

  ngOnInit() {
    this.postForm = this.fb.group({
      content: []
    });
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
