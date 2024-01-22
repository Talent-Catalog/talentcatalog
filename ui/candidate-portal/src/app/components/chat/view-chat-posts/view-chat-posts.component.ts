import {Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {CreateChatRequest, JobChatType} from "../../../model/chat";
import {ChatPostService} from "../../../services/chat-post.service";
import {ChatService} from "../../../services/chat.service";
import {PostsComponentBase} from "../../util/chat/PostsComponentBase";
import {CandidateOpportunity} from "../../../model/candidate-opportunity";
import {Job} from "../../../model/job";
import {Partner} from "../../../model/partner";

@Component({
  selector: 'app-view-chat-posts',
  templateUrl: './view-chat-posts.component.html',
  styleUrls: ['./view-chat-posts.component.scss']
})
export class ViewChatPostsComponent extends PostsComponentBase implements OnInit, OnChanges {
  @Input() candidateOpp: CandidateOpportunity;
  @Input() job: Job;
  @Input() jobChatType: JobChatType;
  @Input() sourcePartner: Partner;
  @Input() readOnly: boolean = false;

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
      candidateOppId: this.candidateOpp?.id,
      jobId: this.job?.id,
      sourcePartnerId: this.sourcePartner?.id
    }

    this.requestJobChat(request);
  }

}
