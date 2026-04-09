import { Component, Input, Pipe, PipeTransform } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { ActivatedRoute, Router } from '@angular/router';
import { of } from 'rxjs';

import { LandingComponent } from './landing.component';
import { AuthenticationService } from '../../services/authentication.service';
import { BrandingService } from '../../services/branding.service';
import { LanguageService } from '../../services/language.service';

@Pipe({name: 'translate'})
class TranslatePipeStub implements PipeTransform {
  transform(value: string): string {
    return value;
  }
}

@Component({selector: 'app-login', template: ''})
class AppLoginStubComponent {}

@Component({selector: 'tc-card', template: '<ng-content></ng-content>'})
class TcCardStubComponent {
  @Input() header: boolean;
}

@Component({selector: 'tc-button', template: '<ng-content></ng-content>'})
class TcButtonStubComponent {
  @Input() color: string;
  @Input() type: string;
  @Input() routerLink: string | any[];
  @Input() queryParamsHandling: string;
}

describe('LandingComponent', () => {
  let component: LandingComponent;
  let fixture: ComponentFixture<LandingComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [
        LandingComponent,
        TranslatePipeStub,
        AppLoginStubComponent,
        TcCardStubComponent,
        TcButtonStubComponent
      ],
      providers: [
        {
          provide: AuthenticationService,
          useValue: {
            isAuthenticated: () => false,
            isRegistered: () => false,
            authenticateInContextTranslation: () => of(null)
          }
        },
        {
          provide: BrandingService,
          useValue: {
            getBrandingInfo: () => of({logo: '', partnerName: '', websiteUrl: ''})
          }
        },
        {provide: Router, useValue: {navigate: () => Promise.resolve(true)}},
        {provide: ActivatedRoute, useValue: {snapshot: {queryParams: {}}}},
        {provide: LanguageService, useValue: {changeLanguage: () => {}, setUsAfghan: () => {}}}
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(LandingComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should render the login card without a header', () => {
    const card = fixture.debugElement.query(By.directive(TcCardStubComponent)).componentInstance as TcCardStubComponent;
    expect(card.header).toBeFalse();
  });

  it('should render the register tc-button with outline styling and merged query params', () => {
    const button = fixture.debugElement.query(By.directive(TcButtonStubComponent)).componentInstance as TcButtonStubComponent;

    expect(button.color).toBe('primary');
    expect(button.type).toBe('outline');
    expect(button.routerLink).toBe('/register');
    expect(button.queryParamsHandling).toBe('merge');
  });
});
