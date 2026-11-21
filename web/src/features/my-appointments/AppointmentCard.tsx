import { Button, Group, Stack, Text } from '@mantine/core';
import type { Appointment } from '../../api/types';
import { formatDay, formatMinutes, formatTimeRange } from '../../components/format';
import { Panel } from '../../components/layout/Panel';
import { StatusBadge } from '../../components/StatusBadge';

interface AppointmentCardProps {
  appointment: Appointment;
  onReschedule?: () => void;
  onCancel?: () => void;
}

export function AppointmentCard({
  appointment,
  onReschedule,
  onCancel,
}: Readonly<AppointmentCardProps>): React.JSX.Element {
  const isUpcoming = appointment.status === 'BOOKED';
  return (
    <Panel>
      <Stack gap="md">
        <Group justify="space-between" align="flex-start">
          <Stack gap={4}>
            <Text fw={600}>
              {formatDay(appointment.start)} · {formatTimeRange(appointment.start, appointment.end)}
            </Text>
            <Text size="sm" c="dimmed">
              {appointment.serviceName} with {appointment.staffName}
            </Text>
          </Stack>
          <StatusBadge status={appointment.status} />
        </Group>
        {isUpcoming && (
          <Group justify="space-between">
            <Text size="sm" c="dimmed">
              Estimated wait {formatMinutes(appointment.estimatedWaitMinutes)}
            </Text>
            <Group gap="sm">
              <Button variant="subtle" color="red" onClick={onCancel}>
                Cancel
              </Button>
              <Button variant="default" onClick={onReschedule}>
                Reschedule
              </Button>
            </Group>
          </Group>
        )}
      </Stack>
    </Panel>
  );
}
