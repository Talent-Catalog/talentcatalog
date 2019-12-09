import {Component, OnInit} from '@angular/core';
import {Router} from "@angular/router";
import {AuthService} from "../../services/auth.service";
import {CandidateService} from "../../services/candidate.service";
import {Candidate} from "../../model/candidate";
import {Observable, of} from "rxjs";
import {catchError, debounceTime, distinctUntilChanged, map, switchMap, tap} from "rxjs/operators";

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss']
})
export class HeaderComponent implements OnInit {

  isNavbarCollapsed=true;
  candidates: Candidate[];
  doSearch;
  searchFailed;
  searching;
  error;


  constructor(private authService: AuthService,
              private candidateService: CandidateService,
              private router: Router) { }

  ngOnInit() {
    //dropdown to add joined searches
    this.doSearch = (text$: Observable<string>) =>
      text$.pipe(
        debounceTime(300),
        distinctUntilChanged(),
        tap(() => {
          this.searching = true;
          this.error = null
        }),
        switchMap(candidateNumber =>
          this.candidateService.find({candidateNumber: candidateNumber, pageSize: 10}).pipe(
            tap(() => this.searchFailed = false),
            map(result => result.content),
            catchError(() => {
              this.searchFailed = true;
              return of([]);
            }))
        ),
        tap(() => this.searching = false)
      );

  }

  renderCandidateRow(candidate: Candidate) {
    return candidate.candidateNumber+": "+candidate.user.firstName + " "+candidate.user.lastName;
  }


  logout() {
    this.authService.logout();
    this.router.navigate(['login']);
  }






  selectSearchResult (selectedItem) {
    console.log('going to', selectedItem.item);
    this.router.navigate(['candidates',  selectedItem.item.id]);

  };
}
