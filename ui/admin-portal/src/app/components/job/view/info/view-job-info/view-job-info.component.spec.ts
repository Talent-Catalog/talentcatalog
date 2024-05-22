import {ComponentFixture, fakeAsync, TestBed, tick} from '@angular/core/testing';
import {ViewJobInfoComponent } from './view-job-info.component';
import {NgbModal, NgbModalRef} from '@ng-bootstrap/ng-bootstrap';
import {Directive, Input} from "@angular/core";
import {EditJobInfoComponent} from "../edit-job-info/edit-job-info.component";
import {MockJob} from "../../../../../MockData/MockJob";

@Directive({
  selector: '[routerLink]'
})
export class RouterLinkDirectiveStub {
  @Input('routerLink') linkParams: any;
}
fdescribe('ViewJobInfoComponent', () => {
  let component: ViewJobInfoComponent;
  let fixture: ComponentFixture<ViewJobInfoComponent>;
  let modalService: NgbModal;
   beforeEach(async () => {
     await TestBed.configureTestingModule({
      declarations: [ ViewJobInfoComponent,RouterLinkDirectiveStub ],
      providers: [
        { provide: NgbModal  },
      ]
    })
    .compileComponents();

    modalService = TestBed.inject(NgbModal);
  });

  beforeEach(() => { // Use this block for component creation
    fixture = TestBed.createComponent(ViewJobInfoComponent);
    component = fixture.componentInstance;

    component.editable = true;
    // Provide a mock Job object
    component.job = MockJob;

    fixture.detectChanges();
  });
  it('should create', () => {
    expect(component).toBeTruthy();
  });
  it('should display job information correctly', () => {
    const compiled = fixture.nativeElement;
    expect(compiled.querySelector('.card-header').textContent).toContain('Job Information');
    expect(compiled.querySelector('.form-control-plaintext').textContent).toContain('USA');
   });

  it('should open edit modal when edit button is clicked', () => {
    // Spy on modalService.open method and return a dummy NgbModalRef
    spyOn(modalService, 'open').and.returnValue({
      componentInstance: {},
      result: Promise.resolve('saved')
    } as NgbModalRef);

    // Trigger the editJobInfo method, for example, by clicking the edit button
    const editButton = fixture.nativeElement.querySelector('.btn-secondary');
    editButton.click();

    // Expect that modalService.open was called with EditJobInfoComponent
    expect(modalService.open).toHaveBeenCalledWith(EditJobInfoComponent, jasmine.any(Object));
  });

});
