import { describe, expect, it } from 'vitest';
import { canMoveTo } from './statusTransitions';

describe('appointment status transitions (SRS 2.5)', () => {
  it('allows the forward path from booked to completed', () => {
    expect(canMoveTo('BOOKED', 'CHECKED_IN')).toBe(true);
    expect(canMoveTo('CHECKED_IN', 'IN_SERVICE')).toBe(true);
    expect(canMoveTo('IN_SERVICE', 'COMPLETED')).toBe(true);
  });

  it('allows cancel and no-show only from booked', () => {
    expect(canMoveTo('BOOKED', 'NO_SHOW')).toBe(true);
    expect(canMoveTo('CHECKED_IN', 'NO_SHOW')).toBe(false);
    expect(canMoveTo('IN_SERVICE', 'CANCELLED')).toBe(false);
  });

  it('does not allow skipping steps or leaving a final status', () => {
    expect(canMoveTo('BOOKED', 'COMPLETED')).toBe(false);
    expect(canMoveTo('COMPLETED', 'BOOKED')).toBe(false);
  });
});
