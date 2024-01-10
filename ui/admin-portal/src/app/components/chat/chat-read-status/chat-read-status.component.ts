import {Component, Input, OnChanges, OnDestroy, OnInit, SimpleChanges} from '@angular/core';
import {Observable, Subscription} from "rxjs";

@Component({
  selector: 'app-chat-read-status',
  templateUrl: './chat-read-status.component.html',
  styleUrls: ['./chat-read-status.component.scss']
})
export class ChatReadStatusComponent implements OnInit, OnChanges, OnDestroy {

  @Input() chatReadStatus$: Observable<boolean>;

  unreadIndicator: string;

  private subscription: Subscription;

  constructor() { }

  ngOnInit(): void {
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (this.chatReadStatus$ && !this.subscription) {
      this.subscribeForChatUpdates();
    }
  }

  ngOnDestroy(): void {
    if (this.subscription) {
      this.subscription.unsubscribe();
    }
  }

  private subscribeForChatUpdates() {
    if (this.chatReadStatus$) {
      this.subscription = this.chatReadStatus$.subscribe(
        (chatIsRead) => {
          this.unreadIndicator = chatIsRead ? '' : '*';
        }
      )
    }
  }
}
