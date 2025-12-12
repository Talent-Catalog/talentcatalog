import {
  AfterContentInit,
  Component,
  ContentChildren,
  EventEmitter,
  Input,
  Output,
  QueryList
} from '@angular/core';
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
 *  - `activeIndexes`: Controls which panels are open (single index or array of indexes).
 *    Supports two-way binding using `[(activeIndexes)]`.
 *
 * **State persistence**
 *  When used with `[(activeIndexes)]`, the accordion’s open state can be stored
 *  in component state, URL parameters, or browser storage, allowing the UI to
 *  restore the same open panels after navigation or page reload.
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
 *
 * @example
 * <!-- Controlled accordion with persisted open state -->
 * <tc-accordion [(activeIndexes)]="openIndexes">
 *   <tc-accordion-item header="First">
 *     <p>First content</p>
 *   </tc-accordion-item>
 *   <tc-accordion-item header="Second">
 *     <p>Second content</p>
 *   </tc-accordion-item>
 * </tc-accordion>
 *
 * // Component TS
 * openIndexes = [0];
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

  /**
   * Controls which accordion panels are currently open.
   *
   * This input allows the open state of the accordion to be controlled
   * from outside the component and kept in sync with the parent.
   *
   * It supports:
   * - a single panel index (number)
   * - multiple panel indexes (number[])
   * - null or undefined to close all panels
   *
   * Using two-way binding with [(activeIndexes)] makes it possible to
   * persist the open panels (for example in component state, URL params,
   * or browser storage) so the accordion can restore its open state
   * after navigation or page reload.
   */
  @Input() set activeIndexes(val: number[] | number | null | undefined) {
    this.openIndexes.clear();
    if (Array.isArray(val)) {
      val.forEach(i => this.openIndexes.add(i));
    } else if (val !== null && val !== undefined) {
      this.openIndexes.add(val as number);
    }
  }
  /**
   * Returns the indexes of the panels that are currently open.
   *
   * This getter is used by Angular as part of the [(activeIndexes)]
   * two-way binding to expose the current accordion state.
   */
  get activeIndexes(): number[] {
    return Array.from(this.openIndexes.values());
  }
  /**
   * Emits the current open panel indexes whenever the accordion state changes.
   *
   * This allows parent components to react to changes and optionally
   * store the open state (for example so the browser "remembers"
   * which panels were open).
   */
  @Output() activeIndexesChange = new EventEmitter<number[]>();

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

  /**
   * Emits the current open panel indexes.
   *
   * Called internally after any state change (toggle, openAll, closeAll)
   * to keep external bindings in sync.
   */
  private emitActiveIndexes() {
    this.activeIndexesChange.emit(Array.from(this.openIndexes.values()));
  }

  toggle(index: number) {
    if (this.openIndexes.has(index)) {
      this.openIndexes.delete(index);
    } else {
      this.openIndexes.add(index);
    }
    this.emitActiveIndexes();
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
    this.emitActiveIndexes();
  }

  closeAll() {
    this.openIndexes.clear();
    this.emitActiveIndexes();
  }
}
