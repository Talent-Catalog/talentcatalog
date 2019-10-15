import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import {CandidateService} from "../../../../services/candidate.service";
import {EditCandidateShortlistItemComponent} from "./edit/edit-candidate-shortlist-item.component";
import {SavedSearch} from "../../../../model/saved-search";
import {CandidateShortlistItem} from "../../../../model/candidate-shortlist-item";

@Component({
  selector: 'app-candidate-shortlist-item',
  templateUrl: './candidate-shortlist-item.component.html',
  styleUrls: ['./candidate-shortlist-item.component.scss']
})
export class CandidateShortlistItemComponent implements OnInit {

  @Input() candidateId: number;
  @Input() candidateShortlistItem: CandidateShortlistItem;
  @Input() savedSearch: SavedSearch;

  @Output() shortlistItemSaved = new EventEmitter();

  loading: boolean;
  error;

  constructor(private candidateService: CandidateService,
              private modalService: NgbModal) { }

  ngOnInit() {
    console.log('candidateShortListItemId', this.candidateShortlistItem);
    console.log('candidateId', this.candidateId);
    console.log('savedSearch', this.savedSearch);
  }


  editShortlistItem() {
    const editModal = this.modalService.open(EditCandidateShortlistItemComponent, {
      centered: true,
      backdrop: 'static'
    });

    editModal.componentInstance.candidateShortListItemId = this.candidateShortlistItem ? this.candidateShortlistItem.id : null;
    editModal.componentInstance.candidateId = this.candidateId;
    editModal.componentInstance.savedSearch = this.savedSearch;

    editModal.result
      .then((candidateShortListItem) => {
        this.shortlistItemSaved.emit(candidateShortListItem);
      })
      .catch(() => { /* Isn't possible */ });

  }


}
