import {Component, OnInit} from '@angular/core';
import {UntypedFormBuilder, UntypedFormGroup} from "@angular/forms";
import {AdminService} from "../../../services/admin.service";

@Component({
  selector: 'app-admin-api',
  templateUrl: './admin-api.component.html',
  styleUrls: ['./admin-api.component.scss']
})
export class AdminApiComponent implements OnInit {

  ack: string;
  error: any;
  form: UntypedFormGroup;

  constructor(
    private fb: UntypedFormBuilder,
    private adminService: AdminService
  ) { }

  ngOnInit(): void {
    this.form = this.fb.group({
      apicall: [],
    });
  }

  send() {
    if (this.form.value.apicall) {
      this.ack = null;
      this.error = null;
      this.adminService.call(this.form.value.apicall).subscribe(
        () => {this.ack = "Done"},
        (error) => {this.error = error}
      )
    }
  }
}
