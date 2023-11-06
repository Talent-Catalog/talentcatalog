import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {MainSidePanelBase} from "../../util/split/MainSidePanelBase";
import {Router} from "@angular/router";
import {AuthorizationService} from "../../../services/authorization.service";
import {JobService} from "../../../services/job.service";
import {JobChat} from "../../../model/chat";

@Component({
  selector: 'app-chats-with-posts',
  templateUrl: './chats-with-posts.component.html',
  styleUrls: ['./chats-with-posts.component.scss']
})
export class ChatsWithPostsComponent extends MainSidePanelBase implements OnInit {
  @Input() chats: JobChat[];
  @Output() chatSelection = new EventEmitter();

  error: any;
  selectedChat: JobChat;

  constructor(
    private router: Router,
    private authService: AuthorizationService,
    private jobService: JobService
  ) {
    super(6);
  }

  ngOnInit(): void {
  }

  onChatSelected(chat: JobChat) {
    this.selectedChat = chat;
    this.chatSelection.emit(chat);
  }
}
