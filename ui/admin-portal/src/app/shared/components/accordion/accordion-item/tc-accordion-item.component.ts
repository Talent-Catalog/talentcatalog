import {Component, ContentChild, ElementRef, Input} from '@angular/core';
import {TcAccordionComponent} from "../tc-accordion.component";

/**
 * @component TcAccordionItemComponent
 * @selector tc-accordion-item
 *
 * @description
 * Represents a single collapsible item inside an Accordion.
 * Contains a header and a body. Can be expanded or collapsed by the parent AccordionComponent.
 *
 * **How it works**
 * - Text only accordion header: use the [header] input to pass the string for the accordion header.
 * - Custom accordion header: if need more flexibility in the accordion header
 * (e.g. display another component, buttons) then add the 'custom-header' element ref to a div in the
 * TcAccordionItemComponent. Within that `<div custom-header>` you can customize what will appear in the header (see example below)
 *
 * **Inputs**
 * - Header: The text displayed in the accordion header
 *
 * @example
 * <!-- Simple text header -->
 * <tc-accordion-item header="Panel Header">
 *   <p>Panel content goes here.</p>
 * </tc-accordion-item>
 *
 * @example
 * <!-- Custom header -->
 *  <tc-accordion-item>
 *     <div custom-header>
 *       <div class="row">
 *         <div class="col">
 *           <p>My Custom Header</p>
 *           <small>This is an example of a customized accordion header</small>
 *         </div>
 *         <div class="col-1">
 *           <tc-button>
 *             <i class="fas fa-plus"></i>
 *           </tc-button>
 *         </div>
 *       </div>
 *     </div>
 *     <p>You can sign up to the TC following these simple steps 1,2</p>
 *   </tc-accordion-item>
 *
 **/
@Component({
  selector: 'tc-accordion-item',
  templateUrl: './tc-accordion-item.component.html',
  styleUrls: ['./tc-accordion-item.component.scss']
})
export class TcAccordionItemComponent {
  /** The text displayed in the accordion header */
  @Input() header = '';
  index: number;

  /** Reference to projected custom header */
  @ContentChild('[custom-header]', { read: ElementRef }) customHeader?: ElementRef;

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
