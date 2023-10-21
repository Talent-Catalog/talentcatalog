import {Component, Input, OnChanges, OnDestroy, OnInit, SimpleChanges} from '@angular/core';
import {ChatPost, JobChat} from "../../../model/chat";
import {ChatPostService} from "../../../services/chat-post.service";
import {Message} from "@stomp/stompjs";
import {RxStompService} from "../../../services/rx-stomp.service";
import {AuthService} from "../../../services/auth.service";
import {Subscription} from "rxjs";

@Component({
  selector: 'app-posts',
  templateUrl: './posts.component.html',
  styleUrls: ['./posts.component.scss']
})
export class PostsComponent implements OnInit, OnChanges, OnDestroy {

  @Input() chat: JobChat;

  currentPost: ChatPost;
  posts: ChatPost[];
  loading: boolean;
  error;

  // @ts-ignore, to suppress warning related to being undefined
  private topicSubscription: Subscription;

  constructor(private chatPostService: ChatPostService,
              private authService: AuthService,
              private rxStompService: RxStompService
  ) { }

  ngOnInit(): void {

    this.rxStompService.configure(this.authService.getRxStompConfig());
    this.rxStompService.activate();

    if (this.chat) {
      this.topicSubscription = this.rxStompService
      .watch('/topic/chat/' + this.chat.id)
      .subscribe((message: Message) => {
        const payload: ChatPost = JSON.parse(message.body);
        this.addNewPost(payload);
      });
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.loadPosts();
  }

  ngOnDestroy() {
    this.topicSubscription.unsubscribe();
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
