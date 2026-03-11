import { Component, EventEmitter, Input, Output, Pipe, PipeTransform } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { of } from 'rxjs';

import { TermsComponent } from './terms.component';
import { CandidateService } from '../../services/candidate.service';
import { TermsInfoService } from '../../services/terms-info.service';

@Pipe({name: 'translate'})
class TranslatePipeStub implements PipeTransform {
  transform(value: string): string {
    return value;
  }
}

@Component({selector: 'app-error', template: ''})
class AppErrorStubComponent {
  @Input() error: any;
}

@Component({selector: 'app-tab-header', template: '<ng-content></ng-content>'})
class AppTabHeaderStubComponent {}

@Component({selector: 'app-show-terms', template: '<ng-content></ng-content>'})
class AppShowTermsStubComponent {
  @Input() requestTermsRead: boolean;
  @Output() termsRead = new EventEmitter<void>();
}

@Component({selector: 'tc-icon', template: '<ng-content></ng-content>'})
class TcIconStubComponent {
  @Input() color: string;
}

@Component({selector: 'tc-button', template: '<ng-content></ng-content>'})
class TcButtonStubComponent {
  @Input() color: string;
  @Input() routerLink: string | any[];
}

describe('PrivacyPolicyComponent', () => {
  let component: TermsComponent;
  let fixture: ComponentFixture<TermsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [
        TermsComponent,
        TranslatePipeStub,
        AppErrorStubComponent,
        AppTabHeaderStubComponent,
        AppShowTermsStubComponent,
        TcIconStubComponent,
        TcButtonStubComponent
      ],
      providers: [
        {
          provide: CandidateService,
          useValue: {
            getCandidatePersonal: () => of({
              user: {partner: {name: 'TBB', notificationEmail: 'partner@example.com'}},
              acceptedPrivacyPolicyId: 'old-policy',
              acceptedPrivacyPolicyDate: '2024-01-01',
              acceptedPrivacyPolicyPartner: {name: 'TBB'}
            }),
            updateAcceptedPrivacyPolicy: () => of(null),
            updatePendingTermsAcceptance: () => of(null)
          }
        },
        {
          provide: TermsInfoService,
          useValue: {
            getCurrentByType: () => of({id: 'new-policy', content: '<p>Policy</p>'})
          }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(TermsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should render the back button linked to the profile page', () => {
    const buttons = fixture.debugElement.queryAll(By.directive(TcButtonStubComponent))
      .map(debugEl => debugEl.componentInstance as TcButtonStubComponent);

    expect(buttons[0].routerLink).toBe('/profile');
  });

  it('should render a success-colored accept button when terms acceptance is required', () => {
    const buttons = fixture.debugElement.queryAll(By.directive(TcButtonStubComponent))
      .map(debugEl => debugEl.componentInstance as TcButtonStubComponent);
    const showTerms = fixture.debugElement.query(By.directive(AppShowTermsStubComponent)).componentInstance as AppShowTermsStubComponent;

    expect(component.requestAcceptance).toBeTrue();
    expect(showTerms.requestTermsRead).toBeTrue();
    expect(buttons[1].color).toBe('success');
  });
});
