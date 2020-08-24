import {
  Component,
  EventEmitter,
  Input,
  OnChanges,
  OnInit,
  Output,
  SimpleChanges
} from '@angular/core';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {Candidate} from "../../../../model/candidate";
import {FormBuilder, FormGroup} from "@angular/forms";
import {
  AttachmentType,
  CandidateAttachment
} from "../../../../model/candidate-attachment";
import {CandidateAttachmentService} from "../../../../services/candidate-attachment.service";
import {environment} from "../../../../../environments/environment";
import {CreateCandidateAttachmentComponent} from "./create/create-candidate-attachment.component";
import {ConfirmationComponent} from "../../../util/confirm/confirmation.component";
import {EditCandidateAttachmentComponent} from "./edit/edit-candidate-attachment.component";
import {User} from "../../../../model/user";

@Component({
  selector: 'app-view-candidate-attachment',
  templateUrl: './view-candidate-attachment.component.html',
  styleUrls: ['./view-candidate-attachment.component.scss']
})
export class ViewCandidateAttachmentComponent implements OnInit, OnChanges {

  @Input() candidate: Candidate;
  @Input() editable: boolean;
  @Input() loggedInUser: User;
  @Output() candidateChanged = new EventEmitter();

  loading: boolean;
  error: any;
  s3BucketUrl = environment.s3BucketUrl;

  attachmentForm: FormGroup;
  expanded: boolean;
  attachments: CandidateAttachment[];
  hasMore: boolean;

  constructor(private candidateAttachmentService: CandidateAttachmentService,
              private modalService: NgbModal,
              private fb: FormBuilder) {
  }

  ngOnInit() {
  }

  ngOnChanges(changes: SimpleChanges) {
    this.expanded = false;
    this.attachments = [];

    this.attachmentForm = this.fb.group({
      candidateId: [this.candidate.id],
      pageSize: 10,
      pageNumber: 0,
      sortDirection: 'DESC',
      sortFields: [['createdDate']]
    });

    if (changes && changes.candidate && changes.candidate.previousValue !== changes.candidate.currentValue) {
      this.doSearch();
    }

  }

  doSearch(refresh?: boolean) {
    this.loading = true;
    this.candidateAttachmentService.search(this.attachmentForm.value).subscribe(
      results => {
        if (refresh) {
          this.attachments = results.content;
        } else {
          this.attachments.push(...results.content);
        }

        this.hasMore = results.totalPages > results.number + 1;
        this.loading = false;
      },
      error => {
        this.error = error;
        this.loading = false;
      })
    ;

  }

  loadMore() {
    this.attachmentForm.controls['pageNumber'].patchValue(this.attachmentForm.value.pageNumber + 1);
    this.doSearch();
  }

  getAttachmentUrl(att: CandidateAttachment) {
    if (att.type === AttachmentType.file) {
      return this.s3BucketUrl + '/candidate/' + (att.migrated ? 'migrated' : this.candidate.candidateNumber) + '/' + att.location;
    }
    return att.location;
  }

  editCandidateAttachment(candidateAttachment: CandidateAttachment) {
      const editCandidateAttachmentModal = this.modalService.open(EditCandidateAttachmentComponent, {
      centered: true,
      backdrop: 'static'
    });

    editCandidateAttachmentModal.componentInstance.attachment = candidateAttachment;

    editCandidateAttachmentModal.result
      .then((updated) => {
        const index = this.attachments.findIndex(attachment => attachment.id === updated.id);
        if (index >= 0) {
          /* DEBUG */
          // console.log('index', index);
          this.attachments[index] = updated;
        } else {
          /* DEBUG */
          // console.log('updated', updated);
          this.doSearch(true); // Shouldn't be necessary, but is here as a fail-safe
        }
      })
      .catch(() => { /* Isn't possible */
      });
  }

  addAttachment(type: string){
    const createCandidateAttachmentModal = this.modalService.open(CreateCandidateAttachmentComponent, {
      centered: true,
      backdrop: 'static'
    });

    createCandidateAttachmentModal.componentInstance.candidateId = this.candidate.id;
    createCandidateAttachmentModal.componentInstance.type = type || 'link';

    createCandidateAttachmentModal.result
      .then(() => {
        this.doSearch(true);
        //Adding attachment should add a folder link if there was not one
        //there before. So emit a candidateChanged event.
        this.candidateChanged.emit();
      })
      .catch(() => { /* Isn't possible */ });
  }

  deleteCandidateAttachment(attachment: CandidateAttachment) {
    const deleteCountryModal = this.modalService.open(ConfirmationComponent, {
      centered: true,
      backdrop: 'static'
    });

    deleteCountryModal.componentInstance.message = 'Are you sure you want to delete ' + attachment.name + '?';

    deleteCountryModal.result
      .then((result) => {
        if (result === true) {
          this.candidateAttachmentService.deleteAttachment(attachment.id).subscribe(
            () => {
              this.doSearch(true);
            },
            (error) => {
              console.log('error', error);
            });
        }
      })
      .catch(() => { /* Isn't possible */ });
  }
}
