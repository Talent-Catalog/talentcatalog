import { Component, OnInit } from '@angular/core';

import { SearchResults } from '../../../model/search-results';

import {FormBuilder, FormGroup} from "@angular/forms";
import {debounceTime, distinctUntilChanged} from "rxjs/operators";
import {User} from "../../../model/user";
import {UserService} from "../../../services/user.service";

@Component({
  selector: 'app-search-users',
  templateUrl: './search-users.component.html',
  styleUrls: ['./search-users.component.scss']
})
export class SearchUsersComponent implements OnInit {

  searchForm: FormGroup;
  loading: boolean;
  error: any;
  pageNumber: number;
  pageSize: number;
  results: SearchResults<User>;




  constructor(private fb: FormBuilder,
              private userService: UserService) { }

  ngOnInit() {


  /* SET UP FORM */
    this.searchForm = this.fb.group({
      keyword: [''],
      role: ['admin'],
    });
    this.pageNumber = 1;
    this.pageSize = 50;

  /* SEARCH ON CHANGE*/
    this.searchForm.get('keyword').valueChanges
      .pipe(
        debounceTime(400),
        distinctUntilChanged()
      )
      .subscribe(res => {
        this.search();
      });
    this.search();


  }


/* SEARCH FORM */
  search() {
    this.loading = true;
    let request = this.searchForm.value;
    request.pageNumber = this.pageNumber - 1;
    request.pageSize =  this.pageSize;
    this.userService.search(request).subscribe(results => {
      this.results = results;
      this.loading = false;
    });
  }
}
