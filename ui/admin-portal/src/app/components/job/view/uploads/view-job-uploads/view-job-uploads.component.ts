import {Component, Input, OnInit} from '@angular/core';
import {Job, JobDocType} from "../../../../../model/job";
import {FileSelectorComponent} from "../../../../util/file-selector/file-selector.component";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {JobService} from "../../../../../services/job.service";

@Component({
  selector: 'app-view-job-uploads',
  templateUrl: './view-job-uploads.component.html',
  styleUrls: ['./view-job-uploads.component.scss']
})
export class ViewJobUploadsComponent implements OnInit {
  @Input() job: Job;
  @Input() editable: boolean;

  error: any;
  saving: boolean;

  constructor(
    private jobService: JobService,
    private modalService: NgbModal,
  ) { }

  ngOnInit(): void {
  }

  private editJobLink(docType: JobDocType) {
    //todo Create InputLinkComponent - capture link and name then save
    let link = "todo link";
    let name = "todo name";

    this.error = null;
    this.saving = true;
    this.jobService.updateJobLink(this.job.id, docType, name, link).subscribe(
      job => {
        //todo Need event to bubble up and change job
        this.job = job
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
        //todo Need event to bubble up and change job
        this.job = job
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

  editJOILink() {
    this.editJobLink("joi")
  }

  uploadJD() {
    this.uploadJobDoc("jd")
  }

  uploadJOI() {
    this.uploadJobDoc("joi")
  }
}
