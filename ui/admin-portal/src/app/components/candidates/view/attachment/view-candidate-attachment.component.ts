import {Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {Candidate} from "../../../../model/candidate";
import {FormBuilder, FormGroup} from "@angular/forms";
import {AttachmentType, CandidateAttachment} from "../../../../model/candidate-attachment";
import {CandidateAttachmentService} from "../../../../services/candidate-attachment.service";
import {environment} from "../../../../../environments/environment";
import {CreateCandidateAttachmentComponent} from "./create/create-candidate-attachment.component";

@Component({
  selector: 'app-view-candidate-attachment',
  templateUrl: './view-candidate-attachment.component.html',
  styleUrls: ['./view-candidate-attachment.component.scss']
})
export class ViewCandidateAttachmentComponent implements OnInit, OnChanges {

  @Input() candidate: Candidate;
  @Input() editable: boolean;

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

  getAttachmentUrl(attachment: CandidateAttachment) {
    if (attachment.type === AttachmentType.file) {
      return this.s3BucketUrl + '/candidate/' + this.candidate.candidateNumber + '/' + attachment.location;
    }
    return attachment.location;
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

  addAttachment(type: string){
    const createCandidateAttachmentModal = this.modalService.open(CreateCandidateAttachmentComponent, {
      centered: true,
      backdrop: 'static'
    });

    createCandidateAttachmentModal.componentInstance.candidateId = this.candidate.id;
    createCandidateAttachmentModal.componentInstance.type = type || 'link';

    createCandidateAttachmentModal.result
      .then(() => this.doSearch())
      .catch(() => { /* Isn't possible */ });
  }

  deleteCandidateAttachment(attachment: CandidateAttachment) {
    alert("todo");
  }
}
