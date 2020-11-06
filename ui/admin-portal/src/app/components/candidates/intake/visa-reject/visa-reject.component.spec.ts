import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {VisaRejectComponent} from './visa-reject.component';

describe('VisaRejectComponent', () => {
  let component: VisaRejectComponent;
  let fixture: ComponentFixture<VisaRejectComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ VisaRejectComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(VisaRejectComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
