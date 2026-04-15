import {Component, TemplateRef, ViewChild} from '@angular/core';

/**
 * @component TcTabContentComponent
 * @description
 * A wrapper component that contains the content to be displayed if the tab is active
 *
 * **Features:**
 * - Only displays content if tab id is active id
 *
 * @selector tc-tab-content
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
  selector: 'tc-tab-content',
  templateUrl: './tc-tab-content.component.html',
  styleUrls: ['./tc-tab-content.component.scss']
})
export class TcTabContentComponent {
  @ViewChild(TemplateRef, { static: true }) template!: TemplateRef<any>;
}
