import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {VisaJobNotesComponent} from './visa-job-notes.component';

describe('VisaJobNotesComponent', () => {
  let component: VisaJobNotesComponent;
  let fixture: ComponentFixture<VisaJobNotesComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ VisaJobNotesComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(VisaJobNotesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
