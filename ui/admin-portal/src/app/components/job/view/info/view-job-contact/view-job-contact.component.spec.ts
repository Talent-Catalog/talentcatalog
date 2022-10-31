import {ComponentFixture, TestBed} from '@angular/core/testing';

import {ViewJobContactComponent} from './view-job-contact.component';

describe('ViewJobContactComponent', () => {
  let component: ViewJobContactComponent;
  let fixture: ComponentFixture<ViewJobContactComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ViewJobContactComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ViewJobContactComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
