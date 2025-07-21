import { Component, Input } from '@angular/core';

@Component({
  selector: 'tc-description-list',
  templateUrl: './description-list.component.html',
  styleUrls: ['./description-list.component.scss']
})
export class DescriptionListComponent {
  @Input() direction: 'row' | 'column' = 'row';
}
