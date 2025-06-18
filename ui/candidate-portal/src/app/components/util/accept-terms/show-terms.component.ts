import {Component, EventEmitter, Input, Output} from '@angular/core';

@Component({
  selector: 'app-show-terms',
  templateUrl: './show-terms.component.html',
  styleUrls: ['./show-terms.component.scss']
})
export class ShowTermsComponent {
  @Input() requestTermsRead = false;
  @Output() termsRead = new EventEmitter<boolean>();

  scrolledToBottom = false;

  onScroll(element: HTMLElement) {
    //Add an extra 0.5 to scroll top - sometimes it is less than it should be at the bottom of the
    //scroll.
    const atBottom = element.scrollTop + 0.5 + element.clientHeight >= element.scrollHeight;
    // console.log(element.scrollTop, element.clientHeight, element.scrollTop + element.clientHeight, element.scrollHeight)
    if (atBottom) {
      this.scrolledToBottom = true;
      this.termsRead.emit(true);
    }
  }
}
