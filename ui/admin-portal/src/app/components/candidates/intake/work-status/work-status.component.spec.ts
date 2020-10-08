import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {WorkStatusComponent} from './work-status.component';

describe('WorkDesiredComponent', () => {
  let component: WorkStatusComponent;
  let fixture: ComponentFixture<WorkStatusComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ WorkStatusComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(WorkStatusComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
