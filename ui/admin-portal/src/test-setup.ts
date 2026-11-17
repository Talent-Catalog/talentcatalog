/// <reference types="vitest/globals" />

import './test-global-custom-elements';



import { vi } from 'vitest';

/**
 * Admin Portal test environment.
 *
 * Angular's Vitest builder initializes TestBed automatically.
 * The imported module registers Admin Portal's shared component stubs.
 */



/**
 * Restore spied methods between tests.
 * Standalone vi.fn() mocks remain managed by their individual specs.
 */
afterEach(() => {
  vi.restoreAllMocks();
});

// ADMIN_VITEST_BROWSER_SHIMS_V2
// Node 24 can expose a global localStorage getter returning undefined while
// the test DOM has window.localStorage. Prefer the DOM implementation and only
// use an in-memory Storage fallback when necessary.
function adminTestMemoryStorage(): Storage {
  const values = new Map<string, string>();
  const result: Storage = {
    get length(): number { return values.size; },
    clear(): void {
      for (const key of values.keys()) delete (this as any)[key];
      values.clear();
    },
    key(index: number): string | null { return Array.from(values.keys())[index] ?? null; },
    getItem(key: string): string | null { return values.get(String(key)) ?? null; },
    setItem(key: string, value: string): void {
      const k = String(key); const v = String(value);
      values.set(k, v);
      Object.defineProperty(this, k, {configurable: true, enumerable: true, writable: true, value: v});
    },
    removeItem(key: string): void {
      const k = String(key); values.delete(k); delete (this as any)[k];
    }
  };
  return result;
}
let adminFallbackStorage: Storage | undefined;
function adminEnsureBrowserAPIs(): void {
  if (typeof window !== 'undefined') {
    let browserStorage: Storage | undefined;
    try {
      const possible = window.localStorage;
      if (possible && typeof possible.getItem === 'function' && typeof possible.clear === 'function') browserStorage = possible;
    } catch { /* SecurityError or Node's unavailable localStorage getter */ }
    if (!browserStorage) {
      adminFallbackStorage ??= adminTestMemoryStorage();
      browserStorage = adminFallbackStorage;
      const winDesc = Object.getOwnPropertyDescriptor(window, 'localStorage');
      if (!winDesc || winDesc.configurable) {
        Object.defineProperty(window, 'localStorage', {configurable: true, enumerable: true, value: browserStorage});
      }
    }
    let hasGlobalStorage = false;
    try {
      const possible = globalThis.localStorage;
      hasGlobalStorage = !!possible && typeof possible.getItem === 'function' && typeof possible.clear === 'function';
    } catch { /* ignore */ }
    if (!hasGlobalStorage) {
      const desc = Object.getOwnPropertyDescriptor(globalThis, 'localStorage');
      if (!desc || desc.configurable) {
        Object.defineProperty(globalThis, 'localStorage', {configurable: true, enumerable: true, writable: true, value: browserStorage});
      } else {
        throw new Error('Admin Portal Vitest setup: globalThis.localStorage cannot be patched.');
      }
    }
  }
  if (typeof document !== 'undefined' && typeof document.execCommand !== 'function') {
    Object.defineProperty(document, 'execCommand', {configurable: true, writable: true, value: () => false});
  }
}
adminEnsureBrowserAPIs();
// Re-establish aliases before each test if a preceding spec replaced them.
beforeEach(() => adminEnsureBrowserAPIs());

// ADMIN_TEST_BLOB_URLS_20261117
// jsdom Blob is not compatible with the Node Blob assumed by Vitest's native URL API.
// Create/revoke test object URLs without attempting to read a Node-only _buffer.
let adminTestBlobSequence = 0;
const adminTestCreateObjectURL = (blob: Blob): string => {
  if (!blob || typeof blob !== 'object') throw new TypeError('Expected a Blob-like object');
  return 'blob:admin-vitest-' + (++adminTestBlobSequence);
};
function adminEnsureTestBlobUrls(): void {
  for (const ctor of new Set<any>([
    typeof URL === 'function' ? URL : null,
    typeof window !== 'undefined' ? window.URL : null
  ])) {
    if (!ctor) continue;
    const patch = (name: string, fn: Function) => {
      const desc = Object.getOwnPropertyDescriptor(ctor, name);
      if (!desc || desc.configurable || desc.writable) {
        Object.defineProperty(ctor, name, {configurable: true, writable: true, value: fn});
      }
    };
    patch('createObjectURL', adminTestCreateObjectURL);
    patch('revokeObjectURL', (_url: string) => {});
  }
}
adminEnsureTestBlobUrls();
beforeEach(() => adminEnsureTestBlobUrls());
