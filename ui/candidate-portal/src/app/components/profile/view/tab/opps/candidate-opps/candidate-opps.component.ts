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
import {
  CandidateOpportunity,
  getCandidateOpportunityChatRequests
} from "../../../../../../model/candidate-opportunity";
import {forkJoin, Observable} from "rxjs";
import {JobChat} from "../../../../../../model/chat";
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
    const collector =
      (chats$: Observable<JobChat>[], opp: CandidateOpportunity): Observable<JobChat>[] =>
      {
        const chatRequests = getCandidateOpportunityChatRequests(opp);
        chatRequests.forEach(
          request => chats$.push(this.chatService.getOrCreate(request))
        );
        return chats$;
      }
    const chats$: Observable<JobChat>[] = [opp].reduce(collector, []);

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
