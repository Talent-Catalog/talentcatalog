import {AfterContentInit, Component, ContentChildren, Input, QueryList} from '@angular/core';
import {TcAccordionItemComponent} from "./accordion-item/tc-accordion-item.component";

/**
 * @component TcAccordionComponent
 * @selector tc-accordion
 * @description
 * A reusable Accordion component that contains multiple collapsible panels (TcAccordionItemComponents).
 * Panels can start all open, first open or default to all closed.
 *
 * **How it works**
 * - Wraps the TcAccordionItem components
 * - Sets the default open/close state of the panels
 * - Keeps track of which panels are opened and closed on click
 *
 * **Inputs**
 * - allOpen: if true initializes accordion with all panels opened, defaults to false.
 * - firstOpen: if true initializes accordion with first panel opened, defaults to false.
 *
 *
 * @example
 * <!-- All panels closed (default) -->
 * <tc-accordion>
 *   <tc-accordion-item title="First">
 *     <p>First content</p>
 *   </tc-accordion-item>
 *   <tc-accordion-item title="Second">
 *     <p>Second content</p>
 *   </tc-accordion-item>
 * </tc-accordion>
 *
 * @example
 * <!-- Only first panel open initially -->
 * <tc-accordion [firstOpen]="true">
 *   <tc-accordion-item title="First">
 *     <p>First content</p>
 *   </tc-accordion-item>
 *   <tc-accordion-item title="Second">
 *     <p>Second content</p>
 *   </tc-accordion-item>
 * </tc-accordion>
 *
 * @example
 * <!-- All panels open initially -->
 * <tc-accordion [allOpen]="true">
 *   <tc-accordion-item title="First">
 *     <p>First content</p>
 *   </tc-accordion-item>
 *   <tc-accordion-item title="Second">
 *     <p>Second content</p>
 *   </tc-accordion-item>
 * </tc-accordion>
 */
@Component({
  selector: 'tc-accordion',
  templateUrl: './tc-accordion.component.html',
  styleUrls: ['./tc-accordion.component.scss']
})
export class TcAccordionComponent implements AfterContentInit {
  /** Initialise with all panels opened - default false */
  @Input() allOpen = false;
  /** Initialise with first panel opened - default false */
  @Input() firstOpen = false;

  openIndexes: Set<number> = new Set();

  @ContentChildren(TcAccordionItemComponent) items!: QueryList<TcAccordionItemComponent>;

  ngAfterContentInit() {
    // Auto-assign indexes to children
    this.items.forEach((item, index) => (item.index = index));
    if (this.allOpen) {
      this.items.forEach((item) => this.openIndexes.add(item.index));
    } else if (this.firstOpen) {
      this.openIndexes.add(0);
    }
  }

  toggle(index: number) {
    if (this.openIndexes.has(index)) {
      this.openIndexes.delete(index);
    } else {
      this.openIndexes.add(index);
    }
  }

  isOpen(index: number): boolean {
    return this.openIndexes.has(index);
  }
}
