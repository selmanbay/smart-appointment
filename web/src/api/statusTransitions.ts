import type { AppointmentStatus } from './types';

/** Allowed status changes from SRS 2.5 (Activity 2 in the SDD). */
export const NEXT_STATUSES: Record<AppointmentStatus, AppointmentStatus[]> = {
  BOOKED: ['CHECKED_IN', 'CANCELLED', 'NO_SHOW'],
  CHECKED_IN: ['IN_SERVICE'],
  IN_SERVICE: ['COMPLETED'],
  COMPLETED: [],
  CANCELLED: [],
  NO_SHOW: [],
};

export function canMoveTo(current: AppointmentStatus, next: AppointmentStatus): boolean {
  return NEXT_STATUSES[current].includes(next);
}
