import {Component, OnInit} from '@angular/core';
import {CandidateService} from '../../../services/candidate.service';
import {Candidate} from '../../../model/candidate';
import {ActivatedRoute, Router} from '@angular/router';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {DeleteCandidateComponent} from './delete/delete-candidate.component';
import {EditCandidateStatusComponent} from "./status/edit-candidate-status.component";
import {Title} from "@angular/platform-browser";
import {AuthService} from "../../../services/auth.service";
import {User} from "../../../model/user";
import {IDropdownSettings} from "ng-multiselect-dropdown";
import {ListItem} from "ng-multiselect-dropdown/multiselect.model";
import {CreateListComponent} from "../../list/create/create-list.component";
import {
  IHasSetOfCandidates,
  SavedList,
  SearchSavedListRequest
} from "../../../model/saved-list";
import {SavedListService} from "../../../services/saved-list.service";
import {CandidateSavedListService} from "../../../services/candidate-saved-list.service";
import {SavedListCandidateService} from "../../../services/saved-list-candidate.service";
import {forkJoin} from "rxjs";
import {CandidateAttachmentService} from '../../../services/candidate-attachment.service';
import {AttachmentType, CandidateAttachment} from '../../../model/candidate-attachment';
import {FormBuilder, FormGroup} from '@angular/forms';
import {environment} from '../../../../environments/environment';

@Component({
  selector: 'app-view-candidate',
  templateUrl: './view-candidate.component.html',
  styleUrls: ['./view-candidate.component.scss']
})
export class ViewCandidateComponent implements OnInit {

  loading: boolean;
  loadingError: boolean;
  error;
  candidate: Candidate;
  mainColWidth = 8;
  sidePanelColWidth = 4;
  loggedInUser: User;

  selectedLists: SavedList[] = [];
  lists: SavedList[] = [];
  attachmentForm: FormGroup;
  attachments: CandidateAttachment[];
  cvs: CandidateAttachment[];
  s3BucketUrl = environment.s3BucketUrl;

  /* MULTI SELECT */
  dropdownSettings: IDropdownSettings = {
    idField: 'id',
    textField: 'name',
    enableCheckAll: true,
    singleSelection: false,
    allowSearchFilter: true
  };

  constructor(private candidateService: CandidateService,
              private savedListService: SavedListService,
              private candidateSavedListService: CandidateSavedListService,
              private savedListCandidateService: SavedListCandidateService,
              private candidateAttachmentService: CandidateAttachmentService,
              private route: ActivatedRoute,
              private router: Router,
              private modalService: NgbModal,
              private titleService: Title,
              private authService: AuthService,
              private fb: FormBuilder) { }

  ngOnInit() {
    this.loadingError = false;
    this.route.paramMap.subscribe(params => {
      const candidateNumber = params.get('candidateNumber');
      this.loading = true;
      this.error = null;
      this.loadingError = false;
      this.candidateService.getByNumber(candidateNumber).subscribe(candidate => {
        if (candidate == null) {
          this.loadingError = true;
          this.error = 'There is no candidate with number: ' + params.get('candidateNumber');
          this.loading = false;
        } else {
          this.setCandidate(candidate);
          this.loadLists();
          this.getAttachments();
        }
      }, error => {
        this.loadingError = true;
        this.error = error;
        this.loading = false;
      });
    });

    this.loggedInUser = this.authService.getLoggedInUser();



    console.log(this.loggedInUser);
  }

  private loadLists() {
    /*load all our non fixed lists */
    this.loading = true;
    const request: SearchSavedListRequest = {
      owned: true,
      shared: true,
      fixed: false
    };

    forkJoin( {
      'lists': this.savedListService.search(request),
      'selectedLists': this.candidateSavedListService.search(this.candidate.id, request)
    }).subscribe(
      results => {
        this.loading = false;
        this.lists = results['lists'];
        this.selectedLists = results['selectedLists'];
      },
      (error) => {
        this.error = error;
        this.loading = false;
      }
    );
  }

  getAttachments() {
    this.attachments = [];

    this.attachmentForm = this.fb.group({
      candidateId: [this.candidate.id],
      pageSize: 10,
      pageNumber: 0,
      sortDirection: 'DESC',
      sortFields: [['createdDate']]
    });

    this.loading = true;
    this.candidateAttachmentService.search(this.attachmentForm.value).subscribe(
      results => {
        this.attachments = results.content;
        this.cvs = results.content.filter(attachment => attachment.cv === true)
        this.loading = false;
      },
      error => {
        this.error = error;
        this.loading = false;
      })
    ;

  }

  getAttachmentUrl(att: CandidateAttachment) {
    if (att.type === AttachmentType.file) {
      return this.s3BucketUrl + '/candidate/' + (att.migrated ? 'migrated' : this.candidate.candidateNumber) + '/' + att.location;
    }
    return att.location;
  }

  openCVs() {
    for (let i = 0; i < this.cvs.length; i++) {
      const newTab = window.open();
      const url = this.getAttachmentUrl(this.cvs[i]);
      newTab.location.href = url;
    }
  }

  deleteCandidate() {
    const modal = this.modalService.open(DeleteCandidateComponent);
    modal.componentInstance.candidate = this.candidate;
    modal.result.then(result => {
      this.router.navigate(['/']);
    });
  }

  editCandidate() {
    const modal = this.modalService.open(EditCandidateStatusComponent);
    modal.componentInstance.candidateId = this.candidate.id;
    modal.result
      .then(result => {this.setCandidate(result); } )
      .catch(() => { /* Isn't possible */ });
  }

  resizeSidePanel() {
    this.mainColWidth = this.mainColWidth === 8 ? this.mainColWidth - 4 : this.mainColWidth + 4;
    this.sidePanelColWidth = this.mainColWidth === 4 ? this.sidePanelColWidth + 4 : this.sidePanelColWidth - 4;
  }


  setCandidate(value: Candidate) {
    this.candidate = value;
    if (this.candidate.user.firstName && this.candidate.user.lastName) {
      this.titleService.setTitle(this.candidate.user.firstName + ' '
        + this.candidate.user.lastName + ' ' + this.candidate.candidateNumber);
    } else {
      this.titleService.setTitle(this.candidate.candidateNumber);
    }
  }

  downloadCV() {
      const tab = window.open();
      this.candidateService.downloadCv(this.candidate.id).subscribe(
        result => {
          tab.location.href = URL.createObjectURL(result);
        },
        error => {
          this.error = error;
        }
      );
  }

  onItemSelect($event: ListItem) {
    const savedListId: number = +$event.id;
    this.addCandidateToList(savedListId, false);
  }

  onItemDeSelect($event: ListItem) {
    const savedListId: number = +$event.id;
    this.removeCandidateFromList(savedListId);
  }

  onSelectAll($event: Array<ListItem>) {
    this.setCandidateLists(this.lists);
  }

  onDeSelectAll($event: Array<ListItem>) {
    this.setCandidateLists(null);
  }

  onNewList() {
    const modal = this.modalService.open(CreateListComponent);
    modal.result
      .then((savedList: SavedList) => {
        this.addCandidateToList(savedList.id, true);
      })
      .catch(() => { /* Isn't possible */
      });
  }

  private addCandidateToList(savedListId: number, reload: boolean) {
    const request: IHasSetOfCandidates = {
      candidateIds: [this.candidate.id]
    };
    this.savedListCandidateService.merge(savedListId, request).subscribe(
          () => {
            if (reload) {
              this.loadLists();
            }
          },
          (error) => {
            this.error = error;
          }

    );
  }

  private setCandidateLists(lists: SavedList[]) {
    const ids: number[] = [];
    if (lists !== null && lists.length > 0) {
      for (const savedList of lists) {
        ids.push(savedList.id);
      }
    }
    this.candidateSavedListService.replace(this.candidate.id,
      {savedListIds: ids})
      .subscribe(
        () => {},
        (error) => {
          this.error = error;
        }
      );
  }

  private removeCandidateFromList(savedListId: number) {
    const request: IHasSetOfCandidates = {
      candidateIds: [this.candidate.id]
    };
    this.savedListCandidateService.remove(savedListId, request).subscribe(
          () => {},
          (error) => {
            this.error = error;
          }
    );
  }
}
