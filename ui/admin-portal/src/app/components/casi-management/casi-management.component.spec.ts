import {NO_ERRORS_SCHEMA} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {of} from 'rxjs';
import {CasiManagementComponent} from './casi-management.component';
import {AuthenticationService} from "../../services/authentication.service";
import {AuthorizationService} from "../../services/authorization.service";
import {LocalStorageService} from "../../services/local-storage.service";

describe('CasiManagementComponent', () => {
  let component: CasiManagementComponent;
  let fixture: ComponentFixture<CasiManagementComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [CasiManagementComponent],
      providers: [
        {
          provide: AuthenticationService,
          useValue: {getLoggedInUser: () => ({id: 1})}
        },
        {
          provide: AuthorizationService,
          useValue: {isSystemAdminOnly: () => true}
        },
        {
          provide: LocalStorageService,
          useValue: {get: () => null, set: () => of(null)}
        }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(CasiManagementComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
