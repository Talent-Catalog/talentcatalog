import {ComponentFixture, TestBed} from '@angular/core/testing';
import {ReactiveFormsModule, FormBuilder, FormsModule} from '@angular/forms';
import {HostChallengesComponent} from './host-challenges.component';
import {CandidateService} from '../../../../services/candidate.service';
import {AutosaveStatusComponent} from "../../../util/autosave-status/autosave-status.component";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {NgSelectModule} from "@ng-select/ng-select";
import {MockCandidate} from "../../../../MockData/MockCandidate";
fdescribe('HostChallengesComponent', () => {
  let component: HostChallengesComponent;
  let fixture: ComponentFixture<HostChallengesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ HostChallengesComponent,AutosaveStatusComponent ],
      imports: [HttpClientTestingModule,NgSelectModule,FormsModule,ReactiveFormsModule],
      providers: [
        FormBuilder,
        { provide: CandidateService },
      ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(HostChallengesComponent);
    component = fixture.componentInstance;
    component.candidateIntakeData = {
      hostChallenges: 'Test Challenge'
    };
    component.entity = new MockCandidate();
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize the form with candidate data', () => {
    expect(component.form.get('hostChallenges').value).toBe('Test Challenge');
  });

  it('should display the candidate country name', () => {
    const compiled = fixture.nativeElement;
    expect(compiled.querySelector('label').textContent).toContain('United States');
  });

  it('should disable the form control when editable is false', () => {
    component.editable = false;
    component.ngOnInit();
    fixture.detectChanges();
    expect(component.form.get('hostChallenges').disabled).toBeTruthy();
  });
});
