import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {User} from "../../../../model/user";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {UserService} from "../../../../services/user.service";
import {AuthService} from "../../../../services/auth.service";

@Component({
  selector: 'app-edit-user',
  templateUrl: './edit-user.component.html',
  styleUrls: ['./edit-user.component.scss']
})
export class EditUserComponent implements OnInit {

  userId: number;
  userForm: FormGroup;
  error;
  loading: boolean;
  saving: boolean;

  constructor(private activeModal: NgbActiveModal,
              private fb: FormBuilder,
              private userService: UserService,
              private authService: AuthService) {
  }

  ngOnInit() {
    this.loading = true;
    this.userService.get(this.userId).subscribe(user => {
      this.userForm = this.fb.group({
        email: [user.email, [Validators.required, Validators.email]],
        username: [user.username, Validators.required],
        firstName: [user.firstName, Validators.required],
        lastName: [user.lastName, Validators.required],
        status: [user.status, Validators.required],
        role: [user.role, Validators.required],
        readOnly: [user.readOnly, Validators.required]
      });
      this.loading = false;
    });

  }

  onSave() {
    this.saving = true;
    // console.log(this.userForm.value);
    this.userService.update(this.userId, this.userForm.value).subscribe(
      (user) => {
        this.closeModal(user);
        this.saving = false;
      },
      (error) => {
        this.error = error;
        this.saving = false;
      });
  }

  closeModal(user: User) {
    this.activeModal.close(user);
  }

  dismiss() {
    this.activeModal.dismiss(false);
  }
}
