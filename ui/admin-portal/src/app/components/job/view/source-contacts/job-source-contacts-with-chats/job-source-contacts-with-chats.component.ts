import {Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {MainSidePanelBase} from "../../../../util/split/MainSidePanelBase";
import {CreateChatRequest, JobChat, JobChatType} from "../../../../../model/chat";
import {Partner} from "../../../../../model/partner";
import {Job} from "../../../../../model/job";
import {ChatService} from "../../../../../services/chat.service";
import {AuthorizationService} from "../../../../../services/authorization.service";

@Component({
  selector: 'app-job-source-contacts-with-chats',
  templateUrl: './job-source-contacts-with-chats.component.html',
  styleUrls: ['./job-source-contacts-with-chats.component.scss']
})
export class JobSourceContactsWithChatsComponent extends MainSidePanelBase
  implements OnInit, OnChanges {

  @Input() job: Job;
  @Input() editable: boolean;

  chatHeader: string = "";
  error: any;
  selectedSourcePartner: Partner;
  selectedSourcePartnerChat: JobChat;

  constructor(
      private authorizationService: AuthorizationService,
      private chatService: ChatService,
  ) {
    super(6);
  }

  ngOnInit(): void {
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (this.job && this.selectedSourcePartner) {
      this.fetchJobChat();
      this.computeChatHeader();
    }
  }

  private fetchJobChat() {
    const request: CreateChatRequest = {
      type: JobChatType.JobCreatorSourcePartner,
      jobId: this.job?.id,
      sourcePartnerId: this.selectedSourcePartner?.id
    }

    this.error = null;
    this.chatService.getOrCreate(request).subscribe(
      (chat) => {this.selectedSourcePartnerChat = chat},
      (error) => {this.error = error}
    )
  }

  onSourcePartnerSelected(sourcePartner: Partner) {
    this.selectedSourcePartner = sourcePartner;
    this.fetchJobChat();
    this.computeChatHeader();
  }

  private computeChatHeader() {
    let name: string = "";

    if (this.authorizationService.isJobCreator()) {
      if (this.selectedSourcePartner) {
        name = this.selectedSourcePartner.name;
      }
    } else if (this.authorizationService.isSourcePartner()) {
      if (this.job) {
        name = this.job.jobCreator?.name;
      }
    }
    this.chatHeader = "Chat " + name;
  }
}
