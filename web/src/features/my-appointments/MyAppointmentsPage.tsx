import { Stack, Tabs, Text } from '@mantine/core';
import { notifications } from '@mantine/notifications';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import dayjs from 'dayjs';
import { useNavigate } from 'react-router';
import { listMyAppointments, updateStatus } from '../../api/appointmentsApi';
import type { Appointment } from '../../api/types';
import { Page } from '../../components/layout/Page';
import { useSession } from '../../components/session/SessionContext';
import { AppointmentCard } from './AppointmentCard';

function AppointmentList({
  appointments,
  emptyText,
  onCancel,
}: Readonly<{
  appointments: Appointment[];
  emptyText: string;
  onCancel?: (appointment: Appointment) => void;
}>): React.JSX.Element {
  const navigate = useNavigate();
  if (appointments.length === 0) {
    return <Text c="dimmed">{emptyText}</Text>;
  }
  return (
    <Stack gap="sm">
      {appointments.map((appointment) => (
        <AppointmentCard
          key={appointment.appointmentId}
          appointment={appointment}
          onReschedule={() => {
            void navigate('/book');
          }}
          onCancel={() => onCancel?.(appointment)}
        />
      ))}
    </Stack>
  );
}

export function MyAppointmentsPage(): React.JSX.Element {
  const { account } = useSession();
  const queryClient = useQueryClient();
  const customerName = account?.fullName ?? '';
  const appointments = useQuery({
    queryKey: ['appointments', customerName],
    queryFn: () => listMyAppointments(customerName),
  });
  const cancel = useMutation({
    mutationFn: (appointment: Appointment) => updateStatus(appointment.appointmentId, 'CANCELLED'),
    onSuccess: async () => {
      notifications.show({
        title: 'Appointment cancelled',
        message: 'The time is free for others again.',
      });
      await queryClient.invalidateQueries({ queryKey: ['appointments'] });
    },
  });
  const now = dayjs();
  const all = appointments.data ?? [];
  const upcoming = all.filter(
    (appointment) => appointment.status === 'BOOKED' && dayjs(appointment.end).isAfter(now),
  );
  const past = all.filter((appointment) => !upcoming.includes(appointment));

  return (
    <Page title="My appointments">
      <Tabs defaultValue="upcoming">
        <Tabs.List mb="lg">
          <Tabs.Tab value="upcoming">Upcoming ({upcoming.length})</Tabs.Tab>
          <Tabs.Tab value="past">Past ({past.length})</Tabs.Tab>
        </Tabs.List>
        <Tabs.Panel value="upcoming">
          <AppointmentList
            appointments={upcoming}
            emptyText="No upcoming appointments."
            onCancel={(appointment) => {
              cancel.mutate(appointment);
            }}
          />
        </Tabs.Panel>
        <Tabs.Panel value="past">
          <AppointmentList appointments={past} emptyText="No past appointments yet." />
        </Tabs.Panel>
      </Tabs>
    </Page>
  );
}
