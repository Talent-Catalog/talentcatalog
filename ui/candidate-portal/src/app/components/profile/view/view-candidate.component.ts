import {Component, OnInit} from '@angular/core';
import {Candidate} from "../../../model/candidate";
import {CandidateService} from "../../../services/candidate.service";
import {US_AFGHAN_SURVEY_TYPE} from "../../../model/survey-type";
import {NgbNavChangeEvent} from "@ng-bootstrap/ng-bootstrap";
import {LocalStorageService} from "angular-2-local-storage";
import {ChatPost, CreateChatRequest, JobChat, JobChatType, JobChatUserInfo} from "../../../model/chat";
import {forkJoin, Subscription} from "rxjs";
import {ChatService} from "../../../services/chat.service";
import {CandidateOpportunityStage} from "../../../model/candidate-opportunity";

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

  //Candidate only sees source chat if is not empty. That way they can't start posting themselves
  //until someone else has posted in the chat.
  sourceChatHasPosts: boolean = false;

  //Used to unsubscribe
  private sourceChatSubscription: Subscription;

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

    //Get chat info to see whether sourceChat has any posts
    this.chatService.getJobChatUserInfo(this.sourceChat).subscribe({
        next: info => {
          this.processChatInfo(info)
        },
        error: err => {console.log(err)}
      }
    );
  }

  private processChatInfo(info: JobChatUserInfo) {
    //Has posts if lastPostId is not null
    this.sourceChatHasPosts = info.lastPostId != null;

    if (!this.sourceChatHasPosts) {
      //Empty now, but subscribe in case a post comes in

      //Get rid of any existing subscription.
      this.unsubscribeSourceChat();
      //And subscribe for posts
      this.sourceChatSubscription = this.chatService.getChatPosts$(this.sourceChat).subscribe({
          next: (post) => this.onNewPost(post)
        }
      );
    }
  }

  private onNewPost(post: ChatPost) {
    //Note that sourceChat now has posts.
    this.sourceChatHasPosts = true;

    //No longer need to subscribe for posts
    this.unsubscribeSourceChat()
  }

  private unsubscribeSourceChat() {
    if (this.sourceChatSubscription) {
      this.sourceChatSubscription.unsubscribe();
      this.sourceChatSubscription = null;
    }
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

  miniIntakeRequired() {
    // check if any of the candidate opps are in a mini intake stage
    let miniIntakeStage: boolean = false;
    for (let opp of this.candidate?.candidateOpportunities) {
      miniIntakeStage = CandidateOpportunityStage[opp.stage] === CandidateOpportunityStage.miniIntake
      if (miniIntakeStage) {
        break;
      }
    }
    return miniIntakeStage;
  }
}
