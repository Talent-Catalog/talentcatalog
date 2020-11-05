import {Component, Input, OnInit} from '@angular/core';

@Component({
  selector: 'app-autosave-status',
  templateUrl: './autosave-status.component.html',
  styleUrls: ['./autosave-status.component.scss']
})
export class AutosaveStatusComponent implements OnInit {
  @Input() saving: boolean;
  @Input() typing: boolean;

  constructor() { }

  ngOnInit(): void {
  }

}
