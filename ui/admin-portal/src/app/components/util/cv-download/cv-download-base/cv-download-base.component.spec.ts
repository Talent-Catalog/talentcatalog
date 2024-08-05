import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CvDownloadBaseComponent } from './cv-download-base.component';

describe('CvDownloadBaseComponent', () => {
  let component: CvDownloadBaseComponent;
  let fixture: ComponentFixture<CvDownloadBaseComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CvDownloadBaseComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CvDownloadBaseComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
