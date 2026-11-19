
import {provideHttpClient, withInterceptorsFromDi, withXhr} from '@angular/common/http';
import {provideHttpClientTesting} from '@angular/common/http/testing';
import {NO_ERRORS_SCHEMA} from '@angular/core';
import {TestBed} from '@angular/core/testing';
import {RouterTestingModule} from '@angular/router/testing';
import {TranslateModule} from '@ngx-translate/core';
import {afterEach, beforeEach, vi} from 'vitest';

/**
 * Global Angular TestBed configuration shared by Candidate Portal unit tests.
 */
beforeEach(() => {
  TestBed.configureTestingModule({
    imports: [
      RouterTestingModule,
      TranslateModule.forRoot()
    ],
    providers: [
      provideHttpClient(
        withXhr(),
        withInterceptorsFromDi()
      ),
      provideHttpClientTesting()
    ],
    schemas: [NO_ERRORS_SCHEMA]
  });
});

/**
 * Installs browser API polyfills required by unit tests running in jsdom.
 *
 * These APIs are available in real browsers but may be missing or incomplete
 * in the Node.js/Vitest environment.
 *
 * Test-only setup. Never import this file into production application code.
 */
function installBrowserTestPolyfills(): void {
  installLocalStorage();
  installMatchMedia();
  installScrollIntoView();
  installImageData();
}

/**
 * Provides a working localStorage instance.
 *
 * Uses jsdom's native storage when available. Otherwise, installs an
 * in-memory implementation that supports key enumeration.
 */
function installLocalStorage(): void {
  let storage: Storage | undefined;

  try {
    storage = window.localStorage;

    if (
      !storage ||
      typeof storage.getItem !== 'function' ||
      typeof storage.clear !== 'function'
    ) {
      storage = undefined;
    }
  } catch {
    storage = undefined;
  }

  if (!storage) {
    const values = new Map<string, string>();

    storage = {
      get length(): number {
        return values.size;
      },

      clear(): void {
        for (const key of values.keys()) {
          delete (this as unknown as Record<string, string>)[key];
        }

        values.clear();
      },

      key(index: number): string | null {
        return Array.from(values.keys())[index] ?? null;
      },

      getItem(key: string): string | null {
        return values.get(String(key)) ?? null;
      },

      setItem(key: string, value: string): void {
        const normalizedKey = String(key);
        const normalizedValue = String(value);

        values.set(normalizedKey, normalizedValue);

        Object.defineProperty(this, normalizedKey, {
          configurable: true,
          enumerable: true,
          writable: true,
          value: normalizedValue
        });
      },

      removeItem(key: string): void {
        const normalizedKey = String(key);

        values.delete(normalizedKey);

        delete (this as unknown as Record<string, string>)[normalizedKey];
      }
    } satisfies Storage;

    Object.defineProperty(window, 'localStorage', {
      configurable: true,
      value: storage
    });
  }

  // Ensure global and window storage references point to the same instance.
  const descriptor = Object.getOwnPropertyDescriptor(
    globalThis,
    'localStorage'
  );

  if (!descriptor || descriptor.configurable) {
    Object.defineProperty(globalThis, 'localStorage', {
      configurable: true,
      writable: true,
      value: storage
    });
  } else if (globalThis.localStorage !== storage) {
    throw new Error(
      'Cannot alias a non-configurable global localStorage in Vitest.'
    );
  }
}

/**
 * Provides matchMedia for tests that check responsive layout or
 * reduced-motion preferences.
 *
 * Individual tests may override this function using vi.spyOn().
 */
function installMatchMedia(): void {
  if (typeof window.matchMedia === 'function') {
    return;
  }

  Object.defineProperty(window, 'matchMedia', {
    configurable: true,
    writable: true,
    value: (query: string): MediaQueryList => ({
      media: query,
      matches: false,
      onchange: null,
      addListener: () => {},
      removeListener: () => {},
      addEventListener: () => {},
      removeEventListener: () => {},
      dispatchEvent: () => false
    })
  });
}

/**
 * Provides scrollIntoView, which is not implemented by jsdom.
 *
 * The no-op implementation allows unit tests to execute without a
 * real browser layout engine.
 */
function installScrollIntoView(): void {
  if (
    typeof Element === 'undefined' ||
    typeof Element.prototype.scrollIntoView === 'function'
  ) {
    return;
  }

  Object.defineProperty(Element.prototype, 'scrollIntoView', {
    configurable: true,
    writable: true,
    value: function scrollIntoView(): void {}
  });
}

/**
 * Provides an ImageData-compatible container for QR decoder unit tests.
 *
 * This supports raw pixel data but does not implement HTML canvas rendering
 * or PNG decoding.
 */
function installImageData(): void {
  if (typeof globalThis.ImageData !== 'undefined') {
    return;
  }

  class TestImageData {
    readonly width: number;
    readonly height: number;
    readonly data: Uint8ClampedArray;
    readonly colorSpace = 'srgb';

    constructor(
      dataOrWidth: Uint8ClampedArray | number,
      widthOrHeight: number,
      height?: number
    ) {
      if (typeof dataOrWidth === 'number') {
        this.width = dataOrWidth;
        this.height = widthOrHeight;
        this.data = new Uint8ClampedArray(
          this.width * this.height * 4
        );
      } else {
        if (height === undefined) {
          throw new TypeError('ImageData height is required');
        }

        this.width = widthOrHeight;
        this.height = height;
        this.data = dataOrWidth;
      }

      if (
        this.width <= 0 ||
        this.height <= 0 ||
        this.data.length !== this.width * this.height * 4
      ) {
        throw new RangeError('Invalid ImageData dimensions');
      }
    }
  }

  Object.defineProperty(globalThis, 'ImageData', {
    configurable: true,
    writable: true,
    value: TestImageData
  });
}

// Initialize browser polyfills before executing any tests.
installBrowserTestPolyfills();

/**
 * Restore spied methods after every test to prevent cross-test interference.
 *
 * vi.restoreAllMocks() restores methods replaced by vi.spyOn().
 * It does not clear standalone vi.fn() service mocks.
 */
afterEach(() => {
  vi.restoreAllMocks();
});
