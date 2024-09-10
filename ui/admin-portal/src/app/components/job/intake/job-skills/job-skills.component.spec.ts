import {JobSkillsComponent} from "./job-skills.component";
import {ComponentFixture, fakeAsync, TestBed, tick} from "@angular/core/testing";
import {ReactiveFormsModule} from "@angular/forms";
import {NgxWigModule} from "ngx-wig";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {AutosaveStatusComponent} from "../../../util/autosave-status/autosave-status.component";
describe('JobSkillsComponent', () => {
  let component: JobSkillsComponent;
  let fixture: ComponentFixture<JobSkillsComponent>;
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ JobSkillsComponent,AutosaveStatusComponent ],
      imports: [ ReactiveFormsModule,NgxWigModule,HttpClientTestingModule ],
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(JobSkillsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form control with correct initial value and disabled state', () => {
    // Access the form control
    const skillRequirementsControl = component.form.get('skillRequirements');

    // Check if the form control is initialized
    expect(skillRequirementsControl).toBeTruthy();

    // Check if the initial value is set correctly
    expect(skillRequirementsControl.value).toEqual(component.jobIntakeData?.skillRequirements);

    // Check if the disabled state is set correctly
    expect(skillRequirementsControl.disabled).toBe(!component.editable);
  });

  it('should make form control valid if editable', () => {
    component.editable = true;
    fixture.detectChanges();

    expect(component.form.get('skillRequirements').valid).toBe(true);
  });

  it('should update form control value on input change', fakeAsync(() => {
    const initialSkillRequirement = 'Skill requirements';
    component.editable = true;
    component.jobIntakeData = { skillRequirements: initialSkillRequirement };
    component.ngOnInit();

    expect(component.form.get('skillRequirements').value).toEqual(initialSkillRequirement);

    // Simulate user input
    const newSkillRequirements = 'Updated skill requirements';
    component.editable = true; // Enable editing
    fixture.detectChanges();


    const skillRequirementsDiv = fixture.nativeElement.querySelector('.nw-editor__res');
    skillRequirementsDiv.innerText = newSkillRequirements;
    skillRequirementsDiv.dispatchEvent(new Event('input'));

    tick(); // Wait for asynchronous operations to complete

    // Check if the form control value is updated correctly
    expect(component.form.get('skillRequirements').value).toEqual(newSkillRequirements);
  }));

});

