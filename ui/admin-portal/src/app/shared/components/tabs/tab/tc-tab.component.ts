import {AfterContentInit, Component, ContentChild, Input, TemplateRef} from '@angular/core';
import {TcTabHeaderComponent} from "./header/tc-tab-header.component";
import {TcTabContentComponent} from "./content/tc-tab-content.component";

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
