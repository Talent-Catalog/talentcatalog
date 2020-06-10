import { Component, OnInit } from '@angular/core';
import {User} from "../../model/user";
import {FormBuilder} from "@angular/forms";
import {UserService} from "../../services/user.service";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {AuthService} from "../../services/auth.service";


@Component({
  selector: 'app-settings',
  templateUrl: './settings.component.html',
  styleUrls: ['./settings.component.scss']
})
export class SettingsComponent implements OnInit {

  loggedInUser: User;

  constructor(private authService: AuthService) { }

  ngOnInit(){
    /* GET LOGGED IN USER ROLE FROM LOCAL STORAGE */
    this.loggedInUser = this.authService.getLoggedInUser();
  }

}
