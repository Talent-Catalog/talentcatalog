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

import {Component, Input, OnInit} from '@angular/core';
import {LinkPreview} from "../../../model/link-preview";
import {LinkPreviewService} from "../../../services/link-preview.service";

@Component({
  selector: 'app-preview-link',
  templateUrl: './preview-link.component.html',
  styleUrls: ['./preview-link.component.scss']
})
export class PreviewLinkComponent implements OnInit {

  @Input() linkPreview: LinkPreview;
  @Input() userIsPostAuthor: boolean;

  constructor(private linkPreviewService: LinkPreviewService) { }

  ngOnInit(): void { }

  public blockLinkPreview(event: Event): void {
    event.stopPropagation()
    event.preventDefault();
    this.linkPreview.blocked = true;

    // If this is a sent post, we also want to delete the preview from the DB
    if (this.linkPreview.id) {
      this.deleteLinkPreview()
    }
  }

  private deleteLinkPreview() {
    this.linkPreviewService.delete(this.linkPreview.id).subscribe()
  }

}
