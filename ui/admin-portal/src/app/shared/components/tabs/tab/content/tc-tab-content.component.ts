import {Component, TemplateRef, ViewChild} from '@angular/core';

@Component({
  selector: 'tc-tab-content',
  templateUrl: './tc-tab-content.component.html',
  styleUrls: ['./tc-tab-content.component.scss']
})
export class TcTabContentComponent {
  @ViewChild(TemplateRef, { static: true }) template!: TemplateRef<any>;
}
