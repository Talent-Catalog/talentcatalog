import {ComponentFixture, TestBed} from '@angular/core/testing';
import {EditMaxEducationLevelComponent} from './edit-max-education-level.component';
import {EducationLevelService} from '../../../../../services/education-level.service';
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {of} from 'rxjs';
import {FormsModule} from '@angular/forms';
import {EducationLevel} from '../../../../../model/education-level';

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
      imports: [FormsModule],
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
    component.currentLevel = dummyLevels[1]; // Bachelor
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load education levels and set selectedLevel based on currentLevel', () => {
    mockEducationLevelService.listEducationLevels.and.returnValue(of(dummyLevels));

    fixture.detectChanges(); // triggers ngOnInit

    expect(component.educationLevels.length).toBe(3);
    expect(component.selectedLevel).toEqual(dummyLevels[1]);
  });

  it('should call activeModal.close with selectedLevel on save()', () => {
    component.selectedLevel = dummyLevels[2];
    component.save();

    expect(mockActiveModal.close).toHaveBeenCalledWith(dummyLevels[2]);
  });
});
