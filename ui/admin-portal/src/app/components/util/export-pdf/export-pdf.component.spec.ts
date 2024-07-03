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
    const mockCanvas = document.createElement('canvas');
    document.body.appendChild(mockCanvas);

    mockCanvas.width = 600;
    mockCanvas.height = 1200;
    window['scrollY'] = -1;
    spyOn(document, 'getElementById').and.returnValue(mockCanvas); // Mock getElementById

    component.exportAsPdf(formName);

    expect(component.saving).toBeTrue();
    expect(document.getElementById).toHaveBeenCalledWith(formName);
    document.body.removeChild(mockCanvas);

  });
});
