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
    jasmine.DEFAULT_TIMEOUT_INTERVAL = 50000;  // Increase timeout interval

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

  it('should generate a PDF when exportAsPdf is called', ((done) => {
    const formName = 'testForm';
    window['scrollY'] = -1;
    const element = document.createElement('div');
    element.id = formName;
    element.style.width = '600px';
    element.style.height = '800px';
    document.body.appendChild(element);

    component.exportAsPdf(formName);

    setTimeout(()=>{
      expect(component.saving).toBeFalse();
      document.body.removeChild(element);
      done();
    },7000)
  }));
});
