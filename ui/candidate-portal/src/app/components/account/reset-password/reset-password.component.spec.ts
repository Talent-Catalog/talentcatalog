import { Component, forwardRef, Input, Pipe, PipeTransform } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { ControlValueAccessor, NG_VALUE_ACCESSOR, ReactiveFormsModule } from '@angular/forms';
import { of } from 'rxjs';

import { ResetPasswordComponent } from './reset-password.component';
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

@Component({selector: 'app-loading', template: ''})
class AppLoadingStubComponent {
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

describe('ResetPasswordComponent', () => {
  let component: ResetPasswordComponent;
  let fixture: ComponentFixture<ResetPasswordComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ReactiveFormsModule],
      declarations: [
        ResetPasswordComponent,
        TranslatePipeStub,
        AppErrorStubComponent,
        AppLoadingStubComponent,
        TcAlertStubComponent,
        TcFieldStubComponent,
        TcLabelStubComponent,
        TcInputStubComponent,
        TcButtonStubComponent
      ],
      providers: [
        {provide: UserService, useValue: {sendResetPassword: () => of(null)}}
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ResetPasswordComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should render one tc field and a disabled submit button while the form is invalid', () => {
    const fields = fixture.debugElement.queryAll(By.directive(TcFieldStubComponent));
    const button = fixture.debugElement.query(By.directive(TcButtonStubComponent)).componentInstance as TcButtonStubComponent;

    expect(fields.length).toBe(1);
    expect(button.disabled).toBeTrue();
  });

  it('should show a success tc-alert after reset email has been sent', () => {
    component.updated = true;
    fixture.detectChanges();

    const alert = fixture.debugElement.query(By.directive(TcAlertStubComponent)).componentInstance as TcAlertStubComponent;
    expect(alert.type).toBe('success');
  });
});
