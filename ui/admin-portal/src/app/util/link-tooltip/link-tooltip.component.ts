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

  @Output() editClicked = new EventEmitter<string>();
  @Output() removeClicked = new EventEmitter<string>();

  constructor() { }

  ngOnInit(): void {
  }

  public onEditClick() {
    this.editClicked.emit(this.url)
  }

  public onRemoveClick() {
    this.removeClicked.emit(this.url)
  }

}
