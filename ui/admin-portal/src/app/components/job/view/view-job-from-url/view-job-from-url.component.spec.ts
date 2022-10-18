import {ComponentFixture, TestBed} from '@angular/core/testing';

import {ViewJobFromUrlComponent} from './view-job-from-url.component';

describe('ViewJobFromUrlComponent', () => {
  let component: ViewJobFromUrlComponent;
  let fixture: ComponentFixture<ViewJobFromUrlComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ViewJobFromUrlComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ViewJobFromUrlComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
