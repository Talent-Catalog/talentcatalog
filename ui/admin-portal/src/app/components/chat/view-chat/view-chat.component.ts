import {Component, Input, OnInit} from '@angular/core';
import {JobChat} from "../../../model/chat";

@Component({
  selector: 'app-view-chat',
  templateUrl: './view-chat.component.html',
  styleUrls: ['./view-chat.component.scss']
})
export class ViewChatComponent implements OnInit {

  @Input() chat: JobChat
  constructor() { }

  ngOnInit(): void {
  }

}
