import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {VisaCheckCaComponent} from './visa-check-ca.component';

describe('VisaCheckCaComponent', () => {
  let component: VisaCheckCaComponent;
  let fixture: ComponentFixture<VisaCheckCaComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ VisaCheckCaComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(VisaCheckCaComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
