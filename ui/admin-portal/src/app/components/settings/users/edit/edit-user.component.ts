import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {User} from "../../../../model/user";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {UserService} from "../../../../services/user.service";
import {AuthService} from "../../../../services/auth.service";
import {CountryService} from "../../../../services/country.service";
import {Country} from "../../../../model/country";
import {IDropdownSettings} from "ng-multiselect-dropdown";

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

  /* MULTI SELECT */
  dropdownSettings: IDropdownSettings = {
    idField: 'id',
    textField: 'name',
    enableCheckAll: false,
    singleSelection: false,
    allowSearchFilter: false
  };

  countries: Country[];

  constructor(private activeModal: NgbActiveModal,
              private fb: FormBuilder,
              private userService: UserService,
              private authService: AuthService,
              private countryService: CountryService) {
  }

  ngOnInit() {
    this.loading = true;
    this.userService.get(this.userId).subscribe(user => {
      console.log(user);
      this.userForm = this.fb.group({
        email: [user.email, [Validators.required, Validators.email]],
        username: [user.username, Validators.required],
        firstName: [user.firstName, Validators.required],
        lastName: [user.lastName, Validators.required],
        status: [user.status, Validators.required],
        role: [user.role, Validators.required],
        sourceCountries: [user.sourceCountries],
        readOnly: [user.readOnly]
      });
      this.loading = false;
    });

    this.countryService.listCountries().subscribe(
      (response) => {
        this.countries = response;
      },
      (error) => {
        this.error = error;
        this.loading = false;
      }
    );

  }

  onSave() {
    this.saving = true;
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

  /* MULTI SELECT METHODS */
  onItemSelect(item: any) {
    console.log(this.userForm);
    // const values = this.userForm.controls.sourceCountryIds.value || [];
    // const addValue = item.id != null ? item.id : item;
    // values.push(addValue);
    // this.userForm.controls.sourceCountryIds.patchValue(values);
    // console.log(values)
  }

  onItemDeSelect(item: any) {
    // const values = this.userForm.controls[formControlName].value || [];
    // const removeValue = item.id != null ? item.id : item;
    // const indexToRemove = values.findIndex(val => val === removeValue);
    // if (indexToRemove >= 0) {
    //   values.splice(indexToRemove, 1);
    //   this.userForm.controls[formControlName].patchValue(values);
    // }
  }

}
