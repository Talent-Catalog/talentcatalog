import {
  Component,
  EventEmitter,
  Input,
  OnChanges,
  OnInit,
  Output,
  SimpleChanges
} from '@angular/core';
import {Job} from "../../../../../model/job";
import {CreateChatRequest, JobChat, JobChatType} from "../../../../../model/chat";
import {ChatService} from "../../../../../services/chat.service";
import {combineLatest, forkJoin, Observable} from "rxjs";
import {AuthorizationService} from "../../../../../services/authorization.service";
import {map} from "rxjs/operators";

@Component({
  selector: 'app-job-group-chats-tab',
  templateUrl: './job-group-chats-tab.component.html',
  styleUrls: ['./job-group-chats-tab.component.scss']
})
export class JobGroupChatsTabComponent implements OnInit, OnChanges {
  @Input() job: Job;
  @Output() chatReadStatusCreated = new EventEmitter<Observable<boolean>>();
  chats: JobChat[];

  error: any;
  loading: boolean;

  constructor(
      private chatService: ChatService,
      private authorizationService: AuthorizationService
    ) { }

  ngOnInit(): void {
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (this.job) {
      this.fetchJobChats();
    }
  }

  private fetchJobChats() {
    const allCandidatesChatRequest: CreateChatRequest = {
      type: JobChatType.AllJobCandidates,
      jobId: this.job?.id
    }
    const allSourcePartnersChatRequest: CreateChatRequest = {
      type: JobChatType.JobCreatorAllSourcePartners,
      jobId: this.job?.id
    }

    forkJoin( {
      'allJobCandidatesChat': this.chatService.getOrCreate(allCandidatesChatRequest),
      'allSourcePartnersChat': this.chatService.getOrCreate(allSourcePartnersChatRequest),
    }).subscribe(
      results => {
        this.loading = false;

        const allJobCandidatesChat = results['allJobCandidatesChat'];
        allJobCandidatesChat.name = "All associated with job plus candidates who have accepted job offers";

        const allSourcePartnersChat = results['allSourcePartnersChat'];
        if (this.authorizationService.isSourcePartner()) {
          allSourcePartnersChat.name = this.job.jobCreator.name + " and all source partners";
        } else {
          allSourcePartnersChat.name = "All source partners";
        }

        this.chats = [allJobCandidatesChat, allSourcePartnersChat];

        let x: Observable<boolean>[] = [];
        for (const chat of this.chats) {
          x.push(this.chatService.getChatReadStatusObservable(chat));
        }
        const chatReadStatus$ = combineLatest(x).pipe(
          map(statuses => statuses[0] && statuses[1])
        );

        this.chatReadStatusCreated.emit(chatReadStatus$);

      },
      (error) => {
        this.error = error;
        this.loading = false;
      }
    );
  }

}
