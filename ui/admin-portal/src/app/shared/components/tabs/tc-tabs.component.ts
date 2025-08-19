import {
  AfterContentInit,
  Component,
  ContentChildren,
  EventEmitter,
  Input,
  Output,
  QueryList,
  TemplateRef
} from '@angular/core';
import {TcTabComponent} from "./tab/tc-tab.component";

export interface Tab {
  id: string;
  description: string;
  header: TemplateRef<any>;
  content: TemplateRef<any>;
}

/**
 * @component TcTabsComponent
 * @description
 * An outer component that contains all the <tc-tab> components. It keeps track of the active tab and outputs event
 * on tab change.
 *
 * **Features:**
 * - Contains the tabs
 * - Outputs event on tab change
 * - Sets with the active tab
 *
 * @selector tc-tabs
 *
 * @example
 * ```html
 * <tc-tabs [activeTabId]="activeTabId" (tabChanged)="setActiveTab($event)">
 *   <tc-tab id="FirstTab" description="This is the first tab">
 *     <tc-tab-header>First Tab</tc-tab-header>
 *     <tc-tab-content>
 *       <app-component></app-component>
 *     </tc-tab-content>
 *   </tc-tab>
 *   <tc-tab id="SecondTab" description="This is the second tab">
 *     <tc-tab-header>Second Tab</tc-tab-header>
 *     <tc-tab-content>
 *       This is some content that isn't in a component.
 *     </tc-tab-content>
 *   </tc-tab>
 * </tc-tabs>
 * ```
 */
@Component({
  selector: 'tc-tabs',
  templateUrl: './tc-tabs.component.html',
  styleUrls: ['./tc-tabs.component.scss']
})
export class TcTabsComponent implements AfterContentInit {
  @Input() activeTabId: string;
  @Output() tabChanged = new EventEmitter<any>();

  @ContentChildren(TcTabComponent) tabComponents!: QueryList<TcTabComponent>;
  tabs: Tab[];
  activeIndex = 0;

  ngAfterContentInit() {
    this.tabs = this.tabComponents.map(tab => ({
      id: tab.id,
      description: tab.description,
      header: tab.header,
      content: tab.content,
    }));
    if (this.activeTabId) {
      const found = this.tabs.findIndex(tab => tab.id === this.activeTabId);
      this.activeIndex = found >= 0 ? found : 0;
    }
    console.log('content init')
    this.emitActiveTab();
  }

  selectTab(index: number) {
    this.activeIndex = index;
    console.log('select tab')
    this.emitActiveTab();
  }

  private emitActiveTab() {
    if (this.selectedTab) {
      this.tabChanged.emit(this.selectedTab.id);
    }
  }

  get selectedTab() {
    return this.tabs[this.activeIndex];
  }
}
