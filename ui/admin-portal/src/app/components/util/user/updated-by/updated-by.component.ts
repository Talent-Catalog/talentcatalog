import {Component, Input, OnInit} from '@angular/core';
import {User} from "../../../../model/user";

@Component({
  selector: 'app-updated-by',
  templateUrl: './updated-by.component.html',
  styleUrls: ['./updated-by.component.scss']
})
export class UpdatedByComponent implements OnInit {

  @Input() createdBy: User;
  @Input() updatedBy: User;
  @Input() createdDate;
  @Input() updatedDate;


  constructor() {
  }

  ngOnInit() {
    console.log('createdBy', this.createdBy);
    console.log('updatedBy', this.updatedBy);


  }

}
