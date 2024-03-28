import {ComponentFixture, TestBed} from '@angular/core/testing';

import {CreateUpdateHelpLinkComponent} from './create-update-help-link.component';

describe('CreateUpdateHelpLinkComponent', () => {
  let component: CreateUpdateHelpLinkComponent;
  let fixture: ComponentFixture<CreateUpdateHelpLinkComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CreateUpdateHelpLinkComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CreateUpdateHelpLinkComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
