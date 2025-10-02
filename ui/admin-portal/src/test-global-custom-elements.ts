// test-global-custom-elements.ts

import {
  Component,
  CUSTOM_ELEMENTS_SCHEMA,
  EventEmitter,
  forwardRef,
  NgModule,
  NO_ERRORS_SCHEMA,
} from '@angular/core';
import {ControlValueAccessor, NG_VALUE_ACCESSOR} from '@angular/forms';
import {TestBed, TestModuleMetadata} from '@angular/core/testing';
import {CommonModule} from '@angular/common';


/* ===========================
   Base stub helpers
   =========================== */

class GenericStubBase {
  constructor(outputs: readonly string[]) {
    outputs.forEach(o => (this as any)[o] = new EventEmitter<any>());
  }
  protected emitOutput<T>(name: string, value?: T): void {
    (this as any)[name]?.emit(value);
  }
}

class ValueAccessorStubBase extends GenericStubBase implements ControlValueAccessor {
  value: any;
  private changeFn = (_: any) => {};
  private touchedFn = () => {};
  protected isDisabled = false;

  writeValue(v: any): void {
    this.value = v;
  }
  registerOnChange(fn: any): void {
    this.changeFn = fn;
  }
  registerOnTouched(fn: any): void {
    this.touchedFn = fn;
  }
  setDisabledState?(isDisabled: boolean): void {
    this.isDisabled = isDisabled;
  }
  updateValue(v: any): void {
    this.value = v;
    this.changeFn(v);
    this.emitOutput('valueChange', v);
  }
  protected markTouched(): void {
    this.touchedFn();
  }
}

/* ===========================
   Concrete stub components
   =========================== */

@Component({
  selector: 'tc-alert',
  template: `
    <div class="alert" [ngClass]="'alert-' + type">
      <ng-content></ng-content>
    </div>
  `,
  inputs: ['type', 'dismissible'],
  outputs: ['closed'],
})
class TcAlertStubComponent extends GenericStubBase {
  type: string;
  constructor() { super(['closed']); }
  onClose(): void { this.emitOutput('closed'); }
}

@Component({
  selector: 'tc-button',
  template: `
    <button
      class="btn link"
      [ngClass]="[
        size ? 'btn-' + size : 'btn-default',
        type ? 'btn-' + type : 'btn-primary'
      ]"
      [disabled]="disabled"
      [attr.aria-label]="ariaLabel"
      [attr.title]="title"
      (click)="emitOutput('click', $event)"
    >
      <ng-content></ng-content>
    </button>
  `,
  inputs: ['size', 'type', 'disabled', 'ariaLabel', 'title'],
  outputs: ['click'],
})
class TcButtonStubComponent extends GenericStubBase {
  ariaLabel: any;
  size: any | string;
  disabled: any;
  title: any;
  type: string;
  constructor() { super(['click']); }
}

@Component({
  selector: 'tc-modal',
  template: `
    <div class="modal-header">
      <h5 class="modal-title">{{ title }}</h5>
    </div>

    <div class="modal-body">
      <ng-content></ng-content>
    </div>

    <div class="modal-footer">
      <button
        *ngIf="showCancel"
        type="button"
        class="btn btn-secondary"
        (click)="emitOutput('onCancel')"
      >
        {{ cancelText || 'Cancel' }}
      </button>
      <button
        type="button"
        class="btn btn-primary"
        [disabled]="disableAction"
        (click)="emitOutput('onAction')"
      >
        {{ actionText || 'Save' }}
      </button>
    </div>
  `,
  inputs: ['title', 'actionText', 'disableAction', 'showCancel', 'icon', 'isError', 'cancelText', 'message'],
  outputs: ['onAction', 'onCancel'],
})
class TcModalStubComponent extends GenericStubBase {
  title: string;
  showCancel: any;
  disableAction: any;
  actionText: string;
  cancelText: string;
  constructor() { super(['onAction', 'onCancel']); }
}

@Component({
  selector: 'tc-input',
  template: `
    <input
      class="form-control"
      [attr.id]="id"
      [attr.name]="name"
      [attr.placeholder]="placeholder"
      [attr.aria-label]="ariaLabel"
      [attr.type]="type || 'text'"
      [disabled]="isDisabled || editable === false"
      [value]="value ?? ''"
      (input)="handleInput($event)"
      (blur)="markTouched()"
    />
  `,
  inputs: ['id', 'ariaLabel', 'name', 'placeholder', 'invalid', 'editable', 'type', 'disabled'],
  outputs: ['valueChange'],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      multi: true,
      useExisting: forwardRef(() => TcInputStubComponent),
    },
  ],
})
class TcInputStubComponent extends ValueAccessorStubBase {
  ariaLabel: any;
  constructor() { super(['valueChange']); }
  handleInput(event: Event): void {
    const input = event.target as HTMLInputElement | null;
    this.updateValue(input ? input.value : undefined);
  }

  placeholder: any;
  editable: boolean;
  id: any;
  name: any;
  type: string;
}

@Component({
  selector: 'tc-textarea',
  template: `
    <textarea
      class="form-control"
      [attr.id]="id"
      [attr.name]="name"
      [attr.placeholder]="placeholder"
      [attr.aria-label]="ariaLabel"
      [disabled]="isDisabled"
      (input)="handleInput($event)"
      (blur)="markTouched()"
    >{{ value ?? defaultValue ?? '' }}</textarea>
  `,
  inputs: ['id', 'name', 'placeholder', 'ariaLabel', 'disabled', 'invalid', 'defaultValue'],
  outputs: ['valueChange'],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      multi: true,
      useExisting: forwardRef(() => TcTextareaStubComponent),
    },
  ],
})
class TcTextareaStubComponent extends ValueAccessorStubBase {
  constructor() { super(['valueChange']); }
  handleInput(event: Event): void {
    const input = event.target as HTMLTextAreaElement | null;
    this.updateValue(input ? input.value : undefined);
  }

  placeholder: any;
  defaultValue: any;
  ariaLabel: any;
  id: any;
  name: any;
}

@Component({
  // one component can match multiple selectors separated by commas
  selector: 'tc-icon, tc-field, tc-label, tc-tab, tc-tab-header, tc-tab-content, tc-card, tc-card-header',
  template: '<ng-content></ng-content>',
})
class PassThroughTcStubComponent extends GenericStubBase {
  constructor() { super([]); }
}

@Component({
  selector: 'tc-tabs',
  template: '<ng-content></ng-content>',
  inputs: ['activeTabId'],
  outputs: ['tabChanged'],
})
class TcTabsStubComponent extends GenericStubBase {
  constructor() { super(['tabChanged']); }
  selectTab(id: string): void { this.emitOutput('tabChanged', id); }
}

@Component({
  selector: 'tc-table',
  template: '<ng-content></ng-content>',
  inputs: ['name', 'striped', 'hover', 'totalElements', 'pageSize', 'pageNumber'],
  outputs: ['pageNumberChange', 'pageChange'],
})
class TcTableStubComponent extends GenericStubBase {
  constructor() { super(['pageNumberChange', 'pageChange']); }
  triggerPageChange(newPage: number): void {
    this.emitOutput('pageNumberChange', newPage);
    this.emitOutput('pageChange');
  }
}

@Component({
  selector: 'tc-link',
  template: `
    <a
      class="link"
      [attr.href]="href"
      [attr.target]="target"
      [attr.title]="title"
      (click)="emitOutput('click', $event)"
    >
      <ng-content></ng-content>
    </a>
  `,
  inputs: ['href', 'target', 'title'],
  outputs: ['click'],
})
class TcLinkStubComponent extends GenericStubBase {
  href: any;
  target: any;
  title: any;
  constructor() { super(['click']); }
}

@Component({
  selector: 'tc-description-list',
  template: '<ng-content></ng-content>',
  inputs: ['direction'],
})
class TcDescriptionListStub extends GenericStubBase {
  constructor() { super([]); }
}


@Component({
  selector: 'tc-description-item',
  template: '<ng-content></ng-content>',
  inputs: ['input', 'icon'],
})
class TcDescriptionItemStub extends GenericStubBase {
  constructor() { super([]); }
}

/* ===========================
   Static declarations (AOT-safe)
   =========================== */

const STUB_DECLARATIONS = [
  TcAlertStubComponent,
  TcButtonStubComponent,
  TcModalStubComponent,
  TcInputStubComponent,
  TcTextareaStubComponent,
  PassThroughTcStubComponent,
  TcTabsStubComponent,
  TcTableStubComponent,
  TcLinkStubComponent,
  TcDescriptionListStub,
  TcDescriptionItemStub,
];

/* ===========================
   Module + TestBed patch
   =========================== */

@NgModule({
  declarations: STUB_DECLARATIONS,
  exports: STUB_DECLARATIONS,
  imports: [CommonModule], // NgIf/NgFor available to stubs
})
class GlobalCustomStubsModule {}

const REAL_COMPONENTS = new Set([
  'TcModalComponent',
  'TcTabsComponent',
  'TcTabComponent',
  'TcTabHeaderComponent',
  'TcTabContentComponent',
]);

const originalConfigure = TestBed.configureTestingModule.bind(TestBed);
(TestBed.configureTestingModule as any) = (meta: TestModuleMetadata = {}) => {
  const hasReal = (meta.declarations ?? []).some(d => REAL_COMPONENTS.has(d?.name));
  if (!hasReal) {
    meta.imports = [...(meta.imports ?? []), GlobalCustomStubsModule];
  }
  meta.schemas = [...(meta.schemas ?? []), NO_ERRORS_SCHEMA, CUSTOM_ELEMENTS_SCHEMA];
  return originalConfigure(meta);
};
