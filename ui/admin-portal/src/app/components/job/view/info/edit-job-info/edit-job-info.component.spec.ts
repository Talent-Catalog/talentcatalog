import {ComponentFixture, TestBed} from '@angular/core/testing';
import {NgbActiveModal, NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {EditJobInfoComponent} from "../edit-job-info/edit-job-info.component";
import {UntypedFormBuilder} from "@angular/forms";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {LocalStorageModule} from "angular-2-local-storage";
import {MockJob} from "../../../../../MockData/MockJob";
import {RouterLinkStubDirective} from "../../../../login/login.component.spec";
describe('EditJobInfoComponent', () => {
  let component: EditJobInfoComponent;
  let fixture: ComponentFixture<EditJobInfoComponent>;
  let modalService: NgbModal;
  let ngbActiveModal: NgbActiveModal;
  let fb: UntypedFormBuilder;
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ EditJobInfoComponent,RouterLinkStubDirective ],
      imports:[HttpClientTestingModule,
        LocalStorageModule.forRoot({}),
      ],
      providers: [
        { provide: NgbModal  },
        { provide: NgbActiveModal  },
        { provide: UntypedFormBuilder  },
      ]
    })
    .compileComponents();

    modalService = TestBed.inject(NgbModal);
    ngbActiveModal = TestBed.inject(NgbActiveModal);
    fb = TestBed.inject(UntypedFormBuilder);
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
    component.jobForm = new UntypedFormBuilder().group(MockJob);
    // Access the form controls
    const submissionDueDateControl = component.jobForm.get('submissionDueDate');
    const contactUserControl = component.jobForm.get('contactUser');
    expect(submissionDueDateControl.value.toDateString()).toEqual(component.job.submissionDueDate.toDateString());
    expect(contactUserControl.value).toEqual(component.job.contactUser);

   });
});
