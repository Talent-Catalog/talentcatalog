import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {VisaOtherOptionsComponent} from './visa-other-options.component';

describe('VisaOtherOptionsComponent', () => {
  let component: VisaOtherOptionsComponent;
  let fixture: ComponentFixture<VisaOtherOptionsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ VisaOtherOptionsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(VisaOtherOptionsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
