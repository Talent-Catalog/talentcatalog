import {Component, Input} from '@angular/core';
import {ServiceAssignment} from "../../../../../../../model/services";

@Component({
  selector: 'app-linkedin-redeemed',
  templateUrl: './linkedin-redeemed.component.html',
  styleUrl: './linkedin-redeemed.component.scss'
})
export class LinkedinRedeemedComponent {
  @Input() assignment: ServiceAssignment;
}
