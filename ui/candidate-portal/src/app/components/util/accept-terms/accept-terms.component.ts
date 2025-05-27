import { Component, EventEmitter, Output } from '@angular/core';

@Component({
  selector: 'app-accept-terms',
  templateUrl: './accept-terms.component.html',
  styleUrls: ['./accept-terms.component.scss']
})
export class AcceptTermsComponent {
  termsAccepted = false;
  scrolledToBottom = false;

  @Output() accepted = new EventEmitter<void>();

  onScroll(element: HTMLElement) {
    const atBottom = element.scrollTop + element.clientHeight >= element.scrollHeight;
    console.log(element.scrollTop, element.clientHeight, element.scrollTop + element.clientHeight, element.scrollHeight)
    if (atBottom) {
      this.scrolledToBottom = true;
    }
  }

  onContinue() {
    this.accepted.emit();
  }
}
