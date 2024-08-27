import {Component, OnInit} from '@angular/core';
import {SearchResults} from "../../../model/search-results";
import {FormBuilder, FormGroup} from "@angular/forms";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {AuthorizationService} from "../../../services/authorization.service";
import {debounceTime, distinctUntilChanged} from "rxjs/operators";
import {HelpFocus, HelpLink, SearchHelpLinkRequest} from "../../../model/help-link";
import {HelpLinkService} from "../../../services/help-link.service";
import {Country} from "../../../model/country";
import {CountryService} from "../../../services/country.service";
import {
  CreateUpdateHelpLinkComponent
} from "./create-update-help-link/create-update-help-link.component";
import {CandidateOpportunityStage} from "../../../model/candidate-opportunity";
import {JobOpportunityStage} from "../../../model/job";

@Component({
  selector: 'app-search-help-links',
  templateUrl: './search-help-links.component.html',
  styleUrls: ['./search-help-links.component.scss']
})
export class SearchHelpLinksComponent implements OnInit {
  error: any;
  loading: boolean;
  pageNumber: number;
  pageSize: number;
  readOnly: boolean;
  results: SearchResults<HelpLink>;
  searchForm: FormGroup;
  destinationCountries: Country[];

  labelHelpRequest: SearchHelpLinkRequest = {jobStage: 'recruitmentProcess'};

  constructor(
    private helpLinkService: HelpLinkService,
    private countryService: CountryService,
    private fb: FormBuilder,
    private modalService: NgbModal,
    private authService: AuthorizationService) { }

  ngOnInit(): void {
    this.countryService.listTCDestinations().subscribe((results) => {
      this.destinationCountries = results;
    })

    this.searchForm = this.fb.group({
      keyword: [''],
      countryId: [null],
    });
    this.pageNumber = 1;
    this.pageSize = 50;

    this.readOnly = this.authService.isReadOnly();

    //Monitor form changes
    this.searchForm.valueChanges
    .pipe(
      debounceTime(400),
      distinctUntilChanged()
    )
    .subscribe(res => {
      this.search();
    });
    this.search();

  }

  /**
   * Search based on current search form contents
   */
  search() {
    this.error = null;
    this.loading = true;
    const request: SearchHelpLinkRequest =  {
      keyword: this.searchForm.value.keyword,
      countryId: this.searchForm.value.countryId,
      pageNumber: this.pageNumber - 1,
      pageSize: this.pageSize,
      sortFields: ['id'],
      sortDirection: 'ASC',
    };

    this.helpLinkService.searchPaged(request).subscribe(
      results => {
        this.results = results;
        this.loading = false;
      },
      error => {
        this.error = error;
        this.loading = false;
      });
  }

  addHelpLink() {
    const addHelpLinkModal = this.modalService.open(CreateUpdateHelpLinkComponent, {
      centered: true,
      backdrop: 'static'
    });

    addHelpLinkModal.componentInstance.destinationCountries = this.destinationCountries;

    addHelpLinkModal.result
    .then((result) => this.search())
    .catch(() => {});
  }

  editHelpLink(helpLink: HelpLink) {
    const editHelpLinkModal = this.modalService.open(CreateUpdateHelpLinkComponent, {
      centered: true,
      backdrop: 'static'
    });

    editHelpLinkModal.componentInstance.helpLink = helpLink;
    editHelpLinkModal.componentInstance.destinationCountries = this.destinationCountries;

    editHelpLinkModal.result
    .then((result) => this.search())
    .catch(() => {});
  }

  caseStageKeyToValue(key: string) {
    return CandidateOpportunityStage[key];
  }

  jobStageKeyToValue(key: string) {
    return JobOpportunityStage[key];
  }

  focusKeyToValue(key: string) {
    return HelpFocus[key];
  }
}
