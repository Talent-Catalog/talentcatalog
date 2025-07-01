import {Component, EventEmitter, Input, Output} from '@angular/core';
import {NgbNavChangeEvent} from "@ng-bootstrap/ng-bootstrap";
import {LocalStorageService} from "../../../services/local-storage.service";
import {Location} from "@angular/common";

@Component({
  selector: 'app-tc-tabs',
  templateUrl: './tc-tabs.component.html',
  styleUrls: ['./tc-tabs.component.scss']
})
export class TcTabsComponent {
  @Input() activeTabId: string;
  @Output() tabChanged = new EventEmitter<any>();

  constructor(private localStorageService: LocalStorageService,
              private location: Location){}

  onTabChanged(event: NgbNavChangeEvent) {
    this.tabChanged.emit(event);
  }
}
