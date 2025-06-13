import {ComponentFixture, TestBed} from '@angular/core/testing';
import {VerifyEmailToastComponent} from './verify-email-toast.component';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {VerifyEmailComponent} from '../verify-email/verify-email.component';
import {NO_ERRORS_SCHEMA} from '@angular/core';

describe('VerifyEmailToastComponent', () => {
  let component: VerifyEmailToastComponent;
  let fixture: ComponentFixture<VerifyEmailToastComponent>;
  let modalServiceSpy: jasmine.SpyObj<NgbModal>;

  beforeEach(async () => {
    const modalSpy = jasmine.createSpyObj('NgbModal', ['open']);

    await TestBed.configureTestingModule({
      declarations: [VerifyEmailToastComponent],
      providers: [
        {provide: NgbModal, useValue: modalSpy}
      ],
      // schemas: [NO_ERRORS_SCHEMA]  // Ignore external components/templates
    }).compileComponents();

    fixture = TestBed.createComponent(VerifyEmailToastComponent);
    component = fixture.componentInstance;
    modalServiceSpy = TestBed.inject(NgbModal) as jasmine.SpyObj<NgbModal>;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should have default showToast as false', () => {
    expect(component.showToast).toBeFalse();
  });

  it('should hide toast when hideToast() is called', () => {
    component.showToast = true;
    component.hideToast();
    expect(component.showToast).toBeFalse();
  });

  it('should open modal and pass userEmail', () => {
    const modalRefMock = {
      componentInstance: {
        userEmail: ''
      }
    };
    modalServiceSpy.open.and.returnValue(modalRefMock as any);

    component.userEmail = 'test@example.com';
    component.openModal();

    expect(modalServiceSpy.open).toHaveBeenCalledWith(VerifyEmailComponent, {centered: true});
    expect(modalRefMock.componentInstance.userEmail).toBe('test@example.com');
  });
});
