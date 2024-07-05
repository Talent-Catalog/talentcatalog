import {Component, Input, OnInit} from '@angular/core';
import {WebScraperService} from "../../services/web-scraper.service";
import {LinkPreview} from "../../model/link-preview";

@Component({
  selector: 'app-preview-link',
  templateUrl: './preview-link.component.html',
  styleUrls: ['./preview-link.component.scss']
})
export class PreviewLinkComponent implements OnInit {

  @Input() linkPreview: LinkPreview;

  constructor(private webScraperService: WebScraperService) { }

  ngOnInit(): void { }

  blockPreview(event: Event): void {
    event.stopPropagation()
    event.preventDefault();
    this.linkPreview.blocked = true;
  }

}
