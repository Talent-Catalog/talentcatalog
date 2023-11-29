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
  sourceChat: JobChat;

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
    this.fetchJobChat();
    this.computeChatHeader();
  }


  private computeChatHeader() {
     //todo
  }

  private fetchJobChat() {
    const request: CreateChatRequest = {
      type: JobChatType.CandidateProspect,
      candidateOppId: this.selectedOpp?.id
    }

    this.error = null;
    this.chatService.getOrCreate(request).subscribe(
      (chat) => {this.sourceChat = chat},
      (error) => {this.error = error}
    )

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
}
