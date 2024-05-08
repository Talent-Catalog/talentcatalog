import {ComponentFixture, fakeAsync, TestBed, tick} from '@angular/core/testing';
 import {NgbActiveModal, NgbModal, NgbModalRef} from '@ng-bootstrap/ng-bootstrap';
import { Job } from '../../../../../model/job';
import { User } from '../../../../../model/user';
import { SavedList } from '../../../../../model/saved-list';
import {CountryService} from "../../../../../services/country.service";
import {JobService} from "../../../../../services/job.service";
import {Directive, Input} from "@angular/core";
import {EditJobInfoComponent} from "../edit-job-info/edit-job-info.component";
import {Observable} from "rxjs";
import {FormBuilder} from "@angular/forms";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {LocalStorageModule} from "angular-2-local-storage";
import {MockJob} from "../../../../../MockData/MockJob";

@Directive({
  selector: '[routerLink]'
})
export class RouterLinkDirectiveStub {
  @Input('routerLink') linkParams: any;
}
fdescribe('EditJobInfoComponent', () => {
  let component: EditJobInfoComponent;
  let fixture: ComponentFixture<EditJobInfoComponent>;
  let modalService: NgbModal;
  let ngbActiveModal: NgbActiveModal;
  let fb: FormBuilder;
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ EditJobInfoComponent,RouterLinkDirectiveStub ],
      imports:[HttpClientTestingModule,
        LocalStorageModule.forRoot({}),
      ],
      providers: [
        { provide: NgbModal  },
        { provide: NgbActiveModal  },
        { provide: FormBuilder  },
      ]
    })
    .compileComponents();

    modalService = TestBed.inject(NgbModal);
    ngbActiveModal = TestBed.inject(NgbActiveModal);
    fb = TestBed.inject(FormBuilder);
  });

  beforeEach(() => { // Use this block for component creation
    fixture = TestBed.createComponent(EditJobInfoComponent);
    component = fixture.componentInstance;

     // Provide a mock Job object
    component.job = MockJob;

    fixture.detectChanges();
  });
  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize jobForm with correct values', () => {
    component.jobForm = new FormBuilder().group(MockJob);
    // Access the form controls
    const submissionDueDateControl = component.jobForm.get('submissionDueDate');
    const contactUserControl = component.jobForm.get('contactUser');
    expect(submissionDueDateControl.value.toDateString()).toEqual(component.job.submissionDueDate.toDateString());
    expect(contactUserControl.value).toEqual(component.job.contactUser);

   });
});
