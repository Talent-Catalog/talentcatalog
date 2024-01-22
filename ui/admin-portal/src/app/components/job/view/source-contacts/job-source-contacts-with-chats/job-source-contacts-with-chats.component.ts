import {Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {MainSidePanelBase} from "../../../../util/split/MainSidePanelBase";
import {CreateChatRequest, JobChat, JobChatType} from "../../../../../model/chat";
import {Partner} from "../../../../../model/partner";
import {Job} from "../../../../../model/job";
import {ChatService} from "../../../../../services/chat.service";
import {AuthorizationService} from "../../../../../services/authorization.service";
import {AuthenticationService} from "../../../../../services/authentication.service";

@Component({
  selector: 'app-job-source-contacts-with-chats',
  templateUrl: './job-source-contacts-with-chats.component.html',
  styleUrls: ['./job-source-contacts-with-chats.component.scss']
})
export class JobSourceContactsWithChatsComponent extends MainSidePanelBase
  implements OnInit, OnChanges {

  @Input() job: Job;
  @Input() editable: boolean;

  chatHeader: string;
  error: any;
  selectable: boolean = true;
  selectedSourcePartner: Partner;
  selectedSourcePartnerChat: JobChat;

  constructor(
      private authenticationService: AuthenticationService,
      private authorizationService: AuthorizationService,
      private chatService: ChatService,
  ) {
    super(7);
  }

  ngOnInit(): void {
    this.computeChatHeader();

    if (this.authorizationService.isSourcePartner()
        && !this.authorizationService.isDefaultSourcePartner()) {
      //Source partners (other than the default source partner) auto select and can only
      //display their chat.
      this.selectedSourcePartner = this.authenticationService.getLoggedInUser().partner;
      this.displayChat();

      //Selection can't change
      this.selectable = false;
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (this.job && this.selectedSourcePartner) {
      this.displayChat();
    }
  }

  private displayChat() {
    this.fetchJobChat();
    this.computeChatHeader();
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
      } else {
        name = ": Select partner to display chat with them"
      }
    } else if (this.authorizationService.isSourcePartner()) {
      if (this.job) {
        name = this.job.jobCreator?.name;
      }
    }
    this.chatHeader = "Private chat with " + name;
  }

  onMarkChatAsRead() {
    if (this.selectedSourcePartnerChat) {
      this.chatService.markChatAsRead(this.selectedSourcePartnerChat);
    }

  }
}
