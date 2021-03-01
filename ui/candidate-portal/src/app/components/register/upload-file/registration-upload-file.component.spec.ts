import {ComponentFixture, TestBed} from '@angular/core/testing';

import {RegistrationUploadFileComponent} from './registration-upload-file.component';

describe('UploadFileComponent', () => {
  let component: RegistrationUploadFileComponent;
  let fixture: ComponentFixture<RegistrationUploadFileComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ RegistrationUploadFileComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(RegistrationUploadFileComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
