import {Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {CreateChatRequest, JobChat, JobChatType} from "../../../model/chat";
import {Job} from "../../../model/job";
import {ChatService} from "../../../services/chat.service";
import {Subject} from "rxjs";

@Component({
  selector: 'app-chat-read-status',
  templateUrl: './chat-read-status.component.html',
  styleUrls: ['./chat-read-status.component.scss']
})
export class ChatReadStatusComponent implements OnInit, OnChanges {

  @Input() job: Job;

  unreadIndicator: string;

  private allJobCandidatesChat: JobChat;

  userMarkedChatAsRead$ = new Subject<void>();

  constructor(
    private chatService: ChatService,
  ) { }

  ngOnInit(): void {
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (this.job) {
      this.loadChats()
    }
  }

  private loadChats() {

    const allCandidatesChatRequest: CreateChatRequest = {
      type: JobChatType.AllJobCandidates,
      jobId: this.job?.id
    }

    this.chatService.getOrCreate(allCandidatesChatRequest).subscribe(
      (jobChat) => {
        this.allJobCandidatesChat = jobChat;
        this.subscribeForChatUpdates();
      }
    )
  }

  private subscribeForChatUpdates() {
    if (this.allJobCandidatesChat) {
      this.chatService
      .subscribeChatReadStatus(this.allJobCandidatesChat).subscribe(
        (chatIsRead) => {
          this.unreadIndicator = chatIsRead ? '' : '*';
        }
      )
    }
  }
}
