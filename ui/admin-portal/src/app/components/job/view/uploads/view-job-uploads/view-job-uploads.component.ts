import {Component, Input, OnInit} from '@angular/core';
import {Job} from "../../../../../model/job";
import {FileSelectorComponent} from "../../../../util/file-selector/file-selector.component";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";

enum DocType {
  jd = "job description",
  joi = "job intake"
}

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
    private modalService: NgbModal,
  ) { }

  ngOnInit(): void {
  }

  get DocType() {
    return DocType;
  }

  editJobLink(docType: DocType) {
    //todo Create InputLinkComponent - capture link and name then save
    let link = "todo link";
    let name = "todo name";
    this.saveLink(docType, link, name);

  }

  private doUpload(docType: DocType, file: File) {
    const formData: FormData = new FormData();
    formData.append('file', file);

    //todo this.jobService.uploadJobDoc(this.job.id, docType, formData).subscribe;


    //todo upload file and if successful set the link and name
    let link = "todo link";
    let name = "todo name";
    this.saveLink(docType, link, name);
  }

  uploadJobDoc(docType: DocType) {
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

  private saveLink(docType: DocType, link: string, name: string) {

    //todo
  }
}
