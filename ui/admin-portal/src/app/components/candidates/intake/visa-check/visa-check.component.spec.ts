import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {VisaCheckComponent} from './visa-check.component';

describe('VisaCheckComponent', () => {
  let component: VisaCheckComponent;
  let fixture: ComponentFixture<VisaCheckComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ VisaCheckComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(VisaCheckComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
