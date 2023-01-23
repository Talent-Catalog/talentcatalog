import {ComponentFixture, TestBed} from '@angular/core/testing';

import {ViewJobUploadsComponent} from './view-job-uploads.component';

describe('ViewJobUploadsComponent', () => {
  let component: ViewJobUploadsComponent;
  let fixture: ComponentFixture<ViewJobUploadsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ViewJobUploadsComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ViewJobUploadsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
