import {ComponentFixture, TestBed} from '@angular/core/testing';

import {RelevantWorkExpComponent} from './relevant-work-exp.component';

describe('RelevantWorkExpComponent', () => {
  let component: RelevantWorkExpComponent;
  let fixture: ComponentFixture<RelevantWorkExpComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ RelevantWorkExpComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(RelevantWorkExpComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
