import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {SavedListLinkService} from "../../../../services/saved-list-link.service";
import {SavedListLink} from "../../../../model/saved-list-link";
import {SavedListService} from "../../../../services/saved-list.service";
import {SavedList, SearchSavedListRequest} from "../../../../model/saved-list";

@Component({
  selector: 'app-create-external-link',
  templateUrl: './create-external-link.component.html',
  styleUrls: ['./create-external-link.component.scss']
})
export class CreateExternalLinkComponent implements OnInit {

  linkForm: FormGroup;
  error;
  saving: boolean;
  savedLists: SavedList[];
  loading: boolean;

  constructor(private activeModal: NgbActiveModal,
              private fb: FormBuilder,
              private savedListLinkService: SavedListLinkService,
              private savedListService: SavedListService) {
  }

  ngOnInit() {
    this.linkForm = this.fb.group({
      savedListId: [null, Validators.required],
      link: [null, Validators.required],
    });
    const request: SearchSavedListRequest = {

    }
    this.savedListService.search(request).subscribe(
      (response) => {
        this.savedLists = response;
      },
      (error) => {
        this.error = error;
        this.loading = false;
      }
    )
  }

  onSave() {
    this.saving = true;
    this.savedListLinkService.create(this.linkForm.value).subscribe(
      (link) => {
        this.closeModal(link)
        this.saving = false;
      },
      (error) => {
        this.error = error;
        this.saving = false;
      });
  }

  closeModal(link: SavedListLink) {
    this.activeModal.close(link);
  }

  dismiss() {
    this.activeModal.dismiss(false);
  }
}
