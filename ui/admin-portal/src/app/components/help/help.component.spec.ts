import {HelpComponent} from "./help.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {HelpLinkService} from "../../services/help-link.service";
import {of, throwError} from "rxjs";
import {By} from "@angular/platform-browser";
import {MOCK_HELP_LINK} from "../../MockData/MockHelpLink";
import {NgbTooltipModule} from "@ng-bootstrap/ng-bootstrap";
import {SearchHelpLinkRequest} from "../../model/help-link";

describe('HelpComponent', () => {
  let component: HelpComponent;
  let fixture: ComponentFixture<HelpComponent>;
  let helpLinkService: jasmine.SpyObj<HelpLinkService>;

  beforeEach(async () => {
    const helpLinkServiceSpy = jasmine.createSpyObj('HelpLinkService', ['fetch']);

    await TestBed.configureTestingModule({
      declarations: [HelpComponent],
      imports: [NgbTooltipModule],
      providers: [
        { provide: HelpLinkService, useValue: helpLinkServiceSpy }
      ]
    })
    .compileComponents();

    helpLinkService = TestBed.inject(HelpLinkService) as jasmine.SpyObj<HelpLinkService>;
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(HelpComponent);
    component = fixture.componentInstance;
    helpLinkService.fetch.and.returnValue(of([MOCK_HELP_LINK]));

    const mockRequest: SearchHelpLinkRequest = {
      countryId: 1,
      caseOppId: 123,
      caseStage: 'Stage1',
      focus: 'Focus1',
      jobOppId: 456,
      jobStage: 'Stage2',
      nextStepName: 'NextStep1',
      userId: 789
    };

    component.request = mockRequest; // Assign mock data to request input
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should fetch help links on button click', (() => {
    fixture.detectChanges();
    const button = fixture.debugElement.query(By.css('.help-tip-button')).nativeElement;
    button.click();
    expect(component.helpLinks).toEqual([MOCK_HELP_LINK]);
    expect(component.loading).toBeFalse();
    expect(component.error).toBeNull();
  }));

  it('should display error if fetching help links fails', (() => {
    const errorMessage = 'Failed to fetch help links';
    helpLinkService.fetch.and.returnValue(throwError(errorMessage));

    const button = fixture.debugElement.query(By.css('.help-tip-button')).nativeElement;
    button.click();

    expect(component.helpLinks).toBeUndefined();
    expect(component.loading).toBeFalse();
    expect(component.error).toBe(errorMessage);
  }));

  it('should display help links in tooltip content', (() => {

    component.helpLinks = [MOCK_HELP_LINK];
    fixture.detectChanges();

    const button = fixture.debugElement.query(By.css('.help-tip-button')).nativeElement;
    button.click(); // Trigger click to open tooltip

    fixture.detectChanges();

    const helpTipContent = document.querySelector('.help-tips');
    expect(helpTipContent.textContent).toContain('Example Help Link (USA)');

  }));
});
