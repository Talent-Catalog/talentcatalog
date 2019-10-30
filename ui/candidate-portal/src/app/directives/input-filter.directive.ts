import {Directive, ElementRef, HostListener, Input} from '@angular/core';

/*
** A directive designed to be used on input[type="text"] elements to filter text based on the provided regular
** expression.
**/

@Directive({
  selector: '[appInputFilter]'
})
export class InputFilterDirective {

  // A regular expression to determine which characters should be filtered out
  // Default: Non-digit characters
  @Input() filterPattern: any = /[^0-9]*/g;

  // List for oninput events and filter out characters based on pattern
  @HostListener('input', ['$event']) onInputChange(event) {
    const val = this._el.nativeElement.value;
    this._el.nativeElement.value = val.replace(this.filterPattern, '');
    // Stop the input event from propagating to the element
    if (val !== this._el.nativeElement.value) {
      event.stopPropagation();
    }
  }

  constructor(private _el: ElementRef) { }

}
