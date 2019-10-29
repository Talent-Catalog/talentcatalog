import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {CandidateAttachmentsComponent} from './candidate-attachments.component';

describe('FileUploadsComponent', () => {
  let component: CandidateAttachmentsComponent;
  let fixture: ComponentFixture<CandidateAttachmentsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CandidateAttachmentsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CandidateAttachmentsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
