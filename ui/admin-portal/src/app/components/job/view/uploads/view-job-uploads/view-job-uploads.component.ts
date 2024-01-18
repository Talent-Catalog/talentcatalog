import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {Job, JobDocType} from "../../../../../model/job";
import {FileSelectorComponent} from "../../../../util/file-selector/file-selector.component";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {JobService} from "../../../../../services/job.service";
import {
  InputLinkComponent,
  UpdateLinkRequest
} from "../../../../util/input/input-link/input-link.component";
import {JobPrepItem, JobPrepJD, JobPrepJOI} from "../../../../../model/job-prep-item";

/*
MODEL: Bubbling up object changes using @Output events (in this case changes to a job)
through a hierarchy of components.
 */

@Component({
  selector: 'app-view-job-uploads',
  templateUrl: './view-job-uploads.component.html',
  styleUrls: ['./view-job-uploads.component.scss']
})
export class ViewJobUploadsComponent implements OnInit {
  @Input() job: Job;
  @Input() editable: boolean;
  @Input() highlightItem: JobPrepItem;
  @Output() jobUpdated = new EventEmitter<Job>();

  error: any;
  saving: boolean;

  constructor(
    private jobService: JobService,
    private modalService: NgbModal,
  ) { }

  ngOnInit(): void {
  }

  //todo No longer have JOI link in here


  private editJobLink(docType: JobDocType) {
    const inputLinkModal = this.modalService.open(InputLinkComponent, {
      centered: true,
      backdrop: 'static'
    })
    inputLinkModal.componentInstance.title = "Add/edit link to document"

    let initialValue: UpdateLinkRequest = {}
    switch (docType) {
      case "jd":
        initialValue.url = this.job.submissionList?.fileJdLink;
        initialValue.name  = this.job.submissionList?.fileJdName;
        break;

      case "joi":
        initialValue.url = this.job.submissionList?.fileJoiLink;
        initialValue.name = this.job.submissionList?.fileJoiName;
        break;

      case "interview":
        initialValue.url = this.job.submissionList?.fileInterviewGuidanceLink;
        initialValue.name = this.job.submissionList?.fileInterviewGuidanceName;
        break;
    }
    inputLinkModal.componentInstance.initialValue = initialValue;
    inputLinkModal.result.then(
      updateLinkRequest => {this.doUpdateLink(docType, updateLinkRequest)}
    );
  }

  private doUpdateLink(docType: JobDocType, updateLinkRequest: UpdateLinkRequest) {
    this.error = null;
    this.saving = true;
    this.jobService.updateJobLink(this.job.id, docType, updateLinkRequest).subscribe(
      job => {
        //Need event to bubble up and change job
        this.jobUpdated.emit(job)
        this.saving = false;
      },
      (error) => {
        this.error = error
        this.saving = false;
      }
    );
  }

  private doUpload(docType: JobDocType, file: File) {
    const formData: FormData = new FormData();
    formData.append('file', file);

    this.error = null;
    this.saving = true;
    this.jobService.uploadJobDoc(this.job.id, docType, formData).subscribe(
      job => {
        //Need event to bubble up and change job
        this.jobUpdated.emit(job)
        this.saving = false;
      },
      (error) => {
        this.error = error
        this.saving = false;
      }
    );
  }

  private uploadJobDoc(docType: JobDocType) {
    const fileSelectorModal = this.modalService.open(FileSelectorComponent, {
      centered: true,
      backdrop: 'static'
    })

    fileSelectorModal.componentInstance.maxFiles = 1;
    fileSelectorModal.componentInstance.closeButtonLabel = "Upload";
    fileSelectorModal.componentInstance.title = "Select file containing the " + docType;

    fileSelectorModal.result
    .then((selectedFiles: File[]) => {
      if (selectedFiles.length > 0) {
        this.doUpload(docType, selectedFiles[0]);
      }
    })
    .catch(() => {});
  }

  editJDLink() {
    this.editJobLink("jd")
  }

  editInterviewGuidanceLink() {
    this.editJobLink("interview")
  }

  editJOILink() {
    this.editJobLink("joi")
  }

  highlightJD() {
    return this.highlightItem instanceof JobPrepJD;
  }

  highlightJOI() {
    return this.highlightItem instanceof JobPrepJOI;
  }

  uploadJD() {
    this.uploadJobDoc("jd")
  }

  uploadJOI() {
    this.uploadJobDoc("joi")
  }

  uploadInterviewGuidance() {
    this.uploadJobDoc("interview")
  }
}
