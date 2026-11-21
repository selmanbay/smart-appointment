// Domain types shared by the API client and the screens. They mirror api/openapi.yaml and SRS Table 9.

export type Role = 'CUSTOMER' | 'STAFF' | 'ORGANIZATION_OWNER' | 'SYSTEM_ADMIN';

export type AppointmentStatus =
  'BOOKED' | 'CHECKED_IN' | 'IN_SERVICE' | 'COMPLETED' | 'CANCELLED' | 'NO_SHOW';

export interface Account {
  userId: number;
  fullName: string;
  role: Role;
  organizationName: string;
}

export interface Service {
  serviceId: number;
  name: string;
  description: string;
  durationMinutes: number;
}

export interface StaffOption {
  staffId: number;
  fullName: string;
  title: string;
  nextFreeTime: string;
  estimatedWaitMinutes: number;
  bookedSlots: number;
  totalSlots: number;
  meanRating: number | null;
  isRecommended: boolean;
}

export interface Slot {
  start: string;
  end: string;
  remainingCapacity: number;
}

export interface Appointment {
  appointmentId: number;
  serviceName: string;
  staffName: string;
  customerName: string;
  start: string;
  end: string;
  status: AppointmentStatus;
  estimatedWaitMinutes: number;
}

export interface BookingRequest {
  serviceId: number;
  staffId: number;
  start: string;
}
