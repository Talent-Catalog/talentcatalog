import {Component, Input} from '@angular/core';
import {TcAccordionComponent} from "../tc-accordion.component";

/**
 * @component TcAccordionItemComponent
 * @selector tc-accordion-item
 *
 * @description
 * Represents a single collapsible item inside an Accordion.
 * Contains a header and a body. Can be expanded or collapsed by the parent AccordionComponent.
 *
 * @example
 * <tc-accordion-item title="Panel Title">
 *   <p>Panel content goes here.</p>
 * </tc-accordion-item>
 */
@Component({
  selector: 'app-tc-accordion-item',
  templateUrl: './tc-accordion-item.component.html',
  styleUrls: ['./tc-accordion-item.component.scss']
})
export class TcAccordionItemComponent {
  /** The text displayed in the accordion header */
  @Input() title = '';
  index: number;

  constructor(public accordion: TcAccordionComponent) {}

  toggle() {
    if (this.accordion) {
      this.accordion.toggle(this.index);
    }
  }

  get isOpen(): boolean {
    return this.accordion ? this.accordion.isOpen(this.index) : false;
  }
}
