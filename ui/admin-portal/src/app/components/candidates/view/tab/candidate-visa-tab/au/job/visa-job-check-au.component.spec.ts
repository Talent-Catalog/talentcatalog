import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {VisaJobCheckAuComponent} from './visa-job-check-au.component';

describe('VisaJobCheckAuComponent', () => {
  let component: VisaJobCheckAuComponent;
  let fixture: ComponentFixture<VisaJobCheckAuComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ VisaJobCheckAuComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(VisaJobCheckAuComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
