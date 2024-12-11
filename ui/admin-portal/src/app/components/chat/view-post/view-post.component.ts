/*
 * Copyright (c) 2024 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

import {
  Component,
  ElementRef,
  HostListener,
  Input,
  OnChanges,
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

@Component({
  selector: 'app-view-post',
  templateUrl: './view-post.component.html',
  styleUrls: ['./view-post.component.scss'],
  //In order to add styling to the innerHtml post content, we need to set this to None.
  // See here: https://stackoverflow.com/a/44215795
  encapsulation: ViewEncapsulation.None
})
export class ViewPostComponent implements OnInit, OnChanges {

  public reactionPickerVisible: boolean = false;
  isCurrentPost: boolean = false;
  public reactionPickerXPos: number;
  public reactionPickerYPos: number;
  public userIsPostAuthor: boolean;

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
    this.setUserIsPostAuthor()
  }

  ngOnChanges(changes: SimpleChanges) {
    for (const propName in changes) {
      if (changes.hasOwnProperty(propName)) {
        switch (propName) {
          case 'currentPost': {
            this.setIsCurrentPostClosePickerIfFalse(
              changes.currentPost.currentValue
            )
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

   // Returns the abbreviation of the user who created the post, if it exists.
   getUserAbbreviation(input: string): string | null {
    const match = input.match(/\(([^)]+)\)/);
    return match ? match[1] : null;
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
    this.reactionService.addReaction(this.post.id, request)
                          .subscribe({
                            next: (updatedReactions) =>
                            this.post.reactions = updatedReactions
                          })
  }

  public onSelectReaction(reaction: Reaction) {
    if (!this.readOnly) {
      this.reactionService.modifyReaction(this.post.id, reaction.id)
      .subscribe({
        next: (updatedReactions) =>
          this.post.reactions = updatedReactions
      })
    }
  }

  private setIsCurrentPostClosePickerIfFalse(currentPost: ChatPost) {
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
