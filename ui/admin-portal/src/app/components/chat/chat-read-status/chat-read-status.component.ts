import {Component, Input, OnChanges, OnDestroy, OnInit, SimpleChanges} from '@angular/core';
import {combineLatest, Observable, Subscription} from "rxjs";
import {ChatService} from "../../../services/chat.service";
import {JobChat} from "../../../model/chat";
import {map} from "rxjs/operators";

@Component({
  selector: 'app-chat-read-status',
  templateUrl: './chat-read-status.component.html',
  styleUrls: ['./chat-read-status.component.scss']
})
export class ChatReadStatusComponent implements OnInit, OnChanges, OnDestroy {

  @Input() chats: JobChat[];

  unreadIndicator: string;

  private subscription: Subscription;

  constructor(
    private chatService: ChatService
  ) { }

  ngOnInit(): void {
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (this.chats && this.chats.length > 0) {
      let allChatsRead =
        this.chats.find(chat => !this.chatService.isChatRead(chat)) == null;
      this.setIndicator(allChatsRead);
      this.subscribeForChatUpdates();
    }
  }

  ngOnDestroy(): void {
    this.unsubscribe();
  }

  private subscribeForChatUpdates() {
    if (this.chats) {
      this.unsubscribe();
      if (this.chats.length == 1) {
        this.subscription = this.chatService.getChatReadStatusObservable(this.chats[0]).subscribe(
          (chatIsRead) => this.setIndicator(chatIsRead)
        )
      } else if (this.chats.length > 1) {

        let x: Observable<boolean>[] = [];
        for (const chat of this.chats) {
          x.push(this.chatService.getChatReadStatusObservable(chat));
        }
        const chatReadStatus$ = combineLatest(x).pipe(
          //TODO TEST this
          //For this to return isRead to be true for all chats, none can be false
          map(statuses => statuses.find(isRead => false) == null)
        );
        this.subscription = chatReadStatus$.subscribe(
          (chatIsRead) => this.setIndicator(chatIsRead)
        )
      }
    }
  }

  private setIndicator(chatIsRead: boolean) {
    this.unreadIndicator = chatIsRead ? '' : '*';
  }

  private unsubscribe() {
    if (this.subscription) {
      this.subscription.unsubscribe();
      this.subscription = null;
    }
  }
}
