// The only place that reads the system clock (BUILD_PLAN 5.1). Tests replace it with setClock().
const systemClock = (): Date => new Date();
let source = systemClock;

export function now(): Date {
  return source();
}

export function setClock(fn: () => Date): void {
  source = fn;
}

export function resetClock(): void {
  source = systemClock;
}
