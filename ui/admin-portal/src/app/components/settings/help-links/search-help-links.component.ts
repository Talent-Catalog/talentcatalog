import {Component, OnInit} from '@angular/core';
import {SearchResults} from "../../../model/search-results";
import {Partner, sourceCountriesAsString} from "../../../model/partner";
import {FormBuilder, FormGroup} from "@angular/forms";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {AuthorizationService} from "../../../services/authorization.service";
import {debounceTime, distinctUntilChanged} from "rxjs/operators";
import {
  CreateUpdatePartnerComponent
} from "../partners/create-update-partner/create-update-partner.component";
import {User} from "../../../model/user";
import {HelpLink, SearchHelpLinkRequest} from "../../../model/help-link";
import {HelpLinkService} from "../../../services/help-link.service";
import {Country} from "../../../model/country";
import {CountryService} from "../../../services/country.service";

@Component({
  selector: 'app-help-links',
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

  constructor(
    private helpLinkService: HelpLinkService,
    private countryService: CountryService,
    private fb: FormBuilder,
    private modalService: NgbModal,
    private authService: AuthorizationService) { }

  ngOnInit(): void {
    this.countryService.listTBBDestinations().subscribe((results) => {
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
    const addHelpLinkModal = this.modalService.open(CreateUpdatePartnerComponent, {
      centered: true,
      backdrop: 'static'
    });

    addHelpLinkModal.result
    .then((result) => this.search())
    .catch(() => {});
  }

  editHelpLink(helpLink: HelpLink) {
    const editHelpLinkModal = this.modalService.open(CreateUpdatePartnerComponent, {
      centered: true,
      backdrop: 'static'
    });

    editHelpLinkModal.componentInstance.helpLink = helpLink;

    editHelpLinkModal.result
    .then((result) => this.search())
    .catch(() => {});
  }

  sourceCountries(partner: Partner) {
    return sourceCountriesAsString(partner);
  }

  showContact(user: User): string {

    return user ? user.firstName + " " + user.lastName + "(" + user.email + ")" : "";
  }

  canAccessSalesforce(): boolean {
    return this.authService.canAccessSalesforce();
  }

}
