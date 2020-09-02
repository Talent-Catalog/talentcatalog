import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {CvIconComponent} from './cv-icon.component';

describe('CvIconComponent', () => {
  let component: CvIconComponent;
  let fixture: ComponentFixture<CvIconComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CvIconComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CvIconComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
