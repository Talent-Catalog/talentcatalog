import {Component, Input, OnInit} from '@angular/core';
import {User} from "../../../model/user";
import {FormBuilder, FormGroup} from "@angular/forms";
import {SearchResults} from "../../../model/search-results";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {AuthorizationService} from "../../../services/authorization.service";
import {debounceTime, distinctUntilChanged} from "rxjs/operators";
import {ConfirmationComponent} from "../../util/confirm/confirmation.component";
import {SavedListService} from "../../../services/saved-list.service";
import {CreateExternalLinkComponent} from "./create/create-external-link.component";
import {EditExternalLinkComponent} from "./edit/edit-external-link.component";
import {
  externalDocLink,
  SavedList,
  SearchSavedListRequest,
  UpdateShortNameRequest
} from "../../../model/saved-list";
import {copyToClipboard} from "../../../util/clipboard";

@Component({
  selector: 'app-search-external-links',
  templateUrl: './search-external-links.component.html',
  styleUrls: ['./search-external-links.component.scss']
})
export class SearchExternalLinksComponent implements OnInit {

  @Input() loggedInUser: User;

  searchForm: FormGroup;
  loading: boolean;
  error: any;
  pageNumber: number;
  pageSize: number;
  results: SearchResults<SavedList>;


  constructor(private fb: FormBuilder,
              private savedListService: SavedListService,
              private modalService: NgbModal,
              private authService: AuthorizationService) {
  }

  ngOnInit() {

    /* SET UP FORM */
    this.searchForm = this.fb.group({
      keyword: [''],
      status: ['active'],
    });
    this.pageNumber = 1;
    this.pageSize = 50;

    this.onChanges();
  }

  onChanges(): void {
    /* SEARCH ON CHANGE*/
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

  /* SEARCH FORM */
  search() {
    this.loading = true;
    const request: SearchSavedListRequest = this.searchForm.value;
    request.pageNumber = this.pageNumber - 1;
    request.pageSize = this.pageSize;
    request.shortName = true;
    this.savedListService.searchPaged(request).subscribe(results => {
      this.results = results;
      this.loading = false;
    });
  }

  addLink() {
    const addLinkModal = this.modalService.open(CreateExternalLinkComponent, {
      centered: true,
      backdrop: 'static'
    });

    addLinkModal.result
      .then((result) => this.search())
      .catch(() => { /* Isn't possible */ });
  }

  editLink(link: SavedList) {
    const editLinkModal = this.modalService.open(EditExternalLinkComponent, {
      centered: true,
      backdrop: 'static'
    });

    editLinkModal.componentInstance.savedList = link;

    editLinkModal.result
      .then((result) => this.search())
      .catch(() => { /* Isn't possible */ });
  }

  deleteLink(savedList: SavedList) {
    const deleteCountryModal = this.modalService.open(ConfirmationComponent, {
      centered: true,
      backdrop: 'static'
    });

    deleteCountryModal.componentInstance.message = 'Are you sure you want to delete the external link for ' + savedList.name;
    const request: UpdateShortNameRequest = {
      savedListId: savedList.id,
      tbbShortName: null,
    }
    deleteCountryModal.result
      .then((result) => {
        if (result === true) {
          this.savedListService.updateShortName(request).subscribe(
            (country) => {
              this.loading = false;
              this.search();
            },
            (error) => {
              this.error = error;
              this.loading = false;
            });
          this.search()
        }
      })
      .catch(() => { /* Isn't possible */ });

  }

  isAnAdmin(): boolean {
    return this.authService.isAnAdmin();
  }

  externalDocLink(savedList: SavedList) {
    return externalDocLink(savedList);
  }

  doCopyLink(savedList: SavedList) {
    const text = externalDocLink(savedList);
    copyToClipboard(text);
    const showReport = this.modalService.open(ConfirmationComponent, {
      centered: true, backdrop: 'static'});
    showReport.componentInstance.title = "Copied link to clipboard";
    showReport.componentInstance.showCancel = false;
    showReport.componentInstance.message = "Paste the link (" + text + ") where you want";

  }
}
