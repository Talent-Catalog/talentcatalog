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

  constructor(
    private fb: FormBuilder,
    private rxStompService: RxStompService
  ) { }

  ngOnInit() {
    this.postForm = this.fb.group({
      post: []
    });
  }

  onSend() {
    if (this.chat) {
      const post: Post = {
        content: this.postForm.value.post
      }
      const body = JSON.stringify(post);
      //todo See retryIfDisconnected in publish doc
      this.rxStompService.publish({ destination: '/app/chat/' + this.chat.id, body: body });
    }
  }
}
