import {Component, EventEmitter, Input, Output} from '@angular/core';

@Component({
  selector: 'app-accept-terms',
  templateUrl: './accept-terms.component.html',
  styleUrls: ['./accept-terms.component.scss']
})
export class AcceptTermsComponent {
  @Input() termsAccepted = false;
  @Input() acceptanceText = "I accept the Terms and Conditions";
  @Output() accepted = new EventEmitter<boolean>();

  scrolledToBottom = false;

  onScroll(element: HTMLElement) {
    const atBottom = element.scrollTop + element.clientHeight >= element.scrollHeight;
    console.log(element.scrollTop, element.clientHeight, element.scrollTop + element.clientHeight, element.scrollHeight)
    if (atBottom) {
      this.scrolledToBottom = true;
    }
  }

  onAcceptanceChange($event: Event) {
    const checked = ($event.target as HTMLInputElement).checked;
    this.accepted.emit(checked)
  }
}
