import {
  useMutation,
  useQuery,
  useQueryClient,
  type UseMutationResult,
  type UseQueryResult,
} from '@tanstack/react-query';
import { useState } from 'react';
import {
  bookAppointment,
  listServices,
  listSlots,
  listStaffOptions,
} from '../../api/appointmentsApi';
import type { Appointment, BookingRequest, Service, Slot, StaffOption } from '../../api/types';
import { firstWorkingDay } from '../../api/workingDays';

export interface BookingForm {
  serviceId: number | null;
  setServiceId: (serviceId: number) => void;
  day: string;
  chooseDay: (day: string) => void;
  chosenStaffId: number | null;
  chooseStaff: (staffId: number) => void;
  slotStart: string | null;
  setSlotStart: (start: string) => void;
  services: UseQueryResult<Service[]>;
  staffOptions: UseQueryResult<StaffOption[]>;
  slots: UseQueryResult<Slot[]>;
  booking: UseMutationResult<Appointment, Error, BookingRequest>;
}

/** State and data for the booking screen (UC-3). */
export function useBookingForm(): BookingForm {
  const queryClient = useQueryClient();
  const [serviceId, setServiceId] = useState<number | null>(1);
  const [day, setDay] = useState(firstWorkingDay);
  const [staffId, setStaffId] = useState<number | null>(null);
  const [slotStart, setSlotStart] = useState<string | null>(null);

  const services = useQuery({ queryKey: ['services'], queryFn: listServices });
  const staffOptions = useQuery({
    queryKey: ['staff-options', serviceId, day],
    queryFn: () => listStaffOptions(day),
    enabled: serviceId !== null,
  });
  const recommendedStaffId =
    staffOptions.data?.find((staff) => staff.isRecommended)?.staffId ?? null;
  const chosenStaffId = staffId ?? recommendedStaffId;
  const slots = useQuery({
    queryKey: ['slots', chosenStaffId, day],
    queryFn: () => listSlots(chosenStaffId ?? 0, day),
    enabled: chosenStaffId !== null,
  });
  const booking = useMutation({
    mutationFn: bookAppointment,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['appointments'] }),
  });

  const chooseDay = (nextDay: string): void => {
    setDay(nextDay);
    setSlotStart(null);
  };
  const chooseStaff = (nextStaffId: number): void => {
    setStaffId(nextStaffId);
    setSlotStart(null);
  };

  return {
    serviceId,
    setServiceId,
    day,
    chooseDay,
    chosenStaffId,
    chooseStaff,
    slotStart,
    setSlotStart,
    services,
    staffOptions,
    slots,
    booking,
  };
}
