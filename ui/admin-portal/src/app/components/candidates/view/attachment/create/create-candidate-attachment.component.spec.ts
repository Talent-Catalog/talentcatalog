import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {CreateCandidateAttachmentComponent} from './create-candidate-attachment.component';

describe('CreateCandidateAttachmentComponent', () => {
  let component: CreateCandidateAttachmentComponent;
  let fixture: ComponentFixture<CreateCandidateAttachmentComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CreateCandidateAttachmentComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CreateCandidateAttachmentComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
