import { ComponentFixture, TestBed } from '@angular/core/testing';

import { JobLanguageComponent } from './job-language.component';

describe('JobLanguageComponent', () => {
  let component: JobLanguageComponent;
  let fixture: ComponentFixture<JobLanguageComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ JobLanguageComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(JobLanguageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
