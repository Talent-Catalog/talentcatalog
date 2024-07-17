import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {JobChat} from "../../../model/chat";
import {Candidate} from "../../../model/candidate";

@Component({
  selector: 'app-view-source-candidates-with-chats',
  templateUrl: './view-source-candidates-with-chats.component.html',
  styleUrls: ['./view-source-candidates-with-chats.component.scss']
})
export class ViewSourceCandidatesWithChatsComponent implements OnInit {

  // TODO: takes an array of source partner's JobChats of type CandidateProspect that have messages,
  //  displays a list of candidates with whom the chats are associated and returns the candidate to
  //  the parent component when clicked. See view-job-source-contacts for something similar.

  @Input() sourceCandidateChats: JobChat[];
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
