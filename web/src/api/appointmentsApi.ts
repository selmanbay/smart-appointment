import dayjs from 'dayjs';
import { APPOINTMENTS, SERVICES, STAFF } from './mockStore';
import type {
  Appointment,
  AppointmentStatus,
  BookingRequest,
  Service,
  Slot,
  StaffOption,
} from './types';

// Prototype API backed by mockStore. Each function keeps the signature of the planned REST call,
// so screens do not change when it is switched to fetch() against /api/v1.

const SLOT_MINUTES = 30;
const DAY_START_HOUR = 9;
const SLOTS_PER_DAY = 14;
const WEEKEND_DAYS = [0, 6];

export async function listServices(): Promise<Service[]> {
  return Promise.resolve(SERVICES);
}

/** UC-18: staff ranked by estimated wait, ties broken by mean rating. */
export async function listStaffOptions(date: string): Promise<StaffOption[]> {
  const ranked = [...STAFF].sort(
    (first, second) =>
      first.estimatedWaitMinutes - second.estimatedWaitMinutes ||
      (second.meanRating ?? 0) - (first.meanRating ?? 0),
  );
  const firstSlot = dayjs(date).hour(DAY_START_HOUR).minute(0);
  return Promise.resolve(
    ranked.map((staff, index) => ({
      ...staff,
      nextFreeTime: firstSlot.add(index * SLOT_MINUTES, 'minute').toISOString(),
      isRecommended: index === 0,
    })),
  );
}

/** UC-3 step 4: free slots of one staff member on one day (SRS Eq. 1). */
export async function listSlots(staffId: number, date: string): Promise<Slot[]> {
  if (WEEKEND_DAYS.includes(dayjs(date).day())) {
    return Promise.resolve([]);
  }
  const dayStart = dayjs(date).hour(DAY_START_HOUR).minute(0);
  const slots = Array.from({ length: SLOTS_PER_DAY }, (_, index) => {
    const start = dayStart.add(index * SLOT_MINUTES, 'minute');
    return {
      start: start.toISOString(),
      end: start.add(SLOT_MINUTES, 'minute').toISOString(),
      remainingCapacity: 1,
    };
  });
  const lunchBreak = (slot: Slot): boolean => dayjs(slot.start).hour() === 12;
  const takenByStaff = (index: number): boolean => (index + staffId) % 4 === 0;
  return Promise.resolve(slots.filter((slot, index) => !lunchBreak(slot) && !takenByStaff(index)));
}

export async function bookAppointment(request: BookingRequest): Promise<Appointment> {
  const service = SERVICES.find((candidate) => candidate.serviceId === request.serviceId);
  const staff = STAFF.find((candidate) => candidate.staffId === request.staffId);
  const created: Appointment = {
    appointmentId: Math.max(...APPOINTMENTS.map((existing) => existing.appointmentId)) + 1,
    serviceName: service?.name ?? 'Service',
    staffName: staff?.fullName ?? 'Staff',
    customerName: 'Alex Morgan',
    start: request.start,
    end: dayjs(request.start)
      .add(service?.durationMinutes ?? SLOT_MINUTES, 'minute')
      .toISOString(),
    status: 'BOOKED',
    estimatedWaitMinutes: staff?.estimatedWaitMinutes ?? 0,
  };
  APPOINTMENTS.push(created);
  return Promise.resolve(created);
}

export async function listMyAppointments(customerName: string): Promise<Appointment[]> {
  return Promise.resolve(
    APPOINTMENTS.filter((appointment) => appointment.customerName === customerName).sort(
      (first, second) => first.start.localeCompare(second.start),
    ),
  );
}

export async function listAgenda(date: string): Promise<Appointment[]> {
  const day = dayjs(date);
  return Promise.resolve(
    APPOINTMENTS.filter((appointment) => dayjs(appointment.start).isSame(day, 'day')).sort(
      (first, second) => first.start.localeCompare(second.start),
    ),
  );
}

export async function updateStatus(
  appointmentId: number,
  status: AppointmentStatus,
): Promise<Appointment> {
  const target = APPOINTMENTS.find((appointment) => appointment.appointmentId === appointmentId);
  if (target === undefined) {
    return Promise.reject(new Error(`Appointment ${String(appointmentId)} was not found`));
  }
  target.status = status;
  return Promise.resolve(target);
}
