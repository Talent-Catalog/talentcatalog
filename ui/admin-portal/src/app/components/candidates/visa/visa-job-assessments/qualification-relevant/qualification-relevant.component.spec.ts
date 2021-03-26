import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {QualificationRelevantComponent} from './qualification-relevant.component';

describe('QualificationRelevantComponent', () => {
  let component: QualificationRelevantComponent;
  let fixture: ComponentFixture<QualificationRelevantComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ QualificationRelevantComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(QualificationRelevantComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
