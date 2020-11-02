import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {LeftHomeReasonComponent} from './left-home-reason.component';

describe('LeftHomeReasonComponent', () => {
  let component: LeftHomeReasonComponent;
  let fixture: ComponentFixture<LeftHomeReasonComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ LeftHomeReasonComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(LeftHomeReasonComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
