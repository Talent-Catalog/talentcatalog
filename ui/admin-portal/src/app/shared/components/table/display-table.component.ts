import {Component, Input} from '@angular/core';

export type TableType = 'Basic' | 'Striped' | 'Dropdown';

@Component({
  selector: 'app-display-table',
  templateUrl: './display-table.component.html',
  styleUrls: ['./display-table.component.scss']
})
export class DisplayTableComponent {

  @Input() name: string;
  @Input() columns: string[] = ["Name", "Stage", "Created", "Due"];
  // todo data this a SearchResults<any> type so we can do pagination
  @Input() data: any[] = [
    {name: "Blue Mountains Highway Motel",
      stage: "Recruitement Process",
      created: "2023-03-14",
      due: "2023-03-14"},
    {name: "DAIS Build Pty Ltd",
      stage: "Visa Eligibility",
      created: "2023-11-03",
      due: "-"}];
  @Input() type: TableType = 'Basic';

}
