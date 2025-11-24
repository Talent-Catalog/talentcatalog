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
 * - Each <tc-accordion-item> inside this component is automatically assigned an index, which is used to control its open/close state.
 *
 * **Inputs**
 * - allOpen: if true initializes accordion with all panels opened, defaults to false.
 * - firstOpen: if true initializes accordion with first panel opened, defaults to false.
 * - showOpenCloseAll: if true displays a button which toggles all panels to open/close.
 *
 *
 * @example
 * <!-- All panels closed (default) -->
 * <tc-accordion>
 *   <tc-accordion-item header="First">
 *     <p>First content</p>
 *   </tc-accordion-item>
 *   <tc-accordion-item header="Second">
 *     <p>Second content</p>
 *   </tc-accordion-item>
 * </tc-accordion>
 *
 * @example
 * <!-- Only first panel open initially -->
 * <tc-accordion [firstOpen]="true">
 *   <tc-accordion-item header="First">
 *     <p>First content</p>
 *   </tc-accordion-item>
 *   <tc-accordion-item header="Second">
 *     <p>Second content</p>
 *   </tc-accordion-item>
 * </tc-accordion>
 *
 * @example
 * <!-- All panels open initially -->
 * <tc-accordion [allOpen]="true">
 *   <tc-accordion-item header="First">
 *     <p>First content</p>
 *   </tc-accordion-item>
 *   <tc-accordion-item header="Second">
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
  /** Display the open all and close all panel buttons - default true */
  @Input() showOpenCloseAll: boolean = true;
  /** Enables passing true when visible overflow is required (e.g. dropdown options cut off by next element */
  @Input() allowOverflow = false;

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

  toggleAll() {
    if (this.openIndexes.size > 0) {
      this.closeAll();
    } else {
      this.openAll();
    }
  }

  openAll() {
    this.items.forEach((item) => this.openIndexes.add(item.index));
  }

  closeAll() {
    this.openIndexes.clear();
  }
}
