import { Component, Input } from '@angular/core';

@Component({
  selector: 'tc-label',
  templateUrl: './label.component.html',
  styleUrls: ['./label.component.scss']
})
export class LabelComponent {
  @Input() for?: string; // to associate with input by id
}
