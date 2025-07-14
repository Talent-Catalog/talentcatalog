import { Component, Input } from '@angular/core';

@Component({
  selector: 'tc-fieldset',
  templateUrl: './fieldset.component.html',
  styleUrls: ['./fieldset.component.scss']
})
export class FieldsetComponent {
  @Input() disabled = false;
}
