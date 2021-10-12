import {Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {AutoSaveComponentBase} from "../../../util/autosave/AutoSaveComponentBase";
import {Candidate, UpdateCandidateShareableDocsRequest} from "../../../../model/candidate";
import {FormBuilder} from "@angular/forms";
import {Observable} from "rxjs";
import {CandidateAttachment} from "../../../../model/candidate-attachment";
import {CandidateService} from "../../../../services/candidate.service";
import {SavedList} from "../../../../model/saved-list";

@Component({
  selector: 'app-shareable-docs',
  templateUrl: './shareable-docs.component.html',
  styleUrls: ['./shareable-docs.component.scss']
})
export class ShareableDocsComponent extends AutoSaveComponentBase implements OnInit, OnChanges {

  @Input() candidate: Candidate;
  @Input() savedList: SavedList;

  @Input() cvs: CandidateAttachment[];
  @Input() other: CandidateAttachment[];

  constructor(private fb: FormBuilder,
              private candidateService: CandidateService) {
    super();
  }

  ngOnInit() {
    if (this.savedList) {
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

  }

  doSave(formValue: any): Observable<Candidate> {
    const request: UpdateCandidateShareableDocsRequest = {
      shareableCvAttachmentId: formValue.shareableCvAttachmentId,
      shareableDocAttachmentId: formValue.shareableDocAttachmentId
    }
    if (this.savedList) {
      request.savedListId = this.savedList.id;
    }
    return this.candidateService.updateShareableDocs(this.candidate.id, request);
  }

  onSuccessfulSave() {
    // How to avoid loosing the value in the front end when changing? Have only the ID not the full value so can't set
    // with form value.
    if (this.savedList) {
      if (this.shareableCvId != null) {
        this.candidate.listShareableCv = this.cvs.find(att => att.id === this.shareableCvId);
      }
      if (this.shareableDocId != null) {
        this.candidate.listShareableDoc = this.other.find(att => att.id === this.shareableDocId);
      }
    }

  }

  get shareableCvId() {
    return this.form.value?.shareableCvAttachmentId;
  }

  get shareableDocId() {
    return this.form.value?.shareableDocAttachmentId;
  }

  ngOnChanges(changes: SimpleChanges): void {
    //Replace the form value with the new candidates context notes when
    //changing from one candidate to the next or when selection has changed.
    if (this.form) {
      if (this.savedList) {
        this.form.controls['shareableCvAttachmentId'].patchValue(this.candidate?.listShareableCv?.id);
        this.form.controls['shareableDocAttachmentId'].patchValue(this.candidate?.listShareableDoc?.id);
      } else {
        this.form.controls['shareableCvAttachmentId'].patchValue(this.candidate?.shareableCv?.id);
        this.form.controls['shareableDocAttachmentId'].patchValue(this.candidate?.shareableDoc?.id);
      }
    }
  }

}
