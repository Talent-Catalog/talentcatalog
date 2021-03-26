import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {VisaJobPutForwardComponent} from './visa-job-put-forward.component';

describe('VisaJobPutForwardComponent', () => {
  let component: VisaJobPutForwardComponent;
  let fixture: ComponentFixture<VisaJobPutForwardComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ VisaJobPutForwardComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(VisaJobPutForwardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
