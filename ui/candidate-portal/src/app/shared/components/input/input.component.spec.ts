import {ComponentFixture, TestBed} from '@angular/core/testing';

import {InputComponent} from './input.component';
import {Component} from '@angular/core';
import {By} from '@angular/platform-browser';

@Component({
  template: `
    <tc-input
      id="reviewable"
      type="checkbox">
    </tc-input>

    <label for="reviewable">
      Should result of searches be reviewable?
    </label>
  `
})
class HostComponent {}
describe('InputComponent', () => {
  let component: InputComponent;
  let fixture: ComponentFixture<InputComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [InputComponent, HostComponent]
    });
    fixture = TestBed.createComponent(InputComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should allow an external label to toggle a checkbox', () => {
    const hostFixture = TestBed.createComponent(HostComponent);
    hostFixture.detectChanges();

    const inputComponent =
      hostFixture.debugElement.query(By.directive(InputComponent));

    const componentHost =
      inputComponent.nativeElement as HTMLElement;

    const nativeInput =
      inputComponent.query(By.css('input')).nativeElement as HTMLInputElement;

    const label =
      hostFixture.nativeElement.querySelector(
        'label[for="reviewable"]'
      ) as HTMLLabelElement;

    expect(componentHost.hasAttribute('id')).toBeFalse();
    expect(nativeInput.id).toBe('reviewable');
    expect(nativeInput.checked).toBeFalse();

    label.click();
    hostFixture.detectChanges();

    expect(nativeInput.checked).toBeTrue();
  });
});
