import { Component, OnInit } from '@angular/core';
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";

@Component({
  selector: 'app-file-selector',
  templateUrl: './file-selector.component.html',
  styleUrls: ['./file-selector.component.scss']
})
export class FileSelectorComponent implements OnInit {

  title: string = "Select files"
  instructions: string;

  selectedFiles: File[] = [];

  constructor(private modal: NgbActiveModal) { }

  ngOnInit(): void {
  }

  addFiles(files: File[]) {
    this.selectedFiles.push(...files);
  }

  cancel() {
    this.modal.close();
  }

  close() {
    this.modal.close(this.selectedFiles);
  }

}
