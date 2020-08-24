import {
  Component,
  EventEmitter,
  HostListener,
  OnInit,
  Output
} from '@angular/core';

@Component({
  selector: 'app-file-upload',
  templateUrl: './file-upload.component.html',
  styleUrls: ['./file-upload.component.scss']
})
export class FileUploadComponent implements OnInit {

  @HostListener('dragover', ['$event'])
  public onDragOver(evt) {
    evt.preventDefault();
    evt.stopPropagation();
    this.hover = true;
  }

  @HostListener('dragleave', ['$event'])
  public onDragLeave(evt) {
    evt.preventDefault();
    evt.stopPropagation();
    this.hover = false;
  }

  @HostListener('drop', ['$event'])
  public onDrop(evt) {
    evt.preventDefault();
    evt.stopPropagation();
    this.hover = false;
    const fileChangeEvent = {target: {files: evt.dataTransfer.files}};
    this.handleFileChanged(fileChangeEvent);
  }

  @Output() uploadStarted = new EventEmitter();

  error: any;
  hover: boolean;

  ngOnInit() {
  }

  handleFileChanged(event: any) {
    const files: File[] = [...event.target.files];

    if (!!files && files.length) {

      this.error = null;

      for (const file of files) {
        if (!this.validFile(file)) {
          return;
        }
      }
      this.uploadStarted.emit(files);
    }
  }

  validFile(file) {
    if (file.name.indexOf('.') === -1) {
      this.error = 'No file extension found. Please rename and re-upload this file.';
      return false;
    }

    const tokens = file.name.split('.');
    const validExtensions = [
      'jpg',
      'png',
      'pdf',
      'doc',
      'docx',
      'txt',
    ];
    const ext = tokens[tokens.length - 1].toLowerCase();
    if (!validExtensions.includes(ext)) {
      this.error = 'Unsupported file extension. Please upload a file with one of the following extensions: ' + validExtensions.join(', ');
      return false;
    }

    return true;
  }
}
