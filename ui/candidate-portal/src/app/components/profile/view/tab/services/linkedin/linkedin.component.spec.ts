import {Component, EventEmitter, forwardRef, Input, Output, Pipe, PipeTransform} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {ControlValueAccessor, FormsModule, NG_VALUE_ACCESSOR} from '@angular/forms';
import {By} from '@angular/platform-browser';
import {Observable, of, throwError} from 'rxjs';
import {CandidateService} from '../../../../../../services/candidate.service';
import {LinkedinService} from '../../../../../../services/linkedin.service';
import {LinkedinComponent} from './linkedin.component';
import {ResourceStatus} from '../../../../../../model/services';

@Pipe({name: 'translate'})
class TranslatePipeStub implements PipeTransform {
  transform(value: string): string {
    return value;
  }
}

@Component({selector: 'app-error', template: ''})
class ErrorStubComponent {
  @Input() error: unknown;
}

@Component({selector: 'tc-loading', template: ''})
class TcLoadingStubComponent {
  @Input() loading = false;
}

@Component({selector: 'tc-alert', template: '<ng-content></ng-content>'})
class TcAlertStubComponent {
  @Input() type?: string;
}

@Component({
  selector: 'tc-input',
  template: '',
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => TcInputStubComponent),
      multi: true,
    },
  ],
})
class TcInputStubComponent implements ControlValueAccessor {
  @Input() placeholder?: string;

  writeValue(): void {}
  registerOnChange(): void {}
  registerOnTouched(): void {}
  setDisabledState(): void {}
}

@Component({
  selector: 'tc-button',
  template: '<button (click)="onClick.emit()"><ng-content></ng-content></button>',
})
class TcButtonStubComponent {
  @Input() disabled = false;
  @Input() size?: string;
  @Input() type?: string;
  @Output() onClick = new EventEmitter<void>();
}

@Component({selector: 'app-linkedin-redeemed', template: ''})
class LinkedinRedeemedStubComponent {
  @Input() assignment: unknown;
  @Input() candidate: unknown;
}

describe('LinkedinComponent', () => {
  let component: LinkedinComponent;
  let fixture: ComponentFixture<LinkedinComponent>;
  let linkedinServiceSpy: jasmine.SpyObj<LinkedinService>;
  let candidateServiceSpy: jasmine.SpyObj<CandidateService>;

  beforeEach(async () => {
    linkedinServiceSpy = jasmine.createSpyObj('LinkedinService', [
      'findAssignmentWithReservedOrRedeemedResource',
      'isOnAssignmentFailureList',
      'assign',
      'updateCouponStatus',
    ]);
    candidateServiceSpy = jasmine.createSpyObj('CandidateService', ['updateCandidateOtherInfo']);

    linkedinServiceSpy.findAssignmentWithReservedOrRedeemedResource.and.returnValue(of(null));
    linkedinServiceSpy.isOnAssignmentFailureList.and.returnValue(of(false));
    linkedinServiceSpy.assign.and.returnValue(of({
      resource: {status: ResourceStatus.RESERVED, resourceCode: 'https://linkedin.example'},
    } as any));
    candidateServiceSpy.updateCandidateOtherInfo.and.returnValue(of(null));

    await TestBed.configureTestingModule({
      declarations: [
        LinkedinComponent,
        TranslatePipeStub,
        ErrorStubComponent,
        TcLoadingStubComponent,
        TcAlertStubComponent,
        TcInputStubComponent,
        TcButtonStubComponent,
        LinkedinRedeemedStubComponent,
      ],
      imports: [FormsModule],
      providers: [
        {provide: LinkedinService, useValue: linkedinServiceSpy},
        {provide: CandidateService, useValue: candidateServiceSpy},
      ],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(LinkedinComponent);
    component = fixture.componentInstance;
    component.candidate = {id: 5, linkedInLink: ''} as any;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should render tc-loading, tc-input, tc-button and invalid-url tc-alert', () => {
    component.linkedInLinkInput = 'not-a-link';
    fixture.detectChanges();

    expect(fixture.debugElement.query(By.directive(TcLoadingStubComponent))).toBeTruthy();
    expect(fixture.debugElement.query(By.directive(TcInputStubComponent))).toBeTruthy();
    expect(fixture.debugElement.query(By.directive(TcAlertStubComponent))).toBeTruthy();
    expect(fixture.debugElement.queryAll(By.directive(TcButtonStubComponent)).length).toBe(2);
  });

  it('should verify and assign when a valid LinkedIn URL is submitted', () => {
    component.linkedInLinkInput = 'https://www.linkedin.com/in/test-user';

    component.verify();

    expect(candidateServiceSpy.updateCandidateOtherInfo).toHaveBeenCalledWith({
      linkedInLink: 'https://www.linkedin.com/in/test-user',
    });
    expect(linkedinServiceSpy.assign).toHaveBeenCalledWith(5);
    expect(component.assignment?.resource.status).toBe(ResourceStatus.RESERVED);
  });

  it('should set error when verification fails', () => {
    linkedinServiceSpy.assign.and.returnValue(new Observable(subscriber => subscriber.error('linkedin-error')));
    component.linkedInLinkInput = 'https://www.linkedin.com/in/test-user';

    component.verify();

    expect(component.error).toBe('linkedin-error');
    expect(component.loading).toBeFalse();
  });
});
