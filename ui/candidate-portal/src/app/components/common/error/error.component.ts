import {Component, Input, OnInit} from '@angular/core';

@Component({
  selector: 'app-error',
  templateUrl: './error.component.html',
  styleUrls: ['./error.component.scss']
})
export class ErrorComponent implements OnInit {

  @Input() error: any;

  constructor() { }

  ngOnInit() {
  }

  ngOnChanges(){
    // console.log(this.error);

  }

}
