import {Component, Input, OnInit} from '@angular/core';

@Component({
  selector: 'app-updated-by',
  templateUrl: './updated-by.component.html',
  styleUrls: ['./updated-by.component.scss']
})
export class UpdatedByComponent implements OnInit {

  @Input() object: {[key: string]: any};

  constructor() {
  }

  ngOnInit() {
  }

}
