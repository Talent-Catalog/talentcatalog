import {Component, OnInit} from '@angular/core';
import {Candidate} from "../../../model/candidate";
import {CandidateService} from "../../../services/candidate.service";
import {US_AFGHAN_SURVEY_TYPE} from "../../../model/survey-type";
import {NgbNavChangeEvent} from "@ng-bootstrap/ng-bootstrap";
import {LocalStorageService} from "angular-2-local-storage";
import {CreateChatRequest, JobChat, JobChatType} from "../../../model/chat";
import {forkJoin} from "rxjs";
import {ChatService} from "../../../services/chat.service";

@Component({
  selector: 'app-view-candidate',
  templateUrl: './view-candidate.component.html',
  styleUrls: ['./view-candidate.component.scss']
})
export class ViewCandidateComponent implements OnInit {

  private lastTabKey: string = 'CandidateLastTab';
  activeTabId: string;
  chatsForAllJobs: JobChat[];
  sourceChat: JobChat;
  sourceChatHasPosts: boolean = false;

  error: any;
  loading: boolean;
  candidate: Candidate;
  usAfghan: boolean;

  constructor(private candidateService: CandidateService,
              private chatService: ChatService,
              private localStorageService: LocalStorageService) { }

  ngOnInit(): void {
    this.fetchCandidate();
    this.selectDefaultTab();
  }

  fetchCandidate() {
    this.candidateService.getProfile().subscribe(
      (candidate) => {
        this.setCandidate(candidate);
        this.candidate = candidate;
        this.usAfghan = candidate.surveyType?.id === US_AFGHAN_SURVEY_TYPE;
        this.loading = false;
      },
      (error) => {
        this.error = error;
        this.loading = false;
      });
  }

  private selectDefaultTab() {
    const defaultActiveTabID: string = this.localStorageService.get(this.lastTabKey);
    this.activeTabId = defaultActiveTabID;
  }

  onTabChanged(event: NgbNavChangeEvent) {
    this.setActiveTabId(event.nextId);
  }

  private setActiveTabId(id: string) {
    this.activeTabId = id;
    this.localStorageService.set(this.lastTabKey, id);
  }

  private setCandidate(candidate: Candidate) {
    this.candidate = candidate;
    this.fetchSourceChat();
    this.fetchAllOpportunityChats();
  }

  private fetchSourceChat() {
    const sourceChatRequest: CreateChatRequest =
      {type: JobChatType.CandidateProspect, candidateId: this.candidate.id}
    this.chatService.getOrCreate(sourceChatRequest).subscribe({
      next: chat => this.setSourceChat(chat)
    })
  }

  private setSourceChat(chat:JobChat) {
    this.sourceChat = chat;
    //todo Get chat info
    //todo If chat info is not empty, set sourceChatHasPosts to true, otherwise subscribe to posts
    //todo If post comes in, set sourceChatHasPosts to true and unsubscribe from posts
  }

  private fetchAllOpportunityChats() {

    //Get all candidate's opportunities
    let candidateOpportunities = this.candidate.candidateOpportunities;
    const candidateId = this.candidate.id;

    //Scan the opportunities to extract all their chats.
    let chats$ = candidateOpportunities
    //First map opportunities to stream of arrays of chat requests for each opportunity
    .map(opp => [
        {type: JobChatType.CandidateRecruiting, candidateId: candidateId, jobId: opp.jobOpp?.id},
        {type: JobChatType.CandidateProspect, candidateId: candidateId},
        {type: JobChatType.AllJobCandidates, jobId: opp.jobOpp?.id}
      ]
    )
    //Convert this stream of arrays of requests, into a stream of requests (ie flattening the arrays)
    .reduce((accumulator,
             requests) => accumulator.concat(requests), [])
    //Lastly map the requests to Observable<JobChat> by calling the service with each request.
    .map(request => this.chatService.getOrCreate(request));

    //Now fetch all those chats
    this.loading = true;
    this.error = null;
    forkJoin(chats$).subscribe(
      (jobChats) => {
        //Filter out duplicate chats (a CandidateProspect chat can be shared across multiple opps)
        this.chatsForAllJobs = this.chatService.removeDuplicateChats(jobChats);
        this.loading = false;
      },
      (error) => {
        this.error = error;
        this.loading = false;
      }
    )
  }

  onMarkChatAsRead() {
    this.chatService.markChatAsRead(this.sourceChat);
  }
}
