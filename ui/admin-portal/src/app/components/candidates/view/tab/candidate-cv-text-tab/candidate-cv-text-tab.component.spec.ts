// candidate-cv-text-tab.component.spec.ts
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {NO_ERRORS_SCHEMA, SimpleChange} from '@angular/core';
import {of, throwError} from 'rxjs';

import {CandidateCvTextTabComponent} from './candidate-cv-text-tab.component';
import {CandidateAttachmentService} from '../../../../../services/candidate-attachment.service';

describe('CandidateCvTextTabComponent', () => {
  let component: CandidateCvTextTabComponent;
  let fixture: ComponentFixture<CandidateCvTextTabComponent>;
  let serviceSpy: jasmine.SpyObj<CandidateAttachmentService>;

  beforeEach(async () => {
    serviceSpy = jasmine.createSpyObj<CandidateAttachmentService>('CandidateAttachmentService', [
      'getCandidateCvText',
    ]);

    await TestBed.configureTestingModule({
      declarations: [CandidateCvTextTabComponent],
      providers: [{ provide: CandidateAttachmentService, useValue: serviceSpy }],
      schemas: [NO_ERRORS_SCHEMA], // ignore the external template
    }).compileComponents();

    fixture = TestBed.createComponent(CandidateCvTextTabComponent);
    component = fixture.componentInstance;
  });

  function triggerChanges(prevCandidate: any, nextCandidate: any) {
    component.candidate = nextCandidate;
    component.ngOnChanges({
      candidate: new SimpleChange(prevCandidate, nextCandidate, prevCandidate == null),
    });
  }

  it('calls service with candidate id and concatenates CvText on success', () => {
    const candidate = { id: 123 } as any;
    const payload = [{id: 1, text: 'first' }, {id:2, text: 'second' }]; // CvText[]
    serviceSpy.getCandidateCvText.and.returnValue(of(payload));

    triggerChanges(null, candidate);

    expect(serviceSpy.getCandidateCvText).toHaveBeenCalledWith(123);
    expect(component.error).toBeNull();
    expect(component.cvText).toBe('first||\nsecond');
  });

  it('sets cvText to empty string when service returns empty array', () => {
    const candidate = { id: 5 } as any;
    serviceSpy.getCandidateCvText.and.returnValue(of([]));

    triggerChanges(null, candidate);

    expect(serviceSpy.getCandidateCvText).toHaveBeenCalledWith(5);
    expect(component.error).toBeNull();
    expect(component.cvText).toBe(''); // join of empty array ⇒ ""
  });

  it('overwrites previous error to null on new change before calling service', () => {
    component.error = 'previous error';
    const candidate = { id: 7 } as any;
    serviceSpy.getCandidateCvText.and.returnValue(of([{id:1, text: 'ok' }]));

    triggerChanges(null, candidate);

    expect(component.error).toBeNull();
    expect(component.cvText).toBe('ok');
  });

  it('sets error when service errors', () => {
    const candidate = { id: 42 } as any;
    serviceSpy.getCandidateCvText.and.returnValue(throwError('boom'));

    triggerChanges(null, candidate);

    expect(serviceSpy.getCandidateCvText).toHaveBeenCalledWith(42);
    expect(component.error).toBe('boom');
    expect(component.cvText).toBeUndefined();
  });

  it('still calls service when candidate is undefined (passes undefined id)', () => {
    serviceSpy.getCandidateCvText.and.returnValue(of([{id: 1, text: 'x' }]));

    triggerChanges(null, undefined);

    expect(serviceSpy.getCandidateCvText).toHaveBeenCalledWith(undefined);
    expect(component.cvText).toBe('x');
  });
});
