import {Component, Input, OnInit} from '@angular/core';

@Component({
  selector: 'app-created-by',
  templateUrl: './created-by.component.html',
  styleUrls: ['./created-by.component.scss']
})
export class CreatedByComponent implements OnInit {
  @Input() object: {[key: string]: any};

  constructor() { }

  ngOnInit(): void {
  }

}
