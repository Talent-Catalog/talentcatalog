import {
  Component,
  ElementRef,
  HostListener,
  Input,
  OnInit,
  ViewChild,
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
export class ViewPostComponent implements OnInit {

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

  @ViewChild('thisPost') thisPost: ElementRef;

  constructor(private reactionService: ReactionService) { }

  ngOnInit(): void {
  }

  get isHtml() {
    return isHtml;
  }

  get createdBy(): string {
    let user = this.post.createdBy;
    return UserService.userToString(user, false, false);
  }

  // Toggles the picker on and off — if on, focuses the scroll bar on its post
  public onClickReactionBtn() {
    this.reactionPickerVisible = !this.reactionPickerVisible;
    if(this.reactionPickerVisible) {
      setTimeout(() => {
        this.thisPost.nativeElement.scrollIntoView({behavior: 'smooth'});
      });
    }
  }

  // This method may also update or even delete a reaction, if the user submits an emoji already
  // associated with the post. This behaviour is managed by ReactionService on the server.
  public onSelectEmoji(event) {
    this.reactionPickerVisible = false;
    const request: CreateReactionRequest = {
      emoji: `${event.emoji.native}`
    }
    this.reactionService.createReaction(this.post.id, request)
                          .subscribe({
                            next: (updatedReactions) =>
                            this.post.reactions = updatedReactions
                          })
  }

  public onSelectReaction(reaction: Reaction) {
    this.reactionService.updateReaction(reaction.id)
                          .subscribe({
                            next: (updatedReactions) =>
                            this.post.reactions = updatedReactions
                          })
  }

  // These emojis don't work for some reason — this function excludes them from the picker.
  emojisToShowFilter = (emoji: any) => {
    return emoji.shortName !== 'relaxed' && emoji.shortName !== 'white_frowning_face'
  }

  // Below commented-out methods and class property were closing unwanted emoji pickers.
  // Leaving them here as they're likely to be useful for future styling and functionality.
  // Used in conjunction with @Input currentPost.

  // isCurrentPost: boolean = false;

  // ngOnChanges(changes: SimpleChanges) {
  //   for (const propName in changes) {
  //     if (changes.hasOwnProperty(propName)) {
  //       switch (propName) {
  //         case 'currentPost': {
  //           this.setIsCurrentPost(
  //             changes.currentPost.currentValue
  //           )
  //         }
  //       }
  //     }
  //   }
  // }
  //
  // private setIsCurrentPost(currentPost: ChatPost) {
  //   this.isCurrentPost = currentPost === this.post;
  // }

}
