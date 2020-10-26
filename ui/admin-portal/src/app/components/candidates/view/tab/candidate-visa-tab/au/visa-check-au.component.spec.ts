import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {VisaCheckAuComponent} from './visa-check-au.component';

describe('AuComponent', () => {
  let component: VisaCheckAuComponent;
  let fixture: ComponentFixture<VisaCheckAuComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ VisaCheckAuComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(VisaCheckAuComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
