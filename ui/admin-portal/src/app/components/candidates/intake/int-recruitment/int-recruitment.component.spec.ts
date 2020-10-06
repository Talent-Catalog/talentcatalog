import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {IntRecruitmentComponent} from './int-recruitment.component';

describe('IntRecruitmentComponent', () => {
  let component: IntRecruitmentComponent;
  let fixture: ComponentFixture<IntRecruitmentComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ IntRecruitmentComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(IntRecruitmentComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
