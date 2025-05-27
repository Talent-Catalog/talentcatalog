import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AcceptTermsComponent } from './accept-terms.component';
import { Component, DebugElement } from '@angular/core';
import { By } from '@angular/platform-browser';
import { FormsModule } from '@angular/forms';

@Component({
  template: `
    <app-accept-terms (accepted)="onAccepted()">
      <div style="height: 1000px;">Mock Terms Content</div>
    </app-accept-terms>
  `
})
class TestHostComponent {
  accepted = false;
  onAccepted() {
    this.accepted = true;
  }
}

describe('AcceptTermsComponent', () => {
  let fixture: ComponentFixture<TestHostComponent>;
  let hostComponent: TestHostComponent;
  let acceptComponent: DebugElement;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [AcceptTermsComponent, TestHostComponent],
      imports: [FormsModule]
    }).compileComponents();

    fixture = TestBed.createComponent(TestHostComponent);
    hostComponent = fixture.componentInstance;
    acceptComponent = fixture.debugElement.query(By.directive(AcceptTermsComponent));
    fixture.detectChanges();
  });

  it('should render projected content', () => {
    const projectedContent = acceptComponent.nativeElement.querySelector('.terms-box');
    expect(projectedContent.textContent).toContain('Mock Terms Content');
  });

  it('should disable checkbox and button initially', () => {
    const checkbox = acceptComponent.nativeElement.querySelector('input[type="checkbox"]');
    const button = acceptComponent.nativeElement.querySelector('button');
    expect(checkbox.disabled).toBeTrue();
    expect(button.disabled).toBeTrue();
  });

  it('should enable checkbox after scrolling to bottom', () => {
    const box = acceptComponent.nativeElement.querySelector('.terms-box');
    box.scrollTop = box.scrollHeight; // simulate scroll to bottom
    box.dispatchEvent(new Event('scroll'));
    fixture.detectChanges();

    const checkbox = acceptComponent.nativeElement.querySelector('input[type="checkbox"]');
    expect(checkbox.disabled).toBeFalse();
  });

  it('should emit accepted event after checking and clicking continue', () => {
    const box = acceptComponent.nativeElement.querySelector('.terms-box');
    box.scrollTop = box.scrollHeight;
    box.dispatchEvent(new Event('scroll'));
    fixture.detectChanges();

    const checkbox = acceptComponent.nativeElement.querySelector('input[type="checkbox"]');
    checkbox.click();
    fixture.detectChanges();

    const button = acceptComponent.nativeElement.querySelector('button');
    button.click();
    fixture.detectChanges();

    expect(hostComponent.accepted).toBeTrue();
  });
});
