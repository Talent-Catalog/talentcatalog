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
import {ViewTaskDetailsComponent} from "./view-task-details.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {MockCandidate} from "../../../MockData/MockCandidate";
import {UploadType} from "../../../model/task";
import {UpdatedByComponent} from "../../util/user/updated-by/updated-by.component";

describe('ViewTaskDetailsComponent', () => {
  let component: ViewTaskDetailsComponent;
  let fixture: ComponentFixture<ViewTaskDetailsComponent>;

  const mockCandidate = new MockCandidate();
  const mockTasks = mockCandidate.taskAssignments.slice(0, 3).map(assignment => assignment.task);

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ViewTaskDetailsComponent,UpdatedByComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ViewTaskDetailsComponent);
    component = fixture.componentInstance;
    component.task = mockTasks[0];
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should display task details correctly', () => {
    const compiled = fixture.nativeElement;
    expect(compiled.querySelector('strong').textContent).toContain(mockTasks[0].displayName);
    expect(compiled.querySelector('span').textContent).toContain(mockTasks[0].taskType);
    expect(compiled.querySelector('.font-italic').textContent).toContain(mockTasks[0].description);
  });

  it('should set uploadTypeString on ngOnInit', () => {
    expect(component.uploadTypeString).toBe('cv');
  });

  it('should update uploadTypeString on ngOnChanges', () => {
    const newTask = { ...mockTasks[0], uploadType: UploadType.degree };
    component.task = newTask;
    component.ngOnChanges({});
    expect(component.uploadTypeString).toBe('degree');
  });
});

