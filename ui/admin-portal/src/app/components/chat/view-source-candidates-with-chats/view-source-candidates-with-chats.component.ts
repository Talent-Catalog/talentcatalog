import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {Candidate} from "../../../model/candidate";
import {SearchResults} from "../../../model/search-results";

@Component({
  selector: 'app-view-source-candidates-with-chats',
  templateUrl: './view-source-candidates-with-chats.component.html',
  styleUrls: ['./view-source-candidates-with-chats.component.scss']
})
export class ViewSourceCandidatesWithChatsComponent implements OnInit {

  @Input() candidatesWithActiveChats: SearchResults<Candidate>;
  @Output() candidateSelection = new EventEmitter<Candidate>();

  constructor() { }

  ngOnInit(): void {
    this.populateCandidateList()
  }

  public onCandidateSelected() {
    this.candidateSelection.emit();
  }

  private populateCandidateList() {
    // takes sourceCandidateChats and converts to candidates to be displayed in the view
  }

}
