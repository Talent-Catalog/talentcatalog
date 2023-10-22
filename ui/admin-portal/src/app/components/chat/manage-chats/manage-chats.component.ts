import {Component, OnInit} from '@angular/core';
import {JobChat, UpdateChatRequest} from "../../../model/chat";
import {ChatService} from "../../../services/chat.service";

@Component({
  selector: 'app-manage-chats',
  templateUrl: './manage-chats.component.html',
  styleUrls: ['./manage-chats.component.scss']
})
export class ManageChatsComponent implements OnInit {
  chats: JobChat[] = [];

  error: any;
  loading: boolean;

  private selectedChat: JobChat;

  constructor(private chatService: ChatService) { }

  ngOnInit(): void {
    this.loading = false;
    this.error = null;
    this.chatService.list().subscribe(
        chats => {
          this.chats = chats;
          this.loading = false;
        },
        error => {
          this.error = error;
          this.loading = false;
        });
  }

  doEditChat() {
    //todo doEditChat
  }

  doNewChat() {
    let request: UpdateChatRequest = {};
    this.loading = false;
    this.error = null;
    this.chatService.create(request).subscribe(
        jobChat => {
          this.chats.push(jobChat);
          this.loading = false;
        },
        error => {
          this.error = error;
          this.loading = false;
        });
  }

  onChatSelected(selectedChat: JobChat) {
      this.selectedChat = selectedChat;
  }
}
