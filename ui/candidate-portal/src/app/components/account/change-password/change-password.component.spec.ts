import { Component, forwardRef, Input, Pipe, PipeTransform } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { ControlValueAccessor, NG_VALUE_ACCESSOR, ReactiveFormsModule, UntypedFormBuilder } from '@angular/forms';
import { ActivatedRoute, Router, convertToParamMap } from '@angular/router';
import { of } from 'rxjs';

import { ChangePasswordComponent } from './change-password.component';
import { UserService } from '../../../services/user.service';

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

@Component({selector: 'tc-loading', template: ''})
class TcLoadingStubComponent {
  @Input() loading: boolean;
}

@Component({selector: 'tc-alert', template: '<ng-content></ng-content>'})
class TcAlertStubComponent {
  @Input() type: string;
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

  writeValue(): void {}
  registerOnChange(): void {}
  registerOnTouched(): void {}
  setDisabledState(): void {}
}

@Component({selector: 'tc-button', template: '<ng-content></ng-content>'})
class TcButtonStubComponent {
  @Input() disabled: boolean;
}

describe('ChangePasswordComponent', () => {
  let component: ChangePasswordComponent;
  let fixture: ComponentFixture<ChangePasswordComponent>;
  let formBuilder: UntypedFormBuilder;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ReactiveFormsModule],
      declarations: [
        ChangePasswordComponent,
        TranslatePipeStub,
        AppErrorStubComponent,
        TcLoadingStubComponent,
        TcAlertStubComponent,
        TcFieldStubComponent,
        TcLabelStubComponent,
        TcInputStubComponent,
        TcButtonStubComponent
      ],
      providers: [
        {
          provide: UserService,
          useValue: {
            checkPasswordResetToken: () => of(null),
            resetPassword: () => of(null),
            updatePassword: () => of(null)
          }
        },
        {provide: Router, useValue: {navigate: () => Promise.resolve(true)}},
        {provide: ActivatedRoute, useValue: {paramMap: of(convertToParamMap({}))}}
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ChangePasswordComponent);
    component = fixture.componentInstance;
    formBuilder = TestBed.inject(UntypedFormBuilder);
    fixture.detectChanges();
  });

  it('should render tc loading, three fields, and a disabled submit button in change-password mode', () => {
    const loading = fixture.debugElement.query(By.directive(TcLoadingStubComponent)).componentInstance as TcLoadingStubComponent;
    const fields = fixture.debugElement.queryAll(By.directive(TcFieldStubComponent));
    const button = fixture.debugElement.query(By.directive(TcButtonStubComponent)).componentInstance as TcButtonStubComponent;

    expect(loading.loading).toBeFalse();
    expect(fields.length).toBe(3);
    expect(button.disabled).toBeTrue();
  });

  it('should hide the old password field in reset mode', () => {
    component.reset = true;
    component.form = formBuilder.group({
      token: ['token'],
      password: [''],
      passwordConfirmation: ['']
    });
    fixture.detectChanges();

    const inputs = fixture.debugElement.queryAll(By.directive(TcInputStubComponent))
      .map(debugEl => debugEl.componentInstance as TcInputStubComponent);

    expect(inputs.some(input => input.id === 'oldPassword')).toBeFalse();
    expect(inputs.length).toBe(2);
  });

  it('should show a success tc-alert after the password is updated', () => {
    component.updated = true;
    fixture.detectChanges();

    const alert = fixture.debugElement.query(By.directive(TcAlertStubComponent)).componentInstance as TcAlertStubComponent;
    expect(alert.type).toBe('success');
  });
});
