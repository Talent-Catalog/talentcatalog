import {Component, OnInit} from '@angular/core';
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";

@Component({
  selector: 'app-file-selector',
  templateUrl: './file-selector.component.html',
  styleUrls: ['./file-selector.component.scss']
})
export class FileSelectorComponent implements OnInit {

  closeButtonLabel: string = "Close";
  error: string;
  instructions: string;
  maxFiles: number = 0;
  title: string = "Select files"
  validExtensions: string[] = [
    'jpg',
    'png',
    'pdf',
    'doc',
    'docx',
    'txt',
  ];

  selectedFiles: File[] = [];

  constructor(private modal: NgbActiveModal) { }

  ngOnInit(): void {
  }

  addFiles($event) {
    this.error = null;
    if (this.maxFiles > 0 && $event.files.length > this.maxFiles) {
      this.error = "Only " + this.maxFiles + " file(s) can be selected."
    } else {
      this.selectedFiles = $event.files;
    }
  }

  cancel() {
    this.modal.close();
  }

  close() {
    this.modal.close(this.selectedFiles);
  }

  isValid() {
    return this.selectedFiles.length === 1;
  }

  onError(error: string) {
    this.error = error;
  }
}
