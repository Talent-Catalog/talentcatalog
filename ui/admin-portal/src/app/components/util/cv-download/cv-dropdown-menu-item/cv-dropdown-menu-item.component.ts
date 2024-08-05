import { Component, OnInit } from '@angular/core';
import {CvDownloadBaseComponent} from "../cv-download-base/cv-download-base.component";

@Component({
  selector: 'app-cv-dropdown-menu-item',
  templateUrl: './cv-dropdown-menu-item.component.html',
  styleUrls: ['./cv-dropdown-menu-item.component.scss']
})
export class CvDropdownMenuItemComponent extends CvDownloadBaseComponent implements OnInit { }
