import {Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {ChatPost, JobChat} from "../../../model/chat";

@Component({
  selector: 'app-posts',
  templateUrl: './posts.component.html',
  styleUrls: ['./posts.component.scss']
})
export class PostsComponent implements OnInit, OnChanges {

  @Input() chat: JobChat;

  currentPost: ChatPost;
  posts: ChatPost[];
  loading: boolean;
  error;

  constructor() { }

  ngOnInit(): void {
    //todo Load p
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.loadPosts();
  }

  private loadPosts() {
    //todo
    this.selectCurrent(null);
  }

  selectCurrent(post: ChatPost) {
    this.currentPost = post;
  }
}
