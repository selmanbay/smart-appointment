import dayjs from 'dayjs';
import type { Account, Appointment, Service, StaffOption } from './types';
import { firstWorkingDay } from './workingDays';

// In-memory sample data used until the backend endpoints exist (ADR-0012).
// Times are generated around today so the prototype always shows a realistic day.

const ORGANIZATION = 'Northbridge University · Student Services';

export const DEMO_ACCOUNTS: Account[] = [
  { userId: 1, fullName: 'Alex Morgan', role: 'CUSTOMER', organizationName: ORGANIZATION },
  { userId: 2, fullName: 'Maya Chen', role: 'STAFF', organizationName: ORGANIZATION },
];

export const SERVICES: Service[] = [
  {
    serviceId: 1,
    name: 'Academic advising',
    description: 'Course planning and registration',
    durationMinutes: 30,
  },
  {
    serviceId: 2,
    name: 'Document request',
    description: 'Transcripts and enrolment letters',
    durationMinutes: 15,
  },
];

export const STAFF: Omit<StaffOption, 'nextFreeTime' | 'isRecommended'>[] = [
  {
    staffId: 1,
    fullName: 'Maya Chen',
    title: 'Academic advisor',
    estimatedWaitMinutes: 5,
    bookedSlots: 6,
    totalSlots: 14,
    meanRating: 4.7,
  },
  {
    staffId: 2,
    fullName: 'Daniel Brooks',
    title: 'Senior advisor',
    estimatedWaitMinutes: 15,
    bookedSlots: 11,
    totalSlots: 14,
    meanRating: 4.8,
  },
];

function today(hour: number, minute: number): dayjs.Dayjs {
  return dayjs(firstWorkingDay()).hour(hour).minute(minute);
}

function appointment(
  appointmentId: number,
  customerName: string,
  start: dayjs.Dayjs,
  status: Appointment['status'],
): Appointment {
  return {
    appointmentId,
    serviceName: 'Academic advising',
    staffName: 'Maya Chen',
    customerName,
    start: start.toISOString(),
    end: start.add(30, 'minute').toISOString(),
    status,
    estimatedWaitMinutes: status === 'BOOKED' ? 5 : 0,
  };
}

export const APPOINTMENTS: Appointment[] = [
  appointment(101, 'Sofia Martin', today(9, 0), 'COMPLETED'),
  appointment(102, 'James Wilson', today(9, 30), 'COMPLETED'),
  appointment(103, 'Alex Morgan', today(10, 0), 'BOOKED'),
  appointment(104, 'Olivia Bennett', today(10, 30), 'CHECKED_IN'),
  appointment(105, 'Ethan Parker', today(11, 30), 'BOOKED'),
  appointment(106, 'Alex Morgan', today(9, 30).add(3, 'day'), 'BOOKED'),
  appointment(107, 'Alex Morgan', today(14, 0).subtract(6, 'day'), 'COMPLETED'),
];
