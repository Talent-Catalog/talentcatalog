import {ComponentFixture, TestBed} from '@angular/core/testing';
import {IntelligenceComponent} from './intelligence.component';
import {EnvService} from '../../services/env.service';
import {Component, Input} from '@angular/core';

// Stub for <app-preset-embed>
@Component({
  selector: 'app-preset-embed',
  template: ''
})
class MockPresetEmbedComponent {
  @Input() dashboardId!: string;
}

describe('IntelligenceComponent', () => {
  let component: IntelligenceComponent;
  let fixture: ComponentFixture<IntelligenceComponent>;
  let mockEnvService: Partial<EnvService>;

  beforeEach(async () => {
    mockEnvService = {
      allCandidatesDashboardId: 'test-dashboard-id'
    };

    await TestBed.configureTestingModule({
      declarations: [IntelligenceComponent, MockPresetEmbedComponent],
      providers: [{provide: EnvService, useValue: mockEnvService}]
    }).compileComponents();

    fixture = TestBed.createComponent(IntelligenceComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should assign dashboardId from EnvService', () => {
    expect(component.allCandidatesDashboardId).toBe('test-dashboard-id');
  });

  it('should pass dashboardId to app-preset-embed component', () => {
    const embedComponent: MockPresetEmbedComponent = fixture.debugElement
    .nativeElement.querySelector('app-preset-embed');
    expect(embedComponent).toBeTruthy();
  });
});
