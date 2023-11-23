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
import {CandidateOpportunity} from "../../../../../../../model/candidate-opportunity";
import {CreateChatRequest, JobChat, JobChatType} from "../../../../../../../model/chat";
import {ChatService} from "../../../../../../../services/chat.service";
import {forkJoin} from "rxjs";

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
  destinationChat: JobChat;
  sourceChat: JobChat;
  selectedChat: JobChat;
  showingSourceChat: boolean;

  constructor(
    private chatService: ChatService
  ) { }

  ngOnInit(): void {
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (this.selectedOpp) {
      this.displayChat();
    }
  }


  private displayChat() {
    this.fetchJobChats();
  }

  private fetchJobChats() {
    const destinationChatRequest: CreateChatRequest = {
      type: JobChatType.CandidateRecruiting,
      candidateOppId: this.selectedOpp?.id
    }
    const sourceChatRequest: CreateChatRequest = {
      type: JobChatType.CandidateProspect,
      candidateOppId: this.selectedOpp?.id
    }

    forkJoin( {
      'sourceChat': this.chatService.getOrCreate(sourceChatRequest),
      'destinationChat': this.chatService.getOrCreate(destinationChatRequest),
    }).subscribe(
      results => {
        this.loading = false;
        this.sourceChat = results['sourceChat'];
        this.destinationChat = results['destinationChat'];
      },
      (error) => {
        this.error = error;
        this.loading = false;
      }
    );
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

  setShowingSourceChat(showingSourceChat: boolean) {
    this.showingSourceChat = showingSourceChat;
    this.selectedChat = showingSourceChat ? this.sourceChat : this.destinationChat;
  }

  unSelectChat() {
    this.selectedChat = null;
  }
}
