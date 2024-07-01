import {DownloadCvComponent} from "./download-cv.component";
import {CandidateService, DownloadCVRequest} from "../../../services/candidate.service";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {FormBuilder, ReactiveFormsModule} from "@angular/forms";
import {of, throwError} from "rxjs";

fdescribe('DownloadCvComponent', () => {
  let component: DownloadCvComponent;
  let fixture: ComponentFixture<DownloadCvComponent>;
  let candidateServiceSpy: jasmine.SpyObj<CandidateService>;
  let activeModalSpy: jasmine.SpyObj<NgbActiveModal>;

  beforeEach(async () => {
    const candidateService = jasmine.createSpyObj('CandidateService', ['downloadCv']);
    const activeModal = jasmine.createSpyObj('NgbActiveModal', ['close', 'dismiss']);

    await TestBed.configureTestingModule({
      declarations: [DownloadCvComponent],
      imports: [ReactiveFormsModule],
      providers: [
        FormBuilder,
        { provide: CandidateService, useValue: candidateService },
        { provide: NgbActiveModal, useValue: activeModal }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(DownloadCvComponent);
    component = fixture.componentInstance;
    candidateServiceSpy = TestBed.inject(CandidateService) as jasmine.SpyObj<CandidateService>;
    activeModalSpy = TestBed.inject(NgbActiveModal) as jasmine.SpyObj<NgbActiveModal>;

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form with default values', () => {
    expect(component.form).toBeDefined();
    expect(component.form.value).toEqual({ name: false, contact: false });
  });

  it('should call downloadCv and close modal on success', () => {
    const request: DownloadCVRequest = {
      candidateId: component.candidateId,
      showName: false,
      showContact: false
    };
    const fakeBlob = new Blob();
    const tabSpy = { location: { href: '' } };
    spyOn(window, 'open').and.returnValue(tabSpy as any);

    candidateServiceSpy.downloadCv.and.returnValue(of(fakeBlob));
    component.onSave();

    expect(candidateServiceSpy.downloadCv).toHaveBeenCalledWith(request);
    expect(activeModalSpy.close).toHaveBeenCalled();
  });

  it('should set error message on failure', () => {
    const errorResponse = 'Error downloading CV';
    candidateServiceSpy.downloadCv.and.returnValue(throwError(errorResponse));
    component.onSave();
    expect(component.error).toBe(errorResponse);
  });

  it('should close the modal', () => {
    component.closeModal();
    expect(activeModalSpy.close).toHaveBeenCalled();
  });

  it('should dismiss the modal', () => {
    component.dismiss();
    expect(activeModalSpy.dismiss).toHaveBeenCalledWith(false);
  });

  it('should disable save button if form is invalid or loading or saving', () => {
    component.loading = true;
    fixture.detectChanges();
    let saveButton = fixture.debugElement.nativeElement.querySelector('.modal-footer .btn-primary');
    expect(saveButton.disabled).toBeTruthy();

    component.loading = false;
    component.saving = true;
    fixture.detectChanges();
    saveButton = fixture.debugElement.nativeElement.querySelector('.modal-footer .btn-primary');
    expect(saveButton.disabled).toBeTruthy();

    component.form.get('name').setValue(null);
    fixture.detectChanges();
    saveButton = fixture.debugElement.nativeElement.querySelector('.modal-footer .btn-primary');
    expect(saveButton.disabled).toBeTruthy();
  });

  it('should show error message if error is present', () => {
    component.error = 'Test error message';
    fixture.detectChanges();
    const errorMessage = fixture.debugElement.nativeElement.querySelector('.alert-danger');
    expect(errorMessage).toBeTruthy();
    expect(errorMessage.textContent).toContain('Test error message');
  });

  it('should call onSave when save button is clicked', () => {
    spyOn(component, 'onSave');
    component.form.setValue({ name: true, contact: true });
    component.loading = false;
    component.saving = false;
    fixture.detectChanges();

    const saveButton = fixture.debugElement.nativeElement.querySelector('.modal-footer .btn-primary');
    saveButton.click();
    expect(component.onSave).toHaveBeenCalled();
  });

  it('should call dismiss when cancel button is clicked', () => {
    spyOn(component, 'dismiss');
    const cancelButton = fixture.debugElement.nativeElement.querySelector('.modal-footer .btn-primary:nth-child(2)');
    cancelButton.click();
    expect(component.dismiss).toHaveBeenCalled();
  });

});
