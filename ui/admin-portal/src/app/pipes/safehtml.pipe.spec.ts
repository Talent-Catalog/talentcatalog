import { SafehtmlPipe } from './safehtml.pipe';

describe('SafehtmlPipe', () => {
  it('create an instance', () => {
    const pipe = new SafehtmlPipe();
    expect(pipe).toBeTruthy();
  });
});
