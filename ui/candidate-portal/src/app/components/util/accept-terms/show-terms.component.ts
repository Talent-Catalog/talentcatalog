import {AfterViewInit, Component, ElementRef, EventEmitter, Input, Output, ViewChild} from '@angular/core';

@Component({
  selector: 'app-show-terms',
  templateUrl: './show-terms.component.html',
  styleUrls: ['./show-terms.component.scss']
})
export class ShowTermsComponent implements AfterViewInit {
  @Input() requestTermsRead = false;
  @Output() termsRead = new EventEmitter<boolean>();
  @ViewChild('termsBox') termsBox: ElementRef<HTMLElement>;

  scrolledToBottom = false;

  ngAfterViewInit(): void {
    if (!this.requestTermsRead || !this.termsBox) {
      return;
    }

    const element = this.termsBox.nativeElement;
    if (element.scrollHeight <= element.clientHeight) {
      this.scrolledToBottom = true;
      this.termsRead.emit(true);
    }
  }

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
