import {Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges} from '@angular/core';
import {Candidate, UpdateCandidateShareableDocsRequest} from "../../../../model/candidate";
import {FormBuilder, FormGroup} from "@angular/forms";
import {CandidateAttachment} from "../../../../model/candidate-attachment";
import {CandidateService} from "../../../../services/candidate.service";
import {isSavedList} from "../../../../model/saved-list";
import {CandidateSource} from "../../../../model/base";

@Component({
  selector: 'app-shareable-docs',
  templateUrl: './shareable-docs.component.html',
  styleUrls: ['./shareable-docs.component.scss']
})
export class ShareableDocsComponent implements OnInit, OnChanges {

  @Input() candidate: Candidate;
  @Output() candidateChange = new EventEmitter<Candidate>();

  @Input() candidateSource: CandidateSource;

  @Output() updatedShareableCV = new EventEmitter<CandidateAttachment>();

  cvs: CandidateAttachment[];
  other: CandidateAttachment[];

  error: boolean;
  loading: boolean;
  saving: boolean;

  savedList: boolean;

  form: FormGroup;

  constructor(private fb: FormBuilder,
              private candidateService: CandidateService) {}

  ngOnInit() {

    // Initialise the form
    if (this.isList) {
      this.form = this.fb.group({
        shareableCvAttachmentId: [this.candidate?.listShareableCv?.id],
        shareableDocAttachmentId: [this.candidate?.listShareableDoc?.id],
      });
    } else {
      this.form = this.fb.group({
        shareableCvAttachmentId: [this.candidate?.shareableCv?.id],
        shareableDocAttachmentId: [this.candidate?.shareableDoc?.id],
      });
    }

    this.form.valueChanges.subscribe((formValue) => {
      this.doSave(formValue);
    })
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.loadDropdowns();

    //Replace form value with the new candidates shareable docs when changing from one candidate to the next in a list.
    if (this.form && this.isList) {
      this.form.controls['shareableCvAttachmentId'].patchValue(this.candidate?.listShareableCv?.id, {emitEvent: false});
      this.form.controls['shareableDocAttachmentId'].patchValue(this.candidate?.listShareableDoc?.id, {emitEvent: false});
    }
  }

  loadDropdowns() {
    //Need to separate cvs & other for the ng select form dropdowns.
    this.cvs = this.filterByCv(true);
    this.other = this.filterByCv(false);
  }

  doSave(formValue: any) {
    this.saving = true;
    const request: UpdateCandidateShareableDocsRequest = {
      shareableCvAttachmentId: formValue.shareableCvAttachmentId,
      shareableDocAttachmentId: formValue.shareableDocAttachmentId
    }
    if (this.isList) {
      request.savedListId = this.candidateSource.id;
    }
    this.candidateService.updateShareableDocs(this.candidate.id, request).subscribe(
      (candidate) => {
        // // todo I want to keep the list specific fields for this candidate that come from the results of candidates for my list. I can't
        // // just replace that candidate with one I fetch that isn't list specific, as that won't have the list specific fields.
        // // So I need to merge my candidate from the list, with the updated candidate.
        // let mergedCandidate = {...this.candidate, ...candidate};
        this.candidateChange.emit(candidate);
        if (this.isList) {
          // todo could we return the listShareableDoc fields from the API like we return in the pagedSearch? That way we don't need the setCandidateListDocs method.
          this.setCandidateListDocs();
        } else {
          this.candidateService.updateCandidate(candidate);
        }
        this.saving = false;
      },
      (error) => {
        this.error = error;
        this.saving = false;
      }
    )
  }

  setCandidateListDocs() {
    // In a list, we need to set the updated value of the shareable docs to the candidate as we are switching between.
    // Note: We don't need to do this for regular shareable docs in the view candidate component as
    // the new candidate is loaded when changing tabs or refreshing.
    if (this.shareableCvId != null) {
      this.candidate.listShareableCv = this.cvs.find(att => att.id === this.shareableCvId);
    } else {
      this.candidate.listShareableCv = null;
    }
    if (this.shareableDocId != null) {
      this.candidate.listShareableDoc = this.other.find(att => att.id === this.shareableDocId);
    } else {
      this.candidate.listShareableDoc = null;
    }
    this.candidateService.updateCandidate(this.candidate);
  }

  get shareableCvId() {
    return this.form.value?.shareableCvAttachmentId;
  }

  get shareableDocId() {
    return this.form.value?.shareableDocAttachmentId;
  }

  get isList() {
    return isSavedList(this.candidateSource);
  }

  filterByCv(isCV: boolean) {
    return this.candidate.candidateAttachments?.filter(a => a.cv === isCV);
  }
}
