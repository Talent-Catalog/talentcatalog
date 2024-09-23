import {Component, Input, OnInit} from '@angular/core';
import {JobChat} from "../../../model/chat";
import {ChatService} from "../../../services/chat.service";

@Component({
  selector: 'app-view-chat',
  templateUrl: './view-chat.component.html',
  styleUrls: ['./view-chat.component.scss']
})
export class ViewChatComponent implements OnInit {

  @Input() chat: JobChat

  constructor(private chatService: ChatService) { }

  ngOnInit(): void {
  }

  get chatParticipantsKey(): string {
    return this.chatService.getChatInfoParticipantsKey(this.chat.type);
  }

  get chatPurposeKey(): string {
    return this.chatService.getChatInfoPurposeKey(this.chat.type);
  }
}
