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
