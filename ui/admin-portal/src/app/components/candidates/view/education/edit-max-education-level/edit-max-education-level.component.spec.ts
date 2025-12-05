import {ComponentFixture, TestBed} from '@angular/core/testing';
import {EditMaxEducationLevelComponent} from './edit-max-education-level.component';
import {EducationLevelService} from '../../../../../services/education-level.service';
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {FormControl, FormGroup, ReactiveFormsModule} from '@angular/forms';
import {EducationLevel} from '../../../../../model/education-level';
import {of} from "rxjs";
import {NgSelectModule} from "@ng-select/ng-select";

describe('EditMaxEducationLevelComponent', () => {
  let component: EditMaxEducationLevelComponent;
  let fixture: ComponentFixture<EditMaxEducationLevelComponent>;
  let mockEducationLevelService: jasmine.SpyObj<EducationLevelService>;
  let mockActiveModal: jasmine.SpyObj<NgbActiveModal>;

  const dummyLevels: EducationLevel[] = [
    { id: 1, name: 'High School', status: 'ACTIVE', level: 1 },
    { id: 2, name: 'Bachelor', status: 'ACTIVE', level: 2 },
    { id: 3, name: 'Master', status: 'INACTIVE', level: 3 },
  ];

  beforeEach(async () => {
    mockEducationLevelService = jasmine.createSpyObj('EducationLevelService', ['listEducationLevels']);
    mockActiveModal = jasmine.createSpyObj('NgbActiveModal', ['close', 'dismiss']);

    await TestBed.configureTestingModule({
      imports: [ReactiveFormsModule, NgSelectModule],
      declarations: [EditMaxEducationLevelComponent],
      providers: [
        { provide: EducationLevelService, useValue: mockEducationLevelService },
        { provide: NgbActiveModal, useValue: mockActiveModal }
      ]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(EditMaxEducationLevelComponent);
    component = fixture.componentInstance;
    component.educationLevel = dummyLevels[0];

    // initialize the form
    component.form = new FormGroup({
      educationLevelId: new FormControl(null)
    });

    mockEducationLevelService.listEducationLevels.and.returnValue(of(dummyLevels));
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should set ngSelect value based on educationLevel', () => {

    fixture.detectChanges(); // triggers ngOnInit

    expect(component.form.get('educationLevelId')!.value).toBe(dummyLevels[0].id);
  });

  it('should call activeModal.close with the newly selected education level on save', () => {
    const selectedLevel = dummyLevels[2]; // Master
    component.form.get('educationLevelId')!.setValue(selectedLevel.id);

    component.save();

    expect(mockActiveModal.close).toHaveBeenCalledWith(selectedLevel.id);
  });
});
