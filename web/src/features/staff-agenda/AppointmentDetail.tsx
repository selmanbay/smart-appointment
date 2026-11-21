import { Button, Group, SimpleGrid, Stack, Text, Title } from '@mantine/core';
import { canMoveTo } from '../../api/statusTransitions';
import type { Appointment, AppointmentStatus } from '../../api/types';
import { formatMinutes, formatTimeRange } from '../../components/format';
import { Panel } from '../../components/layout/Panel';
import { StatusBadge } from '../../components/StatusBadge';

const ACTIONS: { status: AppointmentStatus; label: string; color?: string; variant: string }[] = [
  { status: 'CHECKED_IN', label: 'Check in', variant: 'filled' },
  { status: 'IN_SERVICE', label: 'Start service', variant: 'default' },
  { status: 'COMPLETED', label: 'Complete', variant: 'default' },
  { status: 'NO_SHOW', label: 'Mark no-show', color: 'red', variant: 'subtle' },
];

interface AppointmentDetailProps {
  appointment: Appointment;
  isSaving: boolean;
  onChangeStatus: (status: AppointmentStatus) => void;
}

/** UC-8: record check-in, start, completion or no-show. Only transitions allowed by SRS 2.5 are enabled. */
export function AppointmentDetail({
  appointment,
  isSaving,
  onChangeStatus,
}: Readonly<AppointmentDetailProps>): React.JSX.Element {
  return (
    <Panel>
      <Stack gap="md">
        <Group justify="space-between" align="flex-start">
          <Stack gap={4}>
            <Title order={2}>{appointment.customerName}</Title>
            <Text size="sm" c="dimmed">
              Appointment #{appointment.appointmentId}
            </Text>
          </Stack>
          <StatusBadge status={appointment.status} />
        </Group>
        <SimpleGrid cols={2} spacing="sm">
          <Detail label="Service" value={appointment.serviceName} />
          <Detail label="Time" value={formatTimeRange(appointment.start, appointment.end)} />
          <Detail label="Estimated wait" value={formatMinutes(appointment.estimatedWaitMinutes)} />
        </SimpleGrid>
        <SimpleGrid cols={2} spacing="sm">
          {ACTIONS.map((action) => (
            <Button
              key={action.status}
              variant={action.variant}
              color={action.color}
              disabled={!canMoveTo(appointment.status, action.status) || isSaving}
              onClick={() => {
                onChangeStatus(action.status);
              }}
            >
              {action.label}
            </Button>
          ))}
        </SimpleGrid>
      </Stack>
    </Panel>
  );
}

function Detail({ label, value }: Readonly<{ label: string; value: string }>): React.JSX.Element {
  return (
    <Stack gap={2}>
      <Text size="xs" c="dimmed">
        {label}
      </Text>
      <Text>{value}</Text>
    </Stack>
  );
}
