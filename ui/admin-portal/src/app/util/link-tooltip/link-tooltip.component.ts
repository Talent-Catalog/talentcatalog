import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';

@Component({
  selector: 'app-link-tooltip',
  templateUrl: './link-tooltip.component.html',
  styleUrls: ['./link-tooltip.component.scss']
})
export class LinkTooltipComponent implements OnInit {

  @Input() xPosition: number;
  @Input() yPosition: number;
  @Input() url: string;

  @Output() onEditClick = new EventEmitter();
  @Output() onRemoveClick = new EventEmitter();

  constructor() { }

  ngOnInit(): void {
  }


}
