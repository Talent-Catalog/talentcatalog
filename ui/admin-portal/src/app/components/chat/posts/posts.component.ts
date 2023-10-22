import {Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {ChatPost, JobChat} from "../../../model/chat";
import {ChatPostService} from "../../../services/chat-post.service";
import {Message} from "@stomp/stompjs";
import {ChatService} from "../../../services/chat.service";

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

  constructor(
      private chatService: ChatService,
      private chatPostService: ChatPostService,
  ) {}

  ngOnInit(): void {
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (this.chat) {
      this.loadPosts();

      //Subscribe for updates
      this.chatService.subscribe(this.chat)
      .subscribe((message: Message) => {
        const payload: ChatPost = JSON.parse(message.body);
        this.addNewPost(payload);
      });
    }
  }

  private loadPosts() {
    if (this.chat) {
      this.loading = false;
      this.error = null;
      this.chatPostService.listPosts(this.chat.id).subscribe(
          posts => {
            this.posts = posts;
            this.loading = false;
          },
          error => {
            this.error = error;
            this.loading = false;
          });
      this.selectCurrent(null);
    }
  }

  selectCurrent(post: ChatPost) {
    this.currentPost = post;
  }

  private addNewPost(post: ChatPost) {
    this.posts.push(post);
  }
}
