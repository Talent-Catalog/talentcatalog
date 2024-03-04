import {
  Component,
  EventEmitter,
  Input,
  OnChanges,
  OnInit,
  Output,
  SimpleChanges
} from '@angular/core';
import {Candidate} from "../../../../../../../model/candidate";
import {
  CandidateOpportunity,
  CandidateOpportunityStage,
  isOppStageGreaterThanOrEqualTo
} from "../../../../../../../model/candidate-opportunity";
import {CreateChatRequest, JobChat, JobChatType} from "../../../../../../../model/chat";
import {ChatService} from "../../../../../../../services/chat.service";
import {forkJoin, of} from "rxjs";

const STAGE_TRANSLATION_KEY_ROOT = 'CASE-STAGE.';

@Component({
  selector: 'app-candidate-opp',
  templateUrl: './candidate-opp.component.html',
  styleUrls: ['./candidate-opp.component.scss']
})
export class CandidateOppComponent implements OnInit, OnChanges {
  @Input() selectedOpp: CandidateOpportunity;
  @Input() candidate: Candidate;
  @Output() back = new EventEmitter();
  error: any;
  loading: boolean;
  allJobCandidatesChat: JobChat;
  destinationChat: JobChat;
  sourceChat: JobChat;
  selectedChat: JobChat;
  selectedChatType: JobChatType;
  showDestinationChat: boolean;
  showAllChat: boolean;

  constructor(
    private chatService: ChatService
  ) { }

  ngOnInit(): void {
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (this.selectedOpp) {
      this.fetchJobChats();
    }
  }

  get JobChatType() {
    return JobChatType
  }

  private fetchJobChats() {
    let oppStage: number = Object.keys(CandidateOpportunityStage).indexOf(this.selectedOpp?.stage)
    // todo only if past CV review stage.
    //  How to find the number of a desired stage? I can use object keys method.
    //  If i'm using this anyways for the desired stage, do I just use this for the opp stage too instead of passing down the stage number?
    let cvReviewStage: number = Object.keys(CandidateOpportunityStage).indexOf('cvReview');
    this.showDestinationChat = isOppStageGreaterThanOrEqualTo(oppStage, cvReviewStage)
    const destinationChatRequest: CreateChatRequest = {
      type: JobChatType.CandidateRecruiting,
      candidateId: this.candidate.id,
      jobId: this.selectedOpp?.jobOpp?.id
    }
    const sourceChatRequest: CreateChatRequest = {
      type: JobChatType.CandidateProspect,
      candidateId: this.candidate.id
    }
    // todo only if past Accept Offer stage. Do we want to show for closed stages?
    let offerStage: number = Object.keys(CandidateOpportunityStage).indexOf('offer');
    this.showAllChat = isOppStageGreaterThanOrEqualTo(oppStage, offerStage);
    const allJobCandidatesChatRequest: CreateChatRequest = {
      type: JobChatType.AllJobCandidates,
      jobId: this.selectedOpp?.jobOpp?.id
    }

    forkJoin( {
      'sourceChat': this.chatService.getOrCreate(sourceChatRequest),
      'destinationChat': this.showDestinationChat ? this.chatService.getOrCreate(destinationChatRequest) : of(null),
      'allJobCandidatesChat': this.showAllChat? this.chatService.getOrCreate(allJobCandidatesChatRequest) : of(null),
    }).subscribe(
      results => {
        this.loading = false;
        this.sourceChat = results['sourceChat'];
        this.destinationChat = results['destinationChat'];
        this.allJobCandidatesChat = results['allJobCandidatesChat'];
      },
      (error) => {
        this.error = error;
        this.loading = false;
      }
    );
  }

  getChatHeaderTranslationKey(chatType: JobChatType): string {
    let key: string = null;
    switch (chatType) {
      case JobChatType.CandidateProspect:
        key = 'CANDIDATE-OPPS.TABLE.SOURCE-CHAT';
        break;

      case JobChatType.CandidateRecruiting:
        key = 'CANDIDATE-OPPS.TABLE.DESTINATION-CHAT';
        break;

      case JobChatType.AllJobCandidates:
        key = 'CANDIDATE-OPPS.TABLE.ALL-JOB-CANDIDATES-CHAT';
        break;
    }
    return key;
  }

  /**
   * Given the key of a CandidateOpportunityStage enum, return the translation key which is
   * used to display the meaning of this stage to candidates.
   * @param enumStageNameKey Key name of CandidateOpportunityStage
   * @return Translation key of stage description
   */
  getCandidateOpportunityStageTranslationKey(enumStageNameKey: string): string {
    return STAGE_TRANSLATION_KEY_ROOT + enumStageNameKey.toUpperCase();
  }

  goBack() {
    this.selectedOpp = null;
    this.back.emit();
  }

  setSelectedChatType(selectedChatType: JobChatType) {
    this.selectedChatType = selectedChatType;
    switch (this.selectedChatType) {
      case JobChatType.CandidateProspect:
        this.selectedChat = this.sourceChat;
        break;

      case JobChatType.CandidateRecruiting:
        this.selectedChat = this.destinationChat;
        break;

      case JobChatType.AllJobCandidates:
        this.selectedChat = this.allJobCandidatesChat;
        break;

      default:
        this.selectedChat = null;
    }
  }

  unSelectChat() {
    this.selectedChat = null;
  }

  onMarkChatAsRead() {
    this.chatService.markChatAsRead(this.selectedChat);
  }
}
