import {Component, Input} from '@angular/core';

@Component({
  selector: 'app-tc-tab',
  templateUrl: './tc-tab.component.html',
  styleUrls: ['./tc-tab.component.scss']
})
export class TcTabComponent {
  @Input() name: string;
  @Input() description: string;
  @Input() iconClass: string;


}
