import {Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges} from '@angular/core';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {Candidate} from "../../../../model/candidate";
import {FormBuilder, FormGroup} from "@angular/forms";
import {CandidateAttachment} from "../../../../model/candidate-attachment";
import {CandidateAttachmentService} from "../../../../services/candidate-attachment.service";

@Component({
  selector: 'app-view-candidate-attachment',
  templateUrl: './view-candidate-attachment.component.html',
  styleUrls: ['./view-candidate-attachment.component.scss']
})
export class ViewCandidateAttachmentComponent implements OnInit, OnChanges {

  @Input() candidate: Candidate;
  @Input() editable: boolean;

  attachmentForm: FormGroup;
  loading: boolean;
  expanded: boolean;
  error;
  attachments: CandidateAttachment[];
  hasMore: boolean;

  constructor(private candidateAttachmentService: CandidateAttachmentService,
              private modalService: NgbModal,
              private fb: FormBuilder) {
  }

  ngOnInit() {
  }

  ngOnChanges(changes: SimpleChanges) {
    this.editable = true;
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

  doSearch() {
    this.loading = true;
    this.candidateAttachmentService.search(this.attachmentForm.value).subscribe(
      results => {
        this.attachments.push(...results.content);
        this.hasMore = results.totalPages > results.number+1;
        this.loading = false;
      },
      error => {
        this.error = error;
        this.loading = false;
      })
    ;

  }

  loadMore() {
    this.attachmentForm.controls['pageNumber'].patchValue(this.attachmentForm.value.pageNumber+1);
    this.doSearch();
  }

  todo(){
    alert('Need to find where these files are stored');
  }

  editCandidateAttachment(candidateAttachment: CandidateAttachment) {
    alert('todo');

    //   const editCandidateAttachmentModal = this.modalService.open(EditCandidateAttachmentComponent, {
  //     centered: true,
  //     backdrop: 'static'
  //   });
  //
  //   editCandidateAttachmentModal.componentInstance.candidateAttachment = candidateAttachment;
  //
  //   editCandidateAttachmentModal.result
  //     .then((candidateAttachment) => this.doSearch())
  //     .catch(() => { /* Isn't possible */
  //     });
  //
  }
  //
  createCandidateAttachment(){

    alert('todo');
  //   const createCandidateAttachmentModal = this.modalService.open(CreateCandidateAttachmentComponent, {
  //     centered: true,
  //     backdrop: 'static'
  //   });
  //
  //   createCandidateAttachmentModal.componentInstance.candidateId = this.candidate.id;
  //
  //   createCandidateAttachmentModal.result
  //     .then((candidateAttachment) => this.doSearch())
  //     .catch(() => { /* Isn't possible */
  //     });
  //
  }

}
