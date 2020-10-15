import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {AsylumYearComponent} from './asylum-year.component';

describe('AsylumYearComponent', () => {
  let component: AsylumYearComponent;
  let fixture: ComponentFixture<AsylumYearComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ AsylumYearComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AsylumYearComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
