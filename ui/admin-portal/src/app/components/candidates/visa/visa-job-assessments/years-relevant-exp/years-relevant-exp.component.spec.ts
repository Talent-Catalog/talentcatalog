import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {YearsRelevantExpComponent} from './years-relevant-exp.component';

describe('YearsRelevantExpComponent', () => {
  let component: YearsRelevantExpComponent;
  let fixture: ComponentFixture<YearsRelevantExpComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ YearsRelevantExpComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(YearsRelevantExpComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
