import {ComponentFixture, TestBed} from '@angular/core/testing';

import {WebcamUploadComponent} from './webcam-upload.component';

describe('WebcamUploadComponent', () => {
  let component: WebcamUploadComponent;
  let fixture: ComponentFixture<WebcamUploadComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ WebcamUploadComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(WebcamUploadComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
