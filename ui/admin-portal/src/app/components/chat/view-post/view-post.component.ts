import {
  Component,
  HostListener,
  Input,
  OnInit,
  SimpleChanges,
  ViewEncapsulation
} from '@angular/core';
import {isHtml} from 'src/app/util/string';
import {ChatPost} from "../../../model/chat";
import {UserService} from "../../../services/user.service";
import {User} from "../../../model/user";
import {AuthenticationService} from "../../../services/authentication.service";
import {
  ChatPostReactionService,
  CreateChatPostReactionRequest
} from "../../../services/chat-post-reaction-service";

@Component({
  selector: 'app-view-post',
  templateUrl: './view-post.component.html',
  styleUrls: ['./view-post.component.scss'],
  //In order to add styling to the innerHtml post content, we need to set this to None.
  // See here: https://stackoverflow.com/a/44215795
  encapsulation: ViewEncapsulation.None
})
export class ViewPostComponent implements OnInit {

  emojiReactPickerVisible: boolean = false;
  loggedInUser: User;

  // Currently ngx-quill just inserts the url into an <img> tag, this is then saved as innerHTML.
  // Adding this event listener allows us to make the images clickable and open the src attribute in a new tab.
  @HostListener('click', ['$event'])
  public onClick(event: any) {
    if (event.target.tagName == "IMG") {
      window.open(event.target.getAttribute('src'), "_blank");
    }
  }

  @Input() post: ChatPost;

  constructor(
    private authenticationService: AuthenticationService,
    private chatPostReactionService: ChatPostReactionService
  ) { }

  ngOnInit(): void {
    this.loggedInUser = this.authenticationService.getLoggedInUser();
  }

  ngOnChanges(changes: SimpleChanges) {
    console.log(this.post)
  }

  get isHtml() {
    return isHtml;
  }

  get createdBy(): string {
    let user = this.post.createdBy;
    return UserService.userToString(user, false, false);
  }

  public clickEmojiButton() {
    this.emojiReactPickerVisible = !this.emojiReactPickerVisible;
  }

  public addReaction(post, event) {
    this.emojiReactPickerVisible = !this.emojiReactPickerVisible;
    this.createReaction(post.id,`${event.emoji.native}`)
  }

  private createReaction(postId: number, emoji: string) {
    const createReactionRequest: CreateChatPostReactionRequest = {
      emoji: emoji,
      userIds: [this.loggedInUser.id,]
    }

    this.chatPostReactionService.create(postId, createReactionRequest).subscribe(
      (chatPostReaction) => {
        this.post.chatPostReactions.push(chatPostReaction);
      },
      (error) => {
        console.log("nope")
      });
  }

}
