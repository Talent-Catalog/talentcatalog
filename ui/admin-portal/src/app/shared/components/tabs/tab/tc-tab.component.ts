import {AfterContentInit, Component, ContentChild, Input, TemplateRef} from '@angular/core';
import {TcTabHeaderComponent} from "./header/tc-tab-header.component";
import {TcTabContentComponent} from "./content/tc-tab-content.component";

/**
 * @component TcTabComponent
 * @description
 * An outer component that contains the <tc-tab-header> and <tc-tab-content> components. It sets the tab id and the
 * description of the tab which will be displayed in a popover.
 *
 * **Features:**
 * - Contains the tab header and tab content
 * - Sets the id that is used for setting the active tab id in the TcTabsComponent
 * - Sets a description for the tab that is displayed when tab hovered over
 *
 * @selector tc-tab
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
  selector: 'tc-tab',
  templateUrl: './tc-tab.component.html',
  styleUrls: ['./tc-tab.component.scss']
})
export class TcTabComponent implements AfterContentInit {
  @Input() id: string;
  @Input() description: string;

  @ContentChild(TcTabHeaderComponent) headerComponent: TcTabHeaderComponent;
  @ContentChild(TcTabContentComponent) contentComponent: TcTabContentComponent;

  header!: TemplateRef<any>;
  content!: TemplateRef<any>;


  ngAfterContentInit() {
    if (this.headerComponent) {
      this.header = this.headerComponent.template;
    }
    if (this.contentComponent) {
      this.content = this.contentComponent.template;
    }
  }

}
