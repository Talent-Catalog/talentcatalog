import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {WorkAbroadComponent} from './work-abroad.component';

describe('WorkAbroadComponent', () => {
  let component: WorkAbroadComponent;
  let fixture: ComponentFixture<WorkAbroadComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ WorkAbroadComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(WorkAbroadComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
