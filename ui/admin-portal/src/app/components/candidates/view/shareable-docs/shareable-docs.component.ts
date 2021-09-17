import {Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {AutoSaveComponentBase} from "../../../util/autosave/AutoSaveComponentBase";
import {Candidate, UpdateCandidateShareableDocsRequest} from "../../../../model/candidate";
import {FormBuilder} from "@angular/forms";
import {Observable} from "rxjs";
import {CandidateAttachment} from "../../../../model/candidate-attachment";
import {CandidateService} from "../../../../services/candidate.service";

@Component({
  selector: 'app-shareable-docs',
  templateUrl: './shareable-docs.component.html',
  styleUrls: ['./shareable-docs.component.scss']
})
export class ShareableDocsComponent extends AutoSaveComponentBase implements OnInit, OnChanges {

  @Input() candidate: Candidate;

  @Input() cvs: CandidateAttachment[];
  @Input() other: CandidateAttachment[];

  constructor(private fb: FormBuilder,
              private candidateService: CandidateService) {
    super();
  }

  ngOnInit() {
    this.form = this.fb.group({
      shareableCvAttachmentId: [this.candidate?.shareableCv?.id],
      shareableDocAttachmentId: [this.candidate?.shareableDoc?.id],
    });
  }

  doSave(formValue: any): Observable<Candidate> {
    const request: UpdateCandidateShareableDocsRequest = {
      shareableCvAttachmentId: formValue.shareableCvAttachmentId,
      shareableDocAttachmentId: formValue.shareableDocAttachmentId,
    }
    return this.candidateService.updateShareableDocs(this.candidate.id, request);
  }

  ngOnChanges(changes: SimpleChanges): void {
  }

  onSuccessfulSave(): void {
  }

}
