import {Component, Input, Output, EventEmitter} from '@angular/core';
import {CandidateService} from "../../../services/candidate.service";
import {UpdateCandidateMutedRequest} from "../../../model/candidate";


@Component({
  selector: 'app-chat-mute-toggle-button',
  templateUrl: './chat-mute-toggle-button.component.html',
})
export class ChatMuteToggleButtonComponent {
  @Input() candidate: any;
  @Input() showTooltip: boolean = true;
  @Output() mutedToggled = new EventEmitter<boolean>();
  @Input() refreshAfterToggle: boolean = false;

  public error: any;

  constructor(private candidateService: CandidateService) {}

  public computeMuteButtonLabel() {
    return (this.candidate?.muted ? 'Unmute' : 'Mute') + ' Candidate';
  }

  public toggleMuted() {
    this.error = null;
    const request: UpdateCandidateMutedRequest = {
      muted: !this.candidate.muted
    };

    this.candidateService.updateMuted(this.candidate.id, request).subscribe({
      next: () => {
        this.candidate.muted = request.muted;
        if (this.refreshAfterToggle) {
          this.mutedToggled.emit();
        }
      },
      error: (err) => {
        this.error = err;
      }
    });
  }
}
