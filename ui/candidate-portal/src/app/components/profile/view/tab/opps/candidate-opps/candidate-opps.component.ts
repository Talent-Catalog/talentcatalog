import {
  Component,
  EventEmitter,
  Input,
  OnChanges,
  OnInit,
  Output,
  SimpleChanges
} from '@angular/core';
import {Candidate} from "../../../../../../model/candidate";
import {CandidateOpportunity} from "../../../../../../model/candidate-opportunity";
import {forkJoin, Observable} from "rxjs";
import {JobChat, JobChatType} from "../../../../../../model/chat";
import {ChatService} from "../../../../../../services/chat.service";

@Component({
  selector: 'app-candidate-opps',
  templateUrl: './candidate-opps.component.html',
  styleUrls: ['./candidate-opps.component.scss']
})
export class CandidateOppsComponent implements OnInit, OnChanges {
  error: string;
  loading: boolean;
  @Input() candidate: Candidate;
  @Output() refresh = new EventEmitter();

  //Map of job chats by opp id
  private mapOppChatsByOppId: Map<number, JobChat[]> = new Map()

  selectedOpp: CandidateOpportunity;

  constructor(
    private chatService: ChatService
  ) { }

  ngOnInit(): void {
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.candidate) {
      this.fetchChats();
    }
  }

  get opps(): CandidateOpportunity[] {
    return this.candidate?.candidateOpportunities;
  }

  selectOpp(opp: CandidateOpportunity) {
    this.selectedOpp = opp;
  }

  unSelectOpp() {
    this.selectedOpp = null;
    this.refresh.emit();
  }

  getOppChats(opp: CandidateOpportunity): JobChat[] {
    return this.mapOppChatsByOppId.get(opp.id)
  }

  fetchOppChats(opp: CandidateOpportunity) {
    let chats$: Observable<JobChat>[] = [];

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

    for (const request of chatRequests) {
      chats$.push(this.chatService.getOrCreate(request))
    }

    //Now fetch all those chats
    this.loading = true;
    this.error = null;
    forkJoin(chats$).subscribe(
      (jobChats) => {this.mapOppChatsByOppId.set(opp.id, jobChats); this.loading = false;},
      (error) => {
        this.error = error;
        this.loading = false;
      }
    )
  }

  private fetchChats() {
    let opportunities = this.candidate.candidateOpportunities;
    for (const opp of opportunities) {
      this.fetchOppChats(opp);
    }
  }
}
