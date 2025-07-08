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
    console.log('emit active tab')
    if (this.selectedTab) {
      this.tabChanged.emit(this.selectedTab.id);
    }
  }

  get selectedTab() {
    return this.tabs[this.activeIndex];
  }
}
