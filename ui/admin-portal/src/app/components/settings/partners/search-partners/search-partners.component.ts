import {Component, OnInit} from '@angular/core';
import {AuthorizationService} from "../../../../services/authorization.service";
import {FormBuilder, FormGroup} from "@angular/forms";
import {debounceTime, distinctUntilChanged} from "rxjs/operators";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {SearchResults} from "../../../../model/search-results";
import {Partner, sourceCountriesAsString} from "../../../../model/partner";
import {PartnerService} from "../../../../services/partner.service";
import {SearchPartnerRequest} from "../../../../model/base";
import {
  CreateUpdatePartnerComponent
} from "../create-update-partner/create-update-partner.component";
import {User} from "../../../../model/user";

/*
   MODEL - Delegate all authentication logic to authService
 */

@Component({
  selector: 'app-search-partners',
  templateUrl: './search-partners.component.html',
  styleUrls: ['./search-partners.component.scss']
})
export class SearchPartnersComponent implements OnInit {
  error: any;
  loading: boolean;
  pageNumber: number;
  pageSize: number;
  readOnly: boolean;
  results: SearchResults<Partner>;
  searchForm: FormGroup;

  constructor(
    private partnerService: PartnerService,
    private fb: FormBuilder,
    private modalService: NgbModal,
    private authService: AuthorizationService) { }

  ngOnInit(): void {
    this.searchForm = this.fb.group({
      keyword: [''],
      status: ['active'],
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
    const request: SearchPartnerRequest =  {
      keyword: this.searchForm.value.keyword,
      status: this.searchForm.value.status,
      pageNumber: this.pageNumber - 1,
      pageSize: this.pageSize,
      sortFields: ['id'],
      sortDirection: 'ASC',
    };

    this.partnerService.searchPaged(request).subscribe(
      results => {
      this.results = results;
      this.loading = false;
    },
      error => {
        this.error = error;
        this.loading = false;
      });
  }

  addPartner() {
    const addPartnerModal = this.modalService.open(CreateUpdatePartnerComponent, {
      centered: true,
      backdrop: 'static'
    });

    addPartnerModal.result
    .then((result) => this.search())
    .catch(() => {});
  }

  editPartner(partner: Partner) {
    const editPartnerModal = this.modalService.open(CreateUpdatePartnerComponent, {
      centered: true,
      backdrop: 'static'
    });

    editPartnerModal.componentInstance.partner = partner;

    editPartnerModal.result
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
