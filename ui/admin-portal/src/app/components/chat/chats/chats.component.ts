import {
  Component,
  EventEmitter,
  Input,
  OnChanges,
  OnInit,
  Output,
  SimpleChanges
} from '@angular/core';
import {JobChat} from "../../../model/chat";

@Component({
  selector: 'app-chats',
  templateUrl: './chats.component.html',
  styleUrls: ['./chats.component.scss']
})
export class ChatsComponent implements OnInit, OnChanges {
  @Input() chats: JobChat[];
  @Output() chatSelection = new EventEmitter();

  loading: boolean;
  error;

  currentChat: JobChat;
  constructor() { }

  ngOnInit(): void {
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (this.chats && this.chats.length > 0) {
      this.selectCurrent(this.chats[0])
    }
  }


  selectCurrent(chat: JobChat) {
    this.currentChat = chat;

    this.chatSelection.emit(chat);
  }

}
