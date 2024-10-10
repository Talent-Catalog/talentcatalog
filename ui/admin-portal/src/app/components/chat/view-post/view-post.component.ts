import {
  Component,
  ElementRef,
  HostListener,
  Input,
  OnChanges, OnDestroy,
  OnInit,
  SimpleChanges,
  ViewChild,
  ViewEncapsulation
} from '@angular/core';
import {isHtml} from 'src/app/util/string';
import {ChatPost} from "../../../model/chat";
import {UserService} from "../../../services/user.service";
import {AddReactionRequest, ReactionService} from "../../../services/reaction.service";
import {Reaction} from "../../../model/reaction";
import {AuthenticationService} from "../../../services/authentication.service";
import {Subject} from "rxjs";
import {takeUntil} from "rxjs/operators";

@Component({
  selector: 'app-view-post',
  templateUrl: './view-post.component.html',
  styleUrls: ['./view-post.component.scss'],
  //In order to add styling to the innerHtml post content, we need to set this to None.
  // See here: https://stackoverflow.com/a/44215795
  encapsulation: ViewEncapsulation.None
})
export class ViewPostComponent implements OnInit, OnChanges, OnDestroy {

  public reactionPickerVisible: boolean = false;
  isCurrentPost: boolean = false;
  public reactionPickerXPos: number;
  public reactionPickerYPos: number;
  public userIsPostAuthor: boolean;

  private destroyReactionSubscriptions$ = new Subject<void>();

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
  @Input() readOnly = false;

  @ViewChild('thisPost') thisPost: ElementRef;

  constructor(
    private reactionService: ReactionService,
    private authenticationService: AuthenticationService
  ) { }

  ngOnInit(): void {
    this.setUserIsPostAuthor();

    // Subscribe to reaction updates for this post
    this.subscribeToReactionUpdates();
  }

  ngOnChanges(changes: SimpleChanges) {
    for (const propName in changes) {
      if (changes.hasOwnProperty(propName)) {
        switch (propName) {
          case 'currentPost': {
            this.closePickerIfCurrentPostChanged(
              changes.currentPost.currentValue
            )
          }
        }
      }
    }
  }

  ngOnDestroy(): void {
    // Emit a value to signal that subscriptions should complete
    this.destroyReactionSubscriptions$.next();
    this.destroyReactionSubscriptions$.complete(); // Complete the subject
  }

  private subscribeToReactionUpdates() {
    if (this.post && this.post.id) {
      this.reactionService.subscribeToReactions(this.post.id)
      .pipe(takeUntil(this.destroyReactionSubscriptions$)) // Automatically unsubscribe when destroyReactionSubscriptions$ emits
      .subscribe({
        next: (updatedReactions) => {
          this.post.reactions = updatedReactions;
        },
        error: (error) => {
          console.error('Error receiving reaction updates:', error);
        }
      });
    }
  }

  get isHtml() {
    return isHtml;
  }

  get createdBy(): string {
    let user = this.post.createdBy;
    return UserService.userToString(user, false, false);
  }

  //If readonly does nothing.
  //Otherwise, toggles the picker on and off, situating it appropriately in relation to the button
  // The picker's height is 353 x 425 px, navbar is 85px
  public onClickReactionBtn(event) {
    if (!this.readOnly) {
      if (event.clientY < 510 && window.innerHeight - event.clientY < 425) {
        // Won't fit above, won't fit below - place halfway
        this.reactionPickerYPos = event.clientY - 213;
      } else if (window.innerHeight - event.clientY < 425 && event.clientY > 510) {
        // Won't fit below, will fit above - place above
        this.reactionPickerYPos = event.clientY - 425;
      } else {
        // Will fit below, won't fit above OR will fit either side - place below
        this.reactionPickerYPos = event.clientY;
      }
      // Always place to the left of the button without concealing it
      this.reactionPickerXPos = event.clientX - 370;
      this.reactionPickerVisible = !this.reactionPickerVisible;
    }
  }

  // This method may also update or even delete a reaction, if the user submits an emoji already
  // associated with the post. This behaviour is managed by ReactionService on the server.
  public onSelectEmoji(event) {
    this.reactionPickerVisible = false;
    const request: AddReactionRequest = {
      emoji: `${event.emoji.native}`
    }
    this.reactionService.addReaction(this.post.id, request);
  }

  public onSelectReaction(reaction: Reaction) {
    if (!this.readOnly) {
      this.reactionService.modifyReaction(this.post.id, reaction.id);
    }
  }

  private closePickerIfCurrentPostChanged(currentPost: ChatPost) {
    this.isCurrentPost = currentPost === this.post;
    if (!this.isCurrentPost) {
      this.reactionPickerVisible = false;
    }
  }

  // Used to check whether user should see option to block link preview in sent post.
  private setUserIsPostAuthor() {
    this.userIsPostAuthor =
      this.post.createdBy.id === this.authenticationService.getLoggedInUser().id;
  }

}
