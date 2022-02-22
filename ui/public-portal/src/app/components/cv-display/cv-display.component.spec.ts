import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { CvDisplayComponent } from './cv-display.component';

describe('CvDisplayComponent', () => {
  let component: CvDisplayComponent;
  let fixture: ComponentFixture<CvDisplayComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CvDisplayComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CvDisplayComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
