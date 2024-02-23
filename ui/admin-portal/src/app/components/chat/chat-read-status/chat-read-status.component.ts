import {Component, Input, OnChanges, OnDestroy, OnInit, SimpleChanges} from '@angular/core';
import {Observable, Subscription} from "rxjs";
import {ChatService} from "../../../services/chat.service";
import {JobChat} from "../../../model/chat";

/**
 * Component can take as input and array of chats or an Observable<boolean> - but not both.
 * <p/>
 * If input is an array of chats, it sets the unreadIndicator to unread ('*') if
 * any of the chats is unread.
 * If all chats are read it sets the unread indicator to blank.
 * If we don't know the status of one or more chats is sets the indicator to '?'.
 * <p/>
 * If input is an Observable<boolean>, a true value will set the indicator to blank (ie read),
 * false will set the indicator to '*' (unread). Until the time it has received a value,
 * the indicator will be set to '?' (unknown).
 */
@Component({
  selector: 'app-chat-read-status',
  templateUrl: './chat-read-status.component.html',
  styleUrls: ['./chat-read-status.component.scss']
})
export class ChatReadStatusComponent implements OnInit, OnChanges, OnDestroy {

  @Input() chats: JobChat[];

  @Input() observable: Observable<boolean>;

  unreadIndicator: string;

  private subscription: Subscription;

  constructor(
    private chatService: ChatService
  ) { }

  ngOnInit(): void {
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.chats) {
      this.subscribeForChatUpdates();
      this.computeIndicatorFromStaticStatus();
    } else if (changes.observable) {
      this.subscribeToObservable(this.observable);
    }
  }

  computeIndicatorFromStaticStatus() {
    if (this.chats == null) {
      this.setIndicator(null);
    } else {
      //If any of the chats are don't know, then we don't now
      if (this.chats && this.chats.length > 0) {
        const dontKnow =
          this.chats.find(chat => this.chatService.isChatRead(chat) == null) != null;
        if (dontKnow) {
          this.setIndicator(null);
        } else {
          //All read is true if none of them are false
          let allChatsRead =
            this.chats.find(chat => !this.chatService.isChatRead(chat)) == null;
          this.setIndicator(allChatsRead);
        }
      }
    }
  }

  ngOnDestroy(): void {
    this.unsubscribe();
  }

  private subscribeForChatUpdates() {
    if (this.chats) {
      this.unsubscribe();
      if (this.chats.length == 1) {

        this.subscribeToObservable(this.chatService.getChatIsRead$(this.chats[0]));

      } else if (this.chats.length > 1) {
        //Construct single observable to monitor.
        const chatReadStatus$
          = this.chatService.combineChatReadStatuses(this.chats);
        console.log('ChatReadStatus subscribing to chats' + this.chats.map(chat => chat.id).join(','))
        this.subscribeToObservable(chatReadStatus$)
      }
    }
  }

  private subscribeToObservable(observable: Observable<boolean>) {
    this.unsubscribe();
    if (observable) {
      this.subscription = observable.subscribe({
          next: chatIsRead => {
            this.setIndicator(chatIsRead);
          },
          error: error => this.setIndicator(null)
        }
      )
    }
  }

  private setIndicator(chatIsRead: boolean) {
    if (chatIsRead == null) {
      this.unreadIndicator = '?'
    } else {
      this.unreadIndicator = chatIsRead ? '' : '*';
    }
  }

  private unsubscribe() {
    if (this.subscription) {
      this.subscription.unsubscribe();
      this.subscription = null;
    }
  }
}
