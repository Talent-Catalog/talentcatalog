import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {VisaCheckNzComponent} from './visa-check-nz.component';

describe('VisaCheckNzComponent', () => {
  let component: VisaCheckNzComponent;
  let fixture: ComponentFixture<VisaCheckNzComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ VisaCheckNzComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(VisaCheckNzComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
