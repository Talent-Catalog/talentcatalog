import {Component, Input, OnInit} from '@angular/core';
import {JobChat, JobChatType} from "../../../model/chat";

@Component({
  selector: 'app-view-chat',
  templateUrl: './view-chat.component.html',
  styleUrls: ['./view-chat.component.scss']
})
export class ViewChatComponent implements OnInit {

  @Input() chat: JobChat

  constructor() { }

  ngOnInit(): void {
  }

  get displayName(): string {
    let name = "Chat ?";
    if (this.chat) {
      if (this.chat.name) {
        name = this.chat.name;
      } else if (this.chat.type) {
        name = JobChatType[this.chat.type];
      } else {
        name = "Chat " + this.chat.id;
      }
    }
    return name;
  }

}
