import {Component, Input, OnChanges, OnDestroy, OnInit, SimpleChanges} from '@angular/core';
import {combineLatest, Subscription} from "rxjs";
import {ChatService} from "../../../services/chat.service";
import {JobChat} from "../../../model/chat";
import {map} from "rxjs/operators";

/**
 * Component which takes an array of chats and sets the unreadIndicator to unread ('*") if
 * any of the chats is unread.
 * Otherwise it sets the unread indicator to blank.
 */
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

        //Construct array of chat read statues from array of chats
        let chatReadStatuses$ = this.chats.map(
          (chat) => this.chatService.getChatReadStatusObservable(chat));

        //Combine the latest values of all the statuses and return a single status which is true
        //only if all are true (ie none are false)
        const chatReadStatus$ = combineLatest(chatReadStatuses$).pipe(
          //For isRead to be true, no chats can be false (unread)
          map(statuses =>  statuses.find(isRead => isRead == false) == null)
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
