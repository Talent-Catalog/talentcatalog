import {Component, Input, OnInit} from '@angular/core';
import { isHtml } from 'src/app/util/string';
import {ChatPost} from "../../../model/chat";

@Component({
  selector: 'app-view-post',
  templateUrl: './view-post.component.html',
  styleUrls: ['./view-post.component.scss']
})
export class ViewPostComponent implements OnInit {

  @Input() post: ChatPost;

  constructor() { }

  ngOnInit(): void {
  }

  get isHtml() {
    return isHtml;
  }

}
