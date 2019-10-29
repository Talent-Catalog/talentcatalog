import {Component, Input, OnInit} from '@angular/core';
import {CandidateAttachmentService} from "../../../services/candidate-attachment.service";
import {CandidateAttachment} from "../../../model/candidate-attachment";
import {FormBuilder, FormGroup} from "@angular/forms";
import {SearchResults} from "../../../model/search-results";

@Component({
  selector: 'app-candidate-attachments',
  templateUrl: './candidate-attachments.component.html',
  styleUrls: ['./candidate-attachments.component.scss']
})
export class CandidateAttachmentsComponent implements OnInit {

  @Input() preview: boolean = false;

  error: any;
  loading: boolean = true;

  form: FormGroup;
  result: SearchResults<CandidateAttachment>;
  attachments: CandidateAttachment[] = [];

  constructor(private fb: FormBuilder,
              private candidateAttachmentService: CandidateAttachmentService) { }

  get pageControl() {
    return this.form.controls.pageNumber;
  }

  ngOnInit() {
    // Set up the search form
    this.form = this.fb.group({
      pageSize: 10,
      pageNumber: 1
    });

    // Listen for value changes to the page number
    this.pageControl.valueChanges.subscribe(() => this.search());

    this.search();
  }

  search() {
    /* DEBUG */
    console.log('this.pc.v', this.pageControl.value);

    this.candidateAttachmentService.searchCandidateAttachments(this.form.value).subscribe(
      (response) => {
        this.result = response;
        this.attachments = response.content;
        this.loading = false;
      },
      (error) => {
        this.error = error;
        this.loading = false;
      });
  }

  handleAttachmentUploaded(attachment: CandidateAttachment) {
    this.attachments.push(attachment);
  }

  setPage(page) {
    this.pageControl.patchValue(page);
  }

  getPrevPage() {
    if (!this.result || this.result.first) {
      return;
    }
    this.setPage(this.pageControl.value - 1);
  }

  getNextPage() {
    if (!this.result || this.result.last) {
      return;
    }
    this.setPage(this.pageControl.value + 1);
  }

}
