import {ViewCandidateExamComponent} from "./view-candidate-exam.component";
import {CandidateExamService} from "../../../../services/candidate-exam.service";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {NgbActiveModal, NgbModal, NgbModalModule} from "@ng-bootstrap/ng-bootstrap";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {CUSTOM_ELEMENTS_SCHEMA} from "@angular/core";
import {MockCandidate} from "../../../../MockData/MockCandidate";
import {of} from "rxjs";
import {CreateCandidateExamComponent} from "./create/create-candidate-exam.component";
import {EditCandidateExamComponent} from "./edit/edit-candidate-exam.component";
import {ConfirmationComponent} from "../../../util/confirm/confirmation.component";
import {CandidateService} from "../../../../services/candidate.service";

describe('ViewCandidateExamComponent', () => {
  let component: ViewCandidateExamComponent;
  let fixture: ComponentFixture<ViewCandidateExamComponent>;
  let mockCandidateExamService: jasmine.SpyObj<CandidateExamService>;
  let mockModalService: jasmine.SpyObj<NgbModal>;
  let mockCandidateService: jasmine.SpyObj<CandidateService>;

  beforeEach(async () => {
    // Mock CandidateExamService and NgbModal
    mockCandidateExamService = jasmine.createSpyObj('CandidateExamService', ['list', 'delete']);
    mockModalService = jasmine.createSpyObj('NgbModal', ['open']);
    mockCandidateService = jasmine.createSpyObj('CandidateService', ['updateCandidate']);

    await TestBed.configureTestingModule({
      declarations: [ViewCandidateExamComponent],
      imports: [HttpClientTestingModule, NgbModalModule],
      providers: [
        { provide: CandidateExamService, useValue: mockCandidateExamService },
        { provide: NgbModal, useValue: mockModalService },
        NgbActiveModal,
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ViewCandidateExamComponent);
    component = fixture.componentInstance;
    component.candidate = new MockCandidate();
    component.editable = true;
    component.adminUser = true;
    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  describe('createCandidateExam', () => {
    it('should open the create candidate exam modal and update candidate service after returning successfully', async () => {
      const mockModalRef = {
        result: Promise.resolve('some data'),
        componentInstance: {}
      };
      mockModalService.open.and.returnValue(mockModalRef as any);

      spyOn(mockCandidateService, 'updateCandidate'); // Spy on the updateCandidate method

      component.createCandidateExam();

      expect(mockModalService.open).toHaveBeenCalledWith(CreateCandidateExamComponent, {
        centered: true,
        backdrop: 'static',
      });

      await mockModalRef.result; // Wait for the promise to resolve

      expect(mockCandidateService.updateCandidate).toHaveBeenCalled();
    });

    it('should handle modal dismissal', async () => {
      const mockModalRef = {
        result: Promise.reject(),
        componentInstance: {}
      };
      mockModalService.open.and.returnValue(mockModalRef as any);

      spyOn(mockCandidateService, 'updateCandidate'); // Spy on the updateCandidate method

      component.createCandidateExam();

      expect(mockModalService.open).toHaveBeenCalledWith(CreateCandidateExamComponent, {
        centered: true,
        backdrop: 'static',
      });

      await mockModalRef.result.catch(() => {}); // Wait for the promise to reject

      expect(mockCandidateService.updateCandidate).not.toHaveBeenCalled(); // Shouldn't reload on dismissal
    });
  });

  describe('editCandidateExam', () => {
    it('should open the edit candidate exam modal and reload exams after closing', async () => {
      const mockModalRef = {
        result: Promise.resolve(),
        componentInstance: {}
      };
      mockModalService.open.and.returnValue(mockModalRef as any);

      spyOn(mockCandidateService, 'updateCandidate'); // Spy on the updateCandidate method

      component.editCandidateExam(new MockCandidate().candidateExams[0]);

      expect(mockModalService.open).toHaveBeenCalledWith(EditCandidateExamComponent, {
        centered: true,
        backdrop: 'static',
      });

      await mockModalRef.result;

      expect(mockCandidateService.updateCandidate).toHaveBeenCalled();
    });
  });

  describe('deleteCandidateExam', () => {
    it('should open the confirmation modal and delete the exam on confirmation', async () => {
      const mockModalRef = {
        result: Promise.resolve(true),
        componentInstance: {}
      };
      const examToDelete = new MockCandidate().candidateExams[0];
      mockModalService.open.and.returnValue(mockModalRef as any);
      mockCandidateExamService.delete.and.returnValue(of(null));

      spyOn(mockCandidateService, 'updateCandidate'); // Spy on the updateCandidate method

      component.deleteCandidateExam(examToDelete);

      expect(mockModalService.open).toHaveBeenCalledWith(ConfirmationComponent, {
        centered: true,
        backdrop: 'static',
      });

      await mockModalRef.result;

      expect(mockCandidateExamService.delete).toHaveBeenCalledWith(examToDelete.id);
      expect(mockCandidateService.updateCandidate).toHaveBeenCalled();
    });

    it('should not delete the exam if confirmation is canceled', async () => {
      const mockModalRef = {
        result: Promise.resolve(false),
        componentInstance: {}
      };
      mockModalService.open.and.returnValue(mockModalRef as any);

      spyOn(mockCandidateService, 'updateCandidate'); // Spy on the updateCandidate method

      component.deleteCandidateExam(new MockCandidate().candidateExams[0]);

      await mockModalRef.result;

      expect(mockCandidateExamService.delete).not.toHaveBeenCalled();
      expect(mockCandidateService.updateCandidate).not.toHaveBeenCalled();
    });
  });
});
