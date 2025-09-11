import {Component, Input} from '@angular/core';

@Component({
  selector: 'tc-description-item',
  templateUrl: './description-item.component.html',
  styleUrls: ['./description-item.component.scss']
})
export class DescriptionItemComponent {
  @Input() label!: string;
  @Input() icon?: string;
}
