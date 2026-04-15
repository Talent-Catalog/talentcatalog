import { Component, forwardRef, Input, Pipe, PipeTransform } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { ControlValueAccessor, NG_VALUE_ACCESSOR, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { of } from 'rxjs';

import { LoginComponent } from './login.component';
import { AuthenticationService } from '../../../services/authentication.service';
import { CandidateService } from '../../../services/candidate.service';
import { TermsInfoService } from '../../../services/terms-info.service';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';

@Pipe({name: 'translate'})
class TranslatePipeStub implements PipeTransform {
  transform(value: string): string {
    return value;
  }
}

@Component({selector: 'app-error', template: ''})
class AppErrorStubComponent {
  @Input() error: any;
}

@Component({selector: 'tc-field', template: '<ng-content></ng-content>'})
class TcFieldStubComponent {}

@Component({selector: 'tc-label', template: '<ng-content></ng-content>'})
class TcLabelStubComponent {
  @Input() for: string;
}

@Component({
  selector: 'tc-input',
  template: '',
  providers: [{
    provide: NG_VALUE_ACCESSOR,
    useExisting: forwardRef(() => TcInputStubComponent),
    multi: true
  }]
})
class TcInputStubComponent implements ControlValueAccessor {
  @Input() id: string;
  @Input() type: string;
  @Input() invalid: boolean;

  writeValue(): void {}
  registerOnChange(): void {}
  registerOnTouched(): void {}
  setDisabledState(): void {}
}

@Component({selector: 'tc-button', template: '<ng-content></ng-content>'})
class TcButtonStubComponent {
  @Input() disabled: boolean;
}

describe('LoginComponent', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ReactiveFormsModule, RouterTestingModule],
      declarations: [
        LoginComponent,
        TranslatePipeStub,
        AppErrorStubComponent,
        TcFieldStubComponent,
        TcLabelStubComponent,
        TcInputStubComponent,
        TcButtonStubComponent
      ],
      providers: [
        {
          provide: AuthenticationService,
          useValue: {
            login: () => of(null),
            setCandidateStatus: () => {}
          }
        },
        {
          provide: CandidateService,
          useValue: {
            getCandidatePersonal: () => of({candidateNumber: '123', status: 'ACTIVE', acceptedPrivacyPolicyId: '1', changePassword: false}),
            setCandNumberStorage: () => {}
          }
        },
        {
          provide: ActivatedRoute,
          useValue: {
            queryParams: of({}),
            snapshot: {queryParams: {}}
          }
        },
        {provide: TermsInfoService, useValue: {getCurrentByType: () => of({content: '', id: '1'})}},
        {provide: NgbModal, useValue: {open: () => {}}}
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    spyOn(TestBed.inject(Router), 'navigateByUrl').and.returnValue(Promise.resolve(true));
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should render the tc form fields and disable submit while the form is invalid', () => {
    const fields = fixture.debugElement.queryAll(By.directive(TcFieldStubComponent));
    const button = fixture.debugElement.query(By.directive(TcButtonStubComponent)).componentInstance as TcButtonStubComponent;

    expect(fields.length).toBe(2);
    expect(button.disabled).toBeTrue();
  });

  it('should enable submit once both login fields are populated', () => {
    component.loginForm.patchValue({
      username: 'candidate@example.com',
      password: 'password123'
    });
    fixture.detectChanges();

    const button = fixture.debugElement.query(By.directive(TcButtonStubComponent)).componentInstance as TcButtonStubComponent;
    const inputs = fixture.debugElement.queryAll(By.directive(TcInputStubComponent))
      .map(debugEl => debugEl.componentInstance as TcInputStubComponent);

    expect(button.disabled).toBeFalse();
    expect(inputs[0].invalid).toBeFalse();
    expect(inputs[1].invalid).toBeFalse();
  });
});
