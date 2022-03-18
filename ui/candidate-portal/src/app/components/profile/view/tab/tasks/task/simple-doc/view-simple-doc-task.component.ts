import {Component, Input, OnInit} from '@angular/core';
import {FormGroup} from "@angular/forms";
import {DomSanitizer, SafeResourceUrl} from "@angular/platform-browser";

@Component({
  selector: 'app-view-simple-doc-task',
  templateUrl: './view-simple-doc-task.component.html',
  styleUrls: ['./view-simple-doc-task.component.scss']
})
export class ViewSimpleDocTaskComponent implements OnInit {
  @Input() form: FormGroup;
  @Input() docLink: string;
  url: SafeResourceUrl;

  constructor(public sanitizer: DomSanitizer) { }

  ngOnInit(): void {
    this.url = this.sanitizer.bypassSecurityTrustResourceUrl(this.docLink);
  }

}
