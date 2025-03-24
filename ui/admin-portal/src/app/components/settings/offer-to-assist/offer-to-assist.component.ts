import {Component, OnInit} from '@angular/core';
import {UntypedFormBuilder, UntypedFormGroup} from "@angular/forms";
import {CandidateAssistanceType, OfferToAssist} from "../../../model/offer-to-assist";
import {OfferToAssistService} from "../../../services/offer-to-assist.service";
import {SearchResults} from "../../../model/search-results";
import {KeywordPagedSearchRequest} from "../../../model/base";
import {debounceTime, distinctUntilChanged} from "rxjs/operators";

@Component({
  selector: 'app-offer-to-assist',
  templateUrl: './offer-to-assist.component.html',
  styleUrls: ['./offer-to-assist.component.scss']
})
export class OfferToAssistComponent implements OnInit {
  searchForm: UntypedFormGroup;
  loading: boolean;
  error: any;
  pageNumber: number;
  pageSize: number;
  results: SearchResults<OfferToAssist>;

  constructor(private fb: UntypedFormBuilder,
              private offerToAssistService: OfferToAssistService) {
  }

  ngOnInit() {
    /* SET UP FORM */
    this.searchForm = this.fb.group({
      keyword: [''],
    });
    this.pageNumber = 1;
    this.pageSize = 50;

    /* GET RESULTS */
    this.search();

    /* SET UP SEARCH ON CHANGE*/
    this.searchForm.valueChanges
      .pipe(
        debounceTime(400),
        distinctUntilChanged()
      )
      .subscribe(res => {
        this.search();
      });
  }

  search() {
    this.loading = true;
    const request: KeywordPagedSearchRequest =  {
      keyword: this.searchForm.value.keyword,
      pageNumber: this.pageNumber - 1,
      pageSize: this.pageSize
    };
    this.offerToAssistService.search(request).subscribe(
      (results) => {
        this.results = results;
        this.loading = false;
      }, (error) => {
        this.error = error;
        this.loading = false;
      }
    )
  }

  getDisplayEnumValue(value: CandidateAssistanceType) {
    return CandidateAssistanceType[value]
  }
}
