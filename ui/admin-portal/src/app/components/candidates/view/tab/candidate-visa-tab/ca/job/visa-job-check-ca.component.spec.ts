import {ComponentFixture, TestBed} from '@angular/core/testing';

import {VisaJobCheckCaComponent} from './visa-job-check-ca.component';

describe('VisaJobCheckCaComponent', () => {
  let component: VisaJobCheckCaComponent;
  let fixture: ComponentFixture<VisaJobCheckCaComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ VisaJobCheckCaComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(VisaJobCheckCaComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
