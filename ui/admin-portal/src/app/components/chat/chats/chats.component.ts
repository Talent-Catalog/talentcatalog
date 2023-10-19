import {Component, EventEmitter, OnInit, Output} from '@angular/core';
import {JobChat} from "../../../model/chat";

@Component({
  selector: 'app-chats',
  templateUrl: './chats.component.html',
  styleUrls: ['./chats.component.scss']
})
export class ChatsComponent implements OnInit {
  chats: JobChat[];
  @Output() chatSelection = new EventEmitter();

  loading: boolean;
  error;

  currentChat: JobChat;
  constructor() { }

  ngOnInit(): void {
  }
  selectCurrent(chat: JobChat) {
    this.currentChat = chat;

    this.chatSelection.emit(chat);
  }

}
