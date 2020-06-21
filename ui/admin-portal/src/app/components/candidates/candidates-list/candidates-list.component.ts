import {Component, OnInit} from '@angular/core';
import {SavedList} from "../../../model/saved-list";

@Component({
  selector: 'app-candidates-list',
  templateUrl: './candidates-list.component.html',
  styleUrls: ['./candidates-list.component.scss']
})
export class CandidatesListComponent implements OnInit {
  error: string;
  loading: boolean;
  pageNumber: number;
  pageSize: number;
  savedList: SavedList;
  private savedListId: number;

  constructor() { }

  ngOnInit() {
    //todo Load list from params
  }

}
