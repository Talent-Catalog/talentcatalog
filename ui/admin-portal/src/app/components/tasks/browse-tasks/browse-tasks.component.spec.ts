import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BrowseTasksComponent } from './browse-tasks.component';

describe('BrowseTasksComponent', () => {
  let component: BrowseTasksComponent;
  let fixture: ComponentFixture<BrowseTasksComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ BrowseTasksComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(BrowseTasksComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
