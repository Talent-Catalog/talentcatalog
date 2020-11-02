import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {ResidenceStatusComponent} from './residence-status.component';

describe('ResidenceStatusComponent', () => {
  let component: ResidenceStatusComponent;
  let fixture: ComponentFixture<ResidenceStatusComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ResidenceStatusComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ResidenceStatusComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
