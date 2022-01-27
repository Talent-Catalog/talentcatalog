import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ViewTaskDetailsComponent } from './view-task-details.component';

describe('ViewTaskDetailsComponent', () => {
  let component: ViewTaskDetailsComponent;
  let fixture: ComponentFixture<ViewTaskDetailsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ViewTaskDetailsComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ViewTaskDetailsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
