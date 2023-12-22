import {Component, HostListener, Input, OnInit, ViewEncapsulation} from '@angular/core';
import {isHtml} from 'src/app/util/string';
import {ChatPost} from "../../../model/chat";

@Component({
  selector: 'app-view-post',
  templateUrl: './view-post.component.html',
  styleUrls: ['./view-post.component.scss'],
  //In order to add styling to the innerHtml post content, we need to set this to None.
  // See here: https://stackoverflow.com/a/44215795
  encapsulation: ViewEncapsulation.None,
})
export class ViewPostComponent implements OnInit {

  // Currently ngx-quill just inserts the url into an <img> tag, this is then saved as innerHTML.
  // Adding this event listener allows us to make the images clickable and open the src attribute in a new tab.
  @HostListener('click', ['$event'])
  public onClick(event: any) {
    if (event.target.tagName == "IMG") {
      window.open(event.target.getAttribute('src'), "_blank");
    }
  }

  @Input() post: ChatPost;
  @Input() fromMe: boolean;

  constructor() { }

  ngOnInit(): void {
  }

  get isHtml() {
    return isHtml;
  }

}
