import { notifications } from '@mantine/notifications';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useState } from 'react';
import { listAgenda, updateStatus } from '../../api/appointmentsApi';
import type { Appointment, AppointmentStatus } from '../../api/types';
import { firstWorkingDay } from '../../api/workingDays';

export interface AgendaState {
  day: string;
  appointments: Appointment[];
  remaining: Appointment[];
  selected: Appointment | undefined;
  setSelectedId: (appointmentId: number) => void;
  changeStatus: (status: AppointmentStatus) => void;
  isSaving: boolean;
}

const OPEN_STATUSES: AppointmentStatus[] = ['BOOKED', 'CHECKED_IN', 'IN_SERVICE'];

/** Today's appointments for the signed-in staff member and the status change action (UC-8). */
export function useAgenda(): AgendaState {
  const queryClient = useQueryClient();
  const today = firstWorkingDay();
  const [selectedId, setSelectedId] = useState<number | null>(null);
  const agenda = useQuery({ queryKey: ['agenda', today], queryFn: () => listAgenda(today) });
  const statusChange = useMutation({
    mutationFn: (change: { appointmentId: number; status: AppointmentStatus }) =>
      updateStatus(change.appointmentId, change.status),
    onSuccess: async () => {
      notifications.show({
        title: 'Status updated',
        message: 'The time was recorded for waiting-time estimates.',
      });
      await queryClient.invalidateQueries({ queryKey: ['agenda'] });
    },
  });

  const appointments: Appointment[] = agenda.data ?? [];
  const remaining = appointments.filter((appointment) =>
    OPEN_STATUSES.includes(appointment.status),
  );
  const selected =
    appointments.find((appointment) => appointment.appointmentId === selectedId) ?? remaining[0];
  const changeStatus = (status: AppointmentStatus): void => {
    if (selected !== undefined) {
      statusChange.mutate({ appointmentId: selected.appointmentId, status });
    }
  };

  return {
    day: today,
    appointments,
    remaining,
    selected,
    setSelectedId,
    changeStatus,
    isSaving: statusChange.isPending,
  };
}
