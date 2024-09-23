import {Component, Input, OnInit} from '@angular/core';
import {HelpLink, SearchHelpLinkRequest} from "../../model/help-link";
import {HelpLinkService} from "../../services/help-link.service";

/**
 * Component which takes a SearchHelpLinkRequest as input. It also displays a standard help icon
 * button which will fetch the help links and display them as a tool tip when it is clicked.
 */
@Component({
  selector: 'app-help',
  templateUrl: './help.component.html',
  styleUrls: ['./help.component.scss']
})
export class HelpComponent implements OnInit {
  @Input() request: SearchHelpLinkRequest;
  error: any;
  loading: boolean;

  helpLinks: HelpLink[];

  constructor(
    private helpLinkService: HelpLinkService,
  ) { }

  ngOnInit(): void {
  }

  fetchHelp() {
    if (this.request) {
      this.error = null;
      this.loading = true;

      this.helpLinkService.fetch(this.request).subscribe(
        helpLinks => {
          this.helpLinks = helpLinks;
          this.loading = false;
        },
        error => {
          this.error = error;
          this.loading = false;
        });
    }
  }

  displayHelpLink(helpLink: HelpLink) {
    let s = helpLink.label;
    if (helpLink.country) {
      s += " (" + helpLink.country.name + ")";
    }
    return s;
  }
}
