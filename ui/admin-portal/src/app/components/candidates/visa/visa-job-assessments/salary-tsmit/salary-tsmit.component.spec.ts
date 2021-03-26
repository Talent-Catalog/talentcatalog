import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {SalaryTsmitComponent} from './salary-tsmit.component';

describe('SalaryTsmitComponent', () => {
  let component: SalaryTsmitComponent;
  let fixture: ComponentFixture<SalaryTsmitComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ SalaryTsmitComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SalaryTsmitComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
