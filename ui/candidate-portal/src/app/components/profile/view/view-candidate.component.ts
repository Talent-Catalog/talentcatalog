import {Component, OnInit} from '@angular/core';
import {Candidate} from "../../../model/candidate";
import {CandidateService} from "../../../services/candidate.service";
import {US_AFGHAN_SURVEY_TYPE} from "../../../model/survey-type";
import {NgbNavChangeEvent} from "@ng-bootstrap/ng-bootstrap";
import {LocalStorageService} from "angular-2-local-storage";
import {JobChat, JobChatType} from "../../../model/chat";
import {forkJoin, Observable} from "rxjs";
import {ChatService} from "../../../services/chat.service";
import {CandidateOpportunity} from "../../../model/candidate-opportunity";

@Component({
  selector: 'app-view-candidate',
  templateUrl: './view-candidate.component.html',
  styleUrls: ['./view-candidate.component.scss']
})
export class ViewCandidateComponent implements OnInit {

  private lastTabKey: string = 'CandidateLastTab';
  activeTabId: string;
  chatsForAllJobs: JobChat[];

  error;
  loading;
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
    this.fetchAllOpportunityChats();
  }

  private fetchAllOpportunityChats() {

    //Get all candidate's opportunities
    let candidateOpportunities = this.candidate.candidateOpportunities;

    //Scan through opportunities, collecting the chat observables from each opportunity into an array.
    const collector =
      (chats$: Observable<JobChat>[], opp: CandidateOpportunity): Observable<JobChat>[] =>
      {
        const chatRequests = [
          {
            type: JobChatType.CandidateRecruiting,
            candidateOppId: opp?.id
          },
          {
            type: JobChatType.CandidateProspect,
            candidateOppId: opp?.id
          },
          {
            type: JobChatType.AllJobCandidates,
            jobId: opp?.jobOpp?.id
          }
        ];

        chatRequests.forEach(
          request => chats$.push(this.chatService.getOrCreate(request))
        );
        return chats$;
      }
    const chats$: Observable<JobChat>[] = candidateOpportunities.reduce(collector, []);

    //Now fetch all those chats
    this.loading = true;
    this.error = null;
    forkJoin(chats$).subscribe(
      (jobChats) => {this.chatsForAllJobs = jobChats; this.loading = false;},
      (error) => {
        this.error = error;
        this.loading = false;
      }
    )
  }
}
