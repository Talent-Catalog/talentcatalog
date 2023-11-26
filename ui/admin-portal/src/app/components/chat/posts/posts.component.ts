import {Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {JobChat} from "../../../model/chat";
import {ChatPostService} from "../../../services/chat-post.service";
import {ChatService} from "../../../services/chat.service";
import {PostsComponentBase} from "../../util/chat/PostsComponentBase";

@Component({
  selector: 'app-posts',
  templateUrl: './posts.component.html',
  styleUrls: ['./posts.component.scss']
})
export class PostsComponent extends PostsComponentBase implements OnInit, OnChanges {

  @Input() chat: JobChat;
  @Input() readOnly: boolean = false;

  constructor(
      chatService: ChatService,
      chatPostService: ChatPostService,
  ) {
    super(chatService, chatPostService)
  }

  ngOnInit(): void {
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (this.chat) {
      this.onNewChat(this.chat);
    }
  }
}
