import {ComponentFixture, fakeAsync, TestBed, tick, waitForAsync} from '@angular/core/testing';
import { UserChangePasswordComponent } from './user-change-password.component';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { UserService } from '../../../services/user.service';

fdescribe('UserChangePasswordComponent', () => {
  let component: UserChangePasswordComponent;
  let fixture: ComponentFixture<UserChangePasswordComponent>;
  let mockUserService: jasmine.SpyObj<UserService>;
  let mockActivatedRoute: any;
  let mockRouter: any;

  beforeEach(waitForAsync(() => {
    mockUserService = jasmine.createSpyObj('UserService', ['checkPasswordResetToken', 'resetPassword']);

    mockActivatedRoute = {
      paramMap: of({ get: () => 'mockToken' })
    };
    mockRouter = jasmine.createSpyObj('Router', ['navigate']);
     // Mock implementation for checkPasswordResetToken
    mockUserService.checkPasswordResetToken.and.returnValue(of({ success: true }));
// Mock implementation for resetPassword
    mockUserService.resetPassword.and.returnValue(of({ success: true }));
    TestBed.configureTestingModule({
      declarations: [UserChangePasswordComponent],
      imports: [ReactiveFormsModule],
      providers: [
        FormBuilder,
        { provide: UserService, useValue: mockUserService },
        { provide: ActivatedRoute, useValue: mockActivatedRoute },
        { provide: Router, useValue: mockRouter }
      ]
    })
    .compileComponents();

  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(UserChangePasswordComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form based on token', () => {
    expect(component.changePasswordForm).toBeDefined();
    expect(component.token).toBe('mockToken');
  });
  //
  it('should initialize form without token', () => {
    mockActivatedRoute.paramMap = of({ get: () => null });
    fixture = TestBed.createComponent(UserChangePasswordComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    expect(component.changePasswordForm).toBeDefined();
    expect(component.token).toBeNull();
  });

  it('should update password successfully', fakeAsync(() => {
    component.reset = true;
    component.changePasswordForm.setValue({
      token: 'mockToken',
      password: 'newPassword',
      passwordConfirmation: 'newPassword'
    });
    mockUserService.resetPassword.and.returnValue(of(null));
    component.updatePassword();
    tick(2100);
     expect(mockUserService.resetPassword).toHaveBeenCalledWith({
      token: 'mockToken',
      password: 'newPassword',
      passwordConfirmation: 'newPassword'
    });
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/login']);
    expect(component.updated).toBeTruthy();
  }));

  it('should handle error during password update', () => {
    component.reset = true;
    component.changePasswordForm.setValue({
      token: 'mockToken',
      password: 'newPassword',
      passwordConfirmation: 'newPassword'
    });
    const errorMessage = 'Password update failed';
    mockUserService.resetPassword.and.returnValue(throwError(errorMessage));
    component.updatePassword();
    expect(mockUserService.resetPassword).toHaveBeenCalledWith({
      token: 'mockToken',
      password: 'newPassword',
      passwordConfirmation: 'newPassword'
    });
    expect(component.error).toBe(errorMessage);
  });
  //
  it('should reset form', () => {
    component.reset = true;
    component.changePasswordForm.setValue({
      token: 'mockToken',
      password: 'newPassword',
      passwordConfirmation: 'newPassword'
    });
    component.resetForm();
    expect(component.changePasswordForm.value).toEqual({
      token: '',
      password: '',
      passwordConfirmation: ''
    });
    expect(component.updated).toBeFalsy();
    expect(component.error).toBeNull();
  });

  afterAll(()=>{
    fixture.destroy();
  })
});
