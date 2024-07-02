import {Component, Input, OnInit} from '@angular/core';
import {WebScraperService} from "../../services/web-scraper.service";

@Component({
  selector: 'app-preview-link',
  templateUrl: './preview-link.component.html',
  styleUrls: ['./preview-link.component.scss']
})
export class PreviewLinkComponent implements OnInit {

  @Input() link: string;

  constructor(private webScraperService: WebScraperService) { }

  ngOnInit(): void {
    console.log(this.link)
  }


}
