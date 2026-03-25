import {Component, EventEmitter, Input, Output, Pipe, PipeTransform} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {By} from '@angular/platform-browser';
import {of, throwError} from 'rxjs';
import {LinkedinService} from '../../../../../../../services/linkedin.service';
import {LinkedinRedeemedComponent} from './linkedin-redeemed.component';

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

@Component({
  selector: 'tc-button',
  template: '<button (click)="onClick.emit()"><ng-content></ng-content></button>',
})
class TcButtonStubComponent {
  @Input() color?: string;
  @Output() onClick = new EventEmitter<void>();
}

describe('LinkedinRedeemedComponent', () => {
  let component: LinkedinRedeemedComponent;
  let fixture: ComponentFixture<LinkedinRedeemedComponent>;
  let linkedinServiceSpy: jasmine.SpyObj<LinkedinService>;

  beforeEach(async () => {
    linkedinServiceSpy = jasmine.createSpyObj('LinkedinService', [
      'isOnIssueReportList',
      'addCandidateToIssueReportList',
    ]);
    linkedinServiceSpy.isOnIssueReportList.and.returnValue(of(false));
    linkedinServiceSpy.addCandidateToIssueReportList.and.returnValue(of(void 0));

    await TestBed.configureTestingModule({
      declarations: [
        LinkedinRedeemedComponent,
        TranslatePipeStub,
        ErrorStubComponent,
        TcLoadingStubComponent,
        TcButtonStubComponent,
      ],
      providers: [
        {provide: LinkedinService, useValue: linkedinServiceSpy},
      ],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(LinkedinRedeemedComponent);
    component = fixture.componentInstance;
    component.candidate = {id: 5} as any;
    component.assignment = {
      resource: {resourceCode: 'https://linkedin.example'},
    } as any;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should render tc-loading and the report-issue tc-button', () => {
    expect(fixture.debugElement.query(By.directive(TcLoadingStubComponent))).toBeTruthy();

    const button = fixture.debugElement.query(By.directive(TcButtonStubComponent));
    expect(button).toBeTruthy();
    expect((button.componentInstance as TcButtonStubComponent).color).toBe('error');
  });

  it('should report an issue and refresh the issue list state', () => {
    component.reportIssue();

    expect(linkedinServiceSpy.addCandidateToIssueReportList).toHaveBeenCalledWith(component.assignment);
    expect(linkedinServiceSpy.isOnIssueReportList).toHaveBeenCalledWith(5);
  });

  it('should set error when reporting an issue fails', () => {
    linkedinServiceSpy.addCandidateToIssueReportList.and.returnValue(throwError('report-error'));

    component.reportIssue();

    expect(component.error).toBe('report-error');
    expect(component.loading).toBeFalse();
  });
});
