import {ComponentFixture, TestBed} from '@angular/core/testing';
import {ButtonComponent} from './button.component';
import {By} from '@angular/platform-browser';

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

  describe('loading state', () => {
    it('should render spinner icon when loading is true', () => {
      component.loading = true;
      fixture.detectChanges();

      const spinner = fixture.debugElement.query(By.css('i.fas.fa-spinner.fa-spin'));
      expect(spinner).toBeTruthy();
    });

    it('should disable the native button when loading is true', () => {
      component.loading = true;
      fixture.detectChanges();

      const buttonElement: HTMLButtonElement = fixture.nativeElement.querySelector('button');
      expect(buttonElement.disabled).toBeTrue();
    });

    it('should add btn-loading class when loading is true', () => {
      component.loading = true;
      fixture.detectChanges();

      const buttonElement: HTMLButtonElement = fixture.nativeElement.querySelector('button');
      expect(buttonElement.classList).toContain('btn-loading');
    });

    it('should not emit onClick when loading is true', () => {
      component.loading = true;
      fixture.detectChanges();

      spyOn(component.onClick, 'emit');

      const buttonDebug = fixture.debugElement.query(By.css('button'));
      buttonDebug.triggerEventHandler('click', new MouseEvent('click'));

      expect(component.onClick.emit).not.toHaveBeenCalled();
    });

    it('should emit onClick when loading is false and disabled is false', () => {
      component.loading = false;
      component.disabled = false;
      fixture.detectChanges();

      spyOn(component.onClick, 'emit');

      const buttonDebug = fixture.debugElement.query(By.css('button'));
      buttonDebug.triggerEventHandler('click', new MouseEvent('click'));

      expect(component.onClick.emit).toHaveBeenCalledTimes(1);
    });
  });
});
