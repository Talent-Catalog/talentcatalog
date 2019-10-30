import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {EditCandidateAttachmentComponent} from './edit-candidate-attachment.component';

describe('EditCandidateAttachmentComponent', () => {
  let component: EditCandidateAttachmentComponent;
  let fixture: ComponentFixture<EditCandidateAttachmentComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ EditCandidateAttachmentComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(EditCandidateAttachmentComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
