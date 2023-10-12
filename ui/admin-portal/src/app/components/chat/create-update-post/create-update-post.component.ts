import {Component, OnDestroy, OnInit} from '@angular/core';
import {FormBuilder, FormGroup} from "@angular/forms";
import {RxStompService} from "../../../services/rx-stomp.service";
import {Subscription} from "rxjs";
import {Message} from '@stomp/stompjs';
import {ChatPost, Post} from "../../../model/chat";
import {AuthService} from "../../../services/auth.service";

@Component({
  selector: 'app-create-update-post',
  templateUrl: './create-update-post.component.html',
  styleUrls: ['./create-update-post.component.scss']
})
export class CreateUpdatePostComponent implements OnInit, OnDestroy {
  error: any;
  postForm: FormGroup;
  response: string;
  saving: boolean;

  // @ts-ignore, to suppress warning related to being undefined
  private topicSubscription: Subscription;

  constructor(
    private authService: AuthService,
    private fb: FormBuilder,
    private rxStompService: RxStompService
  ) { }

  ngOnInit() {
    this.postForm = this.fb.group({
      post: []
    });

    this.rxStompService.configure(this.authService.getRxStompConfig());
    this.rxStompService.activate();

    this.topicSubscription = this.rxStompService
    .watch('/topic/chat/1')
    .subscribe((message: Message) => {
      const payload: ChatPost = JSON.parse(message.body)
      this.response = payload.content
          + '(' + message.body + ')';
    });
  }

  ngOnDestroy() {
    this.topicSubscription.unsubscribe();
  }

  onSend() {
    const post: Post = {
      content: this.postForm.value.post
    }
    const body = JSON.stringify(post);
    this.rxStompService.publish({ destination: '/app/chat/1', body: body });
  }

}
