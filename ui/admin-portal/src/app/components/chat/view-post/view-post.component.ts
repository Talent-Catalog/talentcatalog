import {
  Component,
  HostListener,
  Input,
  OnChanges,
  OnInit,
  SimpleChanges,
  ViewEncapsulation
} from '@angular/core';
import {isHtml} from 'src/app/util/string';
import {ChatPost} from "../../../model/chat";
import {UserService} from "../../../services/user.service";
import {CreateReactionRequest, ReactionService} from "../../../services/reaction.service";
import {Reaction} from "../../../model/reaction";

@Component({
  selector: 'app-view-post',
  templateUrl: './view-post.component.html',
  styleUrls: ['./view-post.component.scss'],
  //In order to add styling to the innerHtml post content, we need to set this to None.
  // See here: https://stackoverflow.com/a/44215795
  encapsulation: ViewEncapsulation.None
})
export class ViewPostComponent implements OnInit, OnChanges {

  isCurrentPost: boolean = false;
  public reactionPickerVisible: boolean = false;

  // Currently ngx-quill just inserts the url into an <img> tag, this is then saved as innerHTML.
  // Adding this event listener allows us to make the images clickable and open the src attribute in a new tab.
  @HostListener('click', ['$event'])
  public onClick(event: any) {
    if (event.target.tagName == "IMG") {
      window.open(event.target.getAttribute('src'), "_blank");
    }
  }

  @Input() post: ChatPost;
  @Input() currentPost: ChatPost;

  constructor(private reactionService: ReactionService) { }

  ngOnInit(): void {
  }

  ngOnChanges(changes: SimpleChanges) {
    for (const propName in changes) {
      if (changes.hasOwnProperty(propName)) {
        switch (propName) {
          case 'currentPost': {
            this.setIsCurrentPost(changes.currentPost.currentValue)
          }
        }
      }
    }
  }

  get isHtml() {
    return isHtml;
  }

  get createdBy(): string {
    let user = this.post.createdBy;
    return UserService.userToString(user, false, false);
  }

  private setIsCurrentPost(currentPost: ChatPost) {
    this.isCurrentPost = currentPost === this.post;
    if(!this.isCurrentPost) this.reactionPickerVisible = false;
  }

  public openReactionPicker() {
    this.reactionPickerVisible = !this.reactionPickerVisible;
  }

  public emojiSelect(event) {
    const request: CreateReactionRequest = {
      emoji: `${event.emoji.native}`
    }
    this.reactionService.create(this.post.id, request).subscribe({
      complete: () =>
          this.reactionService.list(this.post.id)
          .subscribe(updatedReactions =>
              this.post.reactions = updatedReactions
          )})
  }

  public updateReaction(reaction: Reaction) {
    this.reactionService.update(reaction.id).subscribe({
      complete: () =>
          this.reactionService.list(this.post.id)
          .subscribe(updatedReactions =>
              this.post.reactions = updatedReactions
          )})
  }


}
