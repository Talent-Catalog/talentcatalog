import {ComponentFixture, TestBed} from '@angular/core/testing';

import {JobSkillsComponent} from './job-skills.component';

describe('JobSkillsComponent', () => {
  let component: JobSkillsComponent;
  let fixture: ComponentFixture<JobSkillsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ JobSkillsComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(JobSkillsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
