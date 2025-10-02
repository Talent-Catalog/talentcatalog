import {ComponentFixture, TestBed} from '@angular/core/testing';

import {TcModalComponent} from './tc-modal.component';
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {By} from "@angular/platform-browser";
import {ButtonComponent} from "../button/button.component";
import {Component} from "@angular/core";

@Component({
  template: `
    <tc-modal
      [heading]="heading"
      [actionText]="actionText"
      [disableAction]="disableAction"
      [showCancel]="showCancel"
      [icon]="icon"
      (onAction)="onAction()"
    >
      <p>Modal body content</p>
    </tc-modal>
  `
})
class TestHostComponent {
  heading = 'Test Modal';
  actionText = 'Confirm';
  disableAction = false;
  showCancel = true;
  icon?: string;

  actionCalled = false;
  onAction() {
    this.actionCalled = true;
  }
}

describe('TcModalComponent', () => {
  let fixture: ComponentFixture<TestHostComponent>;
  let host: TestHostComponent;
  let activeModal: NgbActiveModal;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [TcModalComponent, TestHostComponent, ButtonComponent],
      providers: [NgbActiveModal],
    }).compileComponents();

    fixture = TestBed.createComponent(TestHostComponent);
    host = fixture.componentInstance;
    activeModal = TestBed.inject(NgbActiveModal);
    fixture.detectChanges();
  });

  it('should render header', () => {
    const headerEl = fixture.debugElement.query(By.css('.tc-modal-header')).nativeElement;
    expect(headerEl.textContent).toContain('Test Modal');
  });

  it('should render body content', () => {
    const bodyEl = fixture.debugElement.query(By.css('.tc-modal-body')).nativeElement;
    expect(bodyEl.textContent).toContain('Modal body content');
  });

  it('should show icon if provided', () => {
    host.icon = 'fas fa-bell';
    fixture.detectChanges(); // trigger ngIf

    const iconEl = fixture.debugElement.query(By.css('.tc-modal i'));
    expect(iconEl).toBeTruthy();
    expect(iconEl.nativeElement.classList).toContain('fas');
    expect(iconEl.nativeElement.classList).toContain('fa-bell');

    const modalDiv = fixture.debugElement.query(By.css('.tc-modal'));
    expect(modalDiv.nativeElement.className).toContain('has-icon');
  });

  it('should not show icon if not provided', () => {
    host.icon = undefined;
    fixture.detectChanges();

    const iconEl = fixture.debugElement.query(By.css('.tc-modal i'));
    expect(iconEl).toBeNull();

    const modalDiv = fixture.debugElement.query(By.css('.tc-modal'));
    expect(modalDiv.nativeElement.className).toContain('no-icon');
  });

  it('should call onAction when primary button clicked', () => {
    const primaryBtn = fixture.debugElement.queryAll(By.css('tc-button'))[host.showCancel ? 1 : 0];
    primaryBtn.triggerEventHandler('click', null);
    expect(host.actionCalled).toBeTrue();
  });

  it('should call activeModal.dismiss when cancel button clicked', () => {
    spyOn(activeModal, 'dismiss');
    const cancelBtn = fixture.debugElement.queryAll(By.directive(ButtonComponent))[0];
    cancelBtn.nativeElement.click();
    expect(activeModal.dismiss).toHaveBeenCalledWith(false);
  });

  it('should hide cancel button when showCancel is false', () => {
    host.showCancel = false;
    fixture.detectChanges();

    const cancelBtn = fixture.debugElement.query(By.css('tc-modal-footer tc-button[type="outline"]'));
    expect(cancelBtn).toBeNull();
  });

  it('should disable primary button if disableAction is true', () => {
    host.disableAction = true;
    fixture.detectChanges();

    const buttons = fixture.debugElement.queryAll(By.directive(ButtonComponent));
    expect(buttons.length).toBeGreaterThan(0); // sanity check

    const primaryBtn = buttons[host.showCancel ? 1 : 0];
    expect(primaryBtn.componentInstance.disabled).toBeTrue();
  });
});
