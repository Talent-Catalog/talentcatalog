import {
  Component,
  CUSTOM_ELEMENTS_SCHEMA,
  EventEmitter,
  forwardRef,
  NgModule,
  NO_ERRORS_SCHEMA,
  Type,
} from '@angular/core';
import {ControlValueAccessor, NG_VALUE_ACCESSOR} from '@angular/forms';
import {TestBed, TestModuleMetadata} from '@angular/core/testing';
import {CommonModule} from '@angular/common';

// -------------------------------
// Base Classes
// -------------------------------
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

// -------------------------------
// Stub Factory
// -------------------------------
interface StubConfig {
  selector: string;
  template?: string;
  inputs?: string[];
  outputs?: string[];
  valueAccessor?: boolean;
  methods?: Record<string, (this: any, ...args: any[]) => any>;
}

function createStub(config: StubConfig): Type<any> {
  const outputs = [...(config.outputs ?? [])];
  if (config.valueAccessor) outputs.push('valueChange');

  let StubClass: any;

  @Component({
    selector: config.selector,
    template: config.template ?? '<ng-content></ng-content>',
    inputs: config.inputs,
    outputs,
    providers: config.valueAccessor
      ? [{ provide: NG_VALUE_ACCESSOR, multi: true, useExisting: forwardRef(() => StubClass) }]
      : [],
  })
  class Stub extends (config.valueAccessor ? ValueAccessorStubBase : GenericStubBase) {
    constructor() {
      super(outputs);
    }
    handleInput(event: Event): void {
      if (config.valueAccessor && typeof (this as any).updateValue === 'function') {
        const input = event.target as HTMLInputElement | HTMLTextAreaElement | null;
        (this as any).updateValue(input ? input.value : undefined);
      }
    }
  }

  StubClass = Stub;

  if (config.methods) {
    Object.entries(config.methods).forEach(([k, v]) => (StubClass.prototype as any)[k] = v);
  }

  return StubClass as Type<any>;
}

// -------------------------------
// Configs → Stubs
// -------------------------------
const STUBS = [
  {
    selector: 'tc-alert',
    inputs: ['type', 'dismissible'],
    outputs: ['closed'],
    template: `<div class="alert" [ngClass]="'alert-' + type"><ng-content></ng-content></div>`,
    methods: { onClose(this: GenericStubBase) { this.emitOutput('closed'); } },
  },
  {
    selector: 'tc-button',
    inputs: ['size', 'type', 'disabled', 'ariaLabel', 'title'],
    outputs: ['click'],
    template: `
      <button class="btn link"
              [ngClass]="[size ? 'btn-' + size : 'btn-default', type ? 'btn-' + type : 'btn-primary']"
              [disabled]="disabled"
              [attr.aria-label]="ariaLabel"
              [attr.title]="title"
              (click)="emitOutput('click', $event)">
        <ng-content></ng-content>
      </button>`,
  },
  {
    selector: 'tc-modal',
    inputs: ['title','actionText','disableAction','showCancel','icon','isError','cancelText','message'],
    outputs: ['onAction','onCancel'],
    template: `
      <div class="modal-header"><h5 class="modal-title">{{ title }}</h5></div>
      <div class="modal-body"><ng-content></ng-content></div>
      <div class="modal-footer">
        <button *ngIf="showCancel" type="button" class="btn btn-secondary" (click)="emitOutput('onCancel')">
          {{ cancelText || 'Cancel' }}
        </button>
        <button type="button" class="btn btn-primary" [disabled]="disableAction" (click)="emitOutput('onAction')">
          {{ actionText || 'Save' }}
        </button>
      </div>`,
  },
  {
    selector: 'tc-input',
    inputs: ['id','ariaLabel','name','placeholder','invalid','editable','type','disabled'],
    valueAccessor: true,
    template: `
      <input class="form-control"
             [attr.id]="id" [attr.name]="name" [attr.placeholder]="placeholder" [attr.aria-label]="ariaLabel"
             [attr.type]="type || 'text'" [disabled]="isDisabled || editable === false"
             [value]="value ?? ''" (input)="handleInput($event)" (blur)="markTouched()"/>`,
  },
  {
    selector: 'tc-textarea',
    inputs: ['id','name','placeholder','ariaLabel','disabled','invalid','defaultValue'],
    valueAccessor: true,
    template: `
      <textarea class="form-control"
                [attr.id]="id" [attr.name]="name" [attr.placeholder]="placeholder" [attr.aria-label]="ariaLabel"
                [disabled]="isDisabled"
                (input)="handleInput($event)" (blur)="markTouched()">
        {{ value ?? defaultValue ?? '' }}
      </textarea>`,
  },
  { selector: 'tc-icon, tc-field, tc-label, tc-tab, tc-tab-header, tc-tab-content' },
  { selector: 'tc-tabs', inputs: ['activeTabId'], outputs: ['tabChanged'], methods: {
      selectTab(this: GenericStubBase, id: string) { this.emitOutput('tabChanged', id); }
    }},
  { selector: 'tc-table', inputs: ['name','striped','hover','totalElements','pageSize','pageNumber'],
    outputs: ['pageNumberChange','pageChange'], methods: {
      triggerPageChange(this: GenericStubBase, n: number) {
        this.emitOutput('pageNumberChange', n);
        this.emitOutput('pageChange');
      }
    }},
  { selector: 'tc-link', inputs: ['href','target','title'], outputs: ['click'],
    template: `<a class="link" [attr.href]="href" [attr.target]="target" [attr.title]="title"
                  (click)="emitOutput('click', $event)"><ng-content></ng-content></a>` },
];

const STUB_DECLARATIONS = STUBS.map(createStub);

// -------------------------------
// Module + TestBed patch
// -------------------------------
@NgModule({
  declarations: STUB_DECLARATIONS,
  exports: STUB_DECLARATIONS,
  imports: [CommonModule],
})
class GlobalCustomStubsModule {}

const REAL_COMPONENTS = new Set([
  'TcModalComponent','TcTabsComponent','TcTabComponent','TcTabHeaderComponent','TcTabContentComponent',
]);

const originalConfigure = TestBed.configureTestingModule.bind(TestBed);
(TestBed.configureTestingModule as any) = (meta: TestModuleMetadata = {}) => {
  const hasReal = (meta.declarations ?? []).some(d => REAL_COMPONENTS.has(d?.name));
  if (!hasReal) meta.imports = [...(meta.imports ?? []), GlobalCustomStubsModule];
  meta.schemas = [...(meta.schemas ?? []), NO_ERRORS_SCHEMA, CUSTOM_ELEMENTS_SCHEMA];
  return originalConfigure(meta);
};
