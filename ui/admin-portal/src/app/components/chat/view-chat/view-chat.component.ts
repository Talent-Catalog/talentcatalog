import {Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {JobChat} from "../../../model/chat";
import {Observable} from "rxjs";
import {ChatService} from "../../../services/chat.service";

@Component({
  selector: 'app-view-chat',
  templateUrl: './view-chat.component.html',
  styleUrls: ['./view-chat.component.scss']
})
export class ViewChatComponent implements OnInit, OnChanges {

  @Input() chat: JobChat
  chatReadStatus$: Observable<boolean>;
  constructor(
    private chatService: ChatService
  ) { }

  ngOnInit(): void {
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (this.chat) {
      this.chatReadStatus$ = this.chatService.getChatReadStatusObservable(this.chat);
    }
  }

  get displayName(): any {
    return this.chat.name ? this.chat.name : this.chat.id;
  }

}
