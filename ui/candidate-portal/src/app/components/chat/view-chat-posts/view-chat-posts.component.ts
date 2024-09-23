import {
  Component,
  Input,
  OnChanges,
  OnDestroy,
  OnInit,
  QueryList,
  SimpleChanges,
  ViewChild,
  ViewChildren
} from '@angular/core';
import {CreateChatRequest, JobChatType} from "../../../model/chat";
import {Partner} from "../../../model/partner";
import {ChatService} from "../../../services/chat.service";
import {PostsComponentBase} from "../../util/chat/PostsComponentBase";
import {ChatPostService} from "../../../services/chat-post.service";
import {CreateUpdatePostComponent} from "../create-update-post/create-update-post.component";
import {ViewPostComponent} from "../view-post/view-post.component";

@Component({
  selector: 'app-view-chat-posts',
  templateUrl: './view-chat-posts.component.html',
  styleUrls: ['./view-chat-posts.component.scss']
})
export class ViewChatPostsComponent extends PostsComponentBase
  implements OnInit, OnChanges, OnDestroy {
  @Input() candidateId: number;
  @Input() jobId: number;
  @Input() jobChatType: JobChatType;
  @Input() sourcePartner: Partner;
  @Input() readOnly: boolean = false;

  @ViewChild(CreateUpdatePostComponent) editor: CreateUpdatePostComponent;
  @ViewChildren(ViewPostComponent) viewPostComponents: QueryList<ViewPostComponent>;

  constructor(
    chatService: ChatService,
    chatPostService: ChatPostService
  ) {
    super(chatService, chatPostService)
  }

  ngOnInit(): void {
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.chat) {
      if (this.chat) {
        this.onNewChat(this.chat);
      }
    } else {
      this.fetchJobChat();
    }
  }

  private fetchJobChat() {
    const request: CreateChatRequest = {
      type: this.jobChatType,
      candidateId: this.candidateId,
      jobId: this.jobId,
      sourcePartnerId: this.sourcePartner?.id
    }

    this.requestJobChat(request);
  }

  onMarkChatAsRead() {
    if (this.chat) {
      this.chatService.markChatAsRead(this.chat);
    }
  }

}
