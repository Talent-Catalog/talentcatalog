import {ExportPdfComponent} from "./export-pdf.component";
import {ComponentFixture, fakeAsync, TestBed, tick, waitForAsync} from "@angular/core/testing";
import {MockCandidate} from "../../../MockData/MockCandidate";

fdescribe('ExportPdfComponent', () => {
  let component: ExportPdfComponent;
  let fixture: ComponentFixture<ExportPdfComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ExportPdfComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ExportPdfComponent);
    component = fixture.componentInstance;
    component.candidate = new MockCandidate();
    component.idToExport = 'testDiv';

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should set saving to true when exportAsPdf is called', () => {
    const formName = 'testForm';
    const element = document.createElement('div');
    element.id = formName;
    document.body.appendChild(element);

    component.exportAsPdf(formName);

    expect(component.saving).toBeTrue();
    document.body.removeChild(element);
  });

});
