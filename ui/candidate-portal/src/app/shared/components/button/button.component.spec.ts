import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Component } from '@angular/core';
import { ButtonComponent } from './button.component';
import { By } from '@angular/platform-browser';

describe('ButtonComponent', () => {
  let component: ButtonComponent;
  let fixture: ComponentFixture<ButtonComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [ButtonComponent]
    });
    fixture = TestBed.createComponent(ButtonComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should apply correct classes based on size and type inputs', () => {
    component.size = 'sm';
    component.type = 'outline';
    fixture.detectChanges();

    const buttonElement: HTMLButtonElement = fixture.nativeElement.querySelector('button');
    expect(buttonElement.classList).toContain('btn-sm');
    expect(buttonElement.classList).toContain('btn-outline');
  });

  it('should apply "disabled" attribute when disabled is true', () => {
    component.disabled = true;
    fixture.detectChanges();

    const buttonElement: HTMLButtonElement = fixture.nativeElement.querySelector('button');
    expect(buttonElement.disabled).toBeTrue();
  });

  describe('aria-label support', () => {
    beforeEach(() => {
      // Only needed if you implement @Input() ariaLabel
      (component as any).ariaLabel = 'Refresh';
      fixture.detectChanges();
    });

    it('should set aria-label when ariaLabel input is provided', () => {
      const buttonDebug = fixture.debugElement.query(By.css('button'));
      expect(buttonDebug.attributes['aria-label']).toBe('Refresh');
    });
  });
});
