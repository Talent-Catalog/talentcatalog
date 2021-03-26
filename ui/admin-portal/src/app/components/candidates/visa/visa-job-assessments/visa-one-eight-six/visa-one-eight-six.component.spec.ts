import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {VisaOneEightSixComponent} from './visa-one-eight-six.component';

describe('VisaOneEightSixComponent', () => {
  let component: VisaOneEightSixComponent;
  let fixture: ComponentFixture<VisaOneEightSixComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ VisaOneEightSixComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(VisaOneEightSixComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
