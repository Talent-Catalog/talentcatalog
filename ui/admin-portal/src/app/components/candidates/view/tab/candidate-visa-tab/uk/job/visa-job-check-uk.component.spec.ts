import {ComponentFixture, TestBed} from '@angular/core/testing';

import {VisaJobCheckUkComponent} from './visa-job-check-uk.component';

describe('VisaJobCheckUkComponent', () => {
  let component: VisaJobCheckUkComponent;
  let fixture: ComponentFixture<VisaJobCheckUkComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ VisaJobCheckUkComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(VisaJobCheckUkComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
