import {defineConfig} from 'vitest/config';

// Test-only isolation. Angular's unit-test builder otherwise shares a module
// graph across test files, which can make vi.mock() dependent on test order.
export default defineConfig({
  test: {
    isolate: true
  }
});
