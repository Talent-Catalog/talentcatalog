import {ViewCandidateExamComponent} from "./view-candidate-exam.component";
import {CandidateExamService} from "../../../../services/candidate-exam.service";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {NgbActiveModal, NgbModal, NgbModalModule} from "@ng-bootstrap/ng-bootstrap";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {CUSTOM_ELEMENTS_SCHEMA} from "@angular/core";
import {MockCandidate} from "../../../../MockData/MockCandidate";
import {of, throwError} from "rxjs";
import {CreateCandidateExamComponent} from "./create/create-candidate-exam.component";
import {EditCandidateExamComponent} from "./edit/edit-candidate-exam.component";
import {ConfirmationComponent} from "../../../util/confirm/confirmation.component";

describe('ViewCandidateExamComponent', () => {
  let component: ViewCandidateExamComponent;
  let fixture: ComponentFixture<ViewCandidateExamComponent>;
  let mockCandidateExamService: jasmine.SpyObj<CandidateExamService>;
  let mockModalService: jasmine.SpyObj<NgbModal>;

  beforeEach(async () => {
    // Mock CandidateExamService and NgbModal
    mockCandidateExamService = jasmine.createSpyObj('CandidateExamService', ['list', 'delete']);
    mockModalService = jasmine.createSpyObj('NgbModal', ['open']);

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

  describe('doSearch', () => {
    it('should load candidate exams successfully', () => {
      const mockExams = new MockCandidate().candidateExams;
      mockCandidateExamService.list.and.returnValue(of(mockExams));

      component.doSearch();

      expect(component.loading).toBeFalse();
      expect(component.candidateExams).toEqual(mockExams);
      expect(mockCandidateExamService.list).toHaveBeenCalledWith(component.candidate.id);
    });

    it('should handle error when loading exams fails', () => {
      const error = 'Error loading exams';
      mockCandidateExamService.list.and.returnValue(throwError(error));

      component.doSearch();

      expect(component.loading).toBeFalse();
      expect(component.error).toBe(error);
      expect(mockCandidateExamService.list).toHaveBeenCalledWith(component.candidate.id);
    });
  });

  describe('createCandidateExam', () => {
    it('should open the create candidate exam modal and reload exams after closing', async () => {
      const mockModalRef = {
        result: Promise.resolve('some data'),
        componentInstance: {}
      };
      mockModalService.open.and.returnValue(mockModalRef as any);

      spyOn(component, 'doSearch'); // Spy on the doSearch method

      component.createCandidateExam();

      expect(mockModalService.open).toHaveBeenCalledWith(CreateCandidateExamComponent, {
        centered: true,
        backdrop: 'static',
      });

      await mockModalRef.result; // Wait for the promise to resolve

      expect(component.doSearch).toHaveBeenCalled();
    });

    it('should handle modal dismissal', async () => {
      const mockModalRef = {
        result: Promise.reject(),
        componentInstance: {}
      };
      mockModalService.open.and.returnValue(mockModalRef as any);

      spyOn(component, 'doSearch');

      component.createCandidateExam();

      expect(mockModalService.open).toHaveBeenCalledWith(CreateCandidateExamComponent, {
        centered: true,
        backdrop: 'static',
      });

      await mockModalRef.result.catch(() => {}); // Wait for the promise to reject

      expect(component.doSearch).not.toHaveBeenCalled(); // Shouldn't reload on dismissal
    });
  });

  describe('editCandidateExam', () => {
    it('should open the edit candidate exam modal and reload exams after closing', async () => {
      const mockModalRef = {
        result: Promise.resolve(),
        componentInstance: {}
      };
      mockModalService.open.and.returnValue(mockModalRef as any);

      spyOn(component, 'doSearch');

      component.editCandidateExam(new MockCandidate().candidateExams[0]);

      expect(mockModalService.open).toHaveBeenCalledWith(EditCandidateExamComponent, {
        centered: true,
        backdrop: 'static',
      });

      await mockModalRef.result;

      expect(component.doSearch).toHaveBeenCalled();
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

      spyOn(component, 'doSearch');

      component.deleteCandidateExam(examToDelete);

      expect(mockModalService.open).toHaveBeenCalledWith(ConfirmationComponent, {
        centered: true,
        backdrop: 'static',
      });

      await mockModalRef.result;

      expect(mockCandidateExamService.delete).toHaveBeenCalledWith(examToDelete.id);
      expect(component.doSearch).toHaveBeenCalled();
    });

    it('should not delete the exam if confirmation is canceled', async () => {
      const mockModalRef = {
        result: Promise.resolve(false),
        componentInstance: {}
      };
      mockModalService.open.and.returnValue(mockModalRef as any);

      spyOn(component, 'doSearch');

      component.deleteCandidateExam(new MockCandidate().candidateExams[0]);

      await mockModalRef.result;

      expect(mockCandidateExamService.delete).not.toHaveBeenCalled();
      expect(component.doSearch).not.toHaveBeenCalled();
    });
  });
});
