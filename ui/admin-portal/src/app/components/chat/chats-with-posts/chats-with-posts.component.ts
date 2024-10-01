import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {MainSidePanelBase} from "../../util/split/MainSidePanelBase";
import {JobChat} from "../../../model/chat";
import {ChatService} from "../../../services/chat.service";
import {AuthorizationService} from "../../../services/authorization.service";

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
    private chatService: ChatService,
    private authorizationService: AuthorizationService
  ) {
    super(6);
  }

  ngOnInit(): void {
  }

  onChatSelected(chat: JobChat) {
    this.selectedChat = chat;
    this.chatSelection.emit(chat);
  }

  onMarkChatAsRead() {
    if (this.selectedChat) {
      this.chatService.markChatAsRead(this.selectedChat);
    }
  }

  isReadOnlyUser() {
    return this.authorizationService.isReadOnly();
  }
}
