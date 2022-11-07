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
    //todo
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
      this.doUpload(docType, selectedFiles);
    })
    .catch(() => {});
  }

  private doUpload(docType: DocType, selectedFiles: File[]) {
    //todo upload file and if successful set the link and name
  }
}
