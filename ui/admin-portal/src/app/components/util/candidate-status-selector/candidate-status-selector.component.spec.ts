import {CandidateStatusSelectorComponent} from "./candidate-status-selector.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {FormBuilder, ReactiveFormsModule} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";
import {NgxWigModule} from "ngx-wig";
import {CandidateStatus, UpdateCandidateStatusInfo} from "../../../model/candidate";
import {EnumOption, enumOptions} from "../../../util/enum";

describe('CandidateStatusSelectorComponent', () => {
  let component: CandidateStatusSelectorComponent;
  let fixture: ComponentFixture<CandidateStatusSelectorComponent>;
  let fb: FormBuilder;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        ReactiveFormsModule,
        NgSelectModule,
        NgxWigModule
      ],
      declarations: [CandidateStatusSelectorComponent],
      providers: [FormBuilder]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CandidateStatusSelectorComponent);
    component = fixture.componentInstance;
    fb = TestBed.inject(FormBuilder);

    component.candidateStatus = CandidateStatus.active; // Assume 'active' is a valid status
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should filter out draft option from candidateStatusOptions', () => {
    const options: EnumOption[] = enumOptions(CandidateStatus);
    const filteredOptions = options.filter(option => option.key !== CandidateStatus.draft);
    expect(component.candidateStatusOptions).toEqual(filteredOptions);
  });

  it('should initialize form with candidateStatus, comment, and candidateMessage', () => {
    expect(component.candidateStatusInfoForm.get('status').value).toBe(CandidateStatus.active);
    expect(component.candidateStatusInfoForm.get('comment').value).toBeNull();
    expect(component.candidateStatusInfoForm.get('candidateMessage').value).toBeNull();
  });

  it('should emit statusInfoUpdate event on form value changes', () => {
    spyOn(component.statusInfoUpdate, 'emit');

    const formValue = {
      status: CandidateStatus.autonomousEmployment,
      comment: 'Test comment',
      candidateMessage: 'Test message'
    };

    component.candidateStatusInfoForm.setValue(formValue);
    fixture.detectChanges();

    const expectedEvent: UpdateCandidateStatusInfo = {
      status: formValue.status,
      comment: formValue.comment,
      candidateMessage: formValue.candidateMessage
    };

    expect(component.statusInfoUpdate.emit).toHaveBeenCalledWith(expectedEvent);
  });

  it('should create an event with initial status before form creation', () => {
    component.candidateStatusInfoForm = null;
    spyOn(component.statusInfoUpdate, 'emit');

    component.ngOnChanges({});

    const expectedEvent: UpdateCandidateStatusInfo = {
      status: component.candidateStatus
    };

    expect(component.statusInfoUpdate.emit).toHaveBeenCalledWith(expectedEvent);
  });
});
