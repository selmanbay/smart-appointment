import { Group, Stack, Text, UnstyledButton } from '@mantine/core';
import type { Appointment } from '../../api/types';
import { formatTime } from '../../components/format';
import { Panel } from '../../components/layout/Panel';
import { StatusBadge } from '../../components/StatusBadge';

interface AgendaListProps {
  appointments: Appointment[];
  selectedId: number | null;
  onSelect: (appointmentId: number) => void;
}

export function AgendaList({
  appointments,
  selectedId,
  onSelect,
}: Readonly<AgendaListProps>): React.JSX.Element {
  return (
    <Panel>
      <Stack gap="xs">
        {appointments.map((appointment) => (
          <UnstyledButton
            key={appointment.appointmentId}
            onClick={() => {
              onSelect(appointment.appointmentId);
            }}
            p="sm"
            bg={appointment.appointmentId === selectedId ? 'accent.0' : undefined}
            aria-pressed={appointment.appointmentId === selectedId}
          >
            <Group justify="space-between" wrap="nowrap">
              <Group gap="md" wrap="nowrap">
                <Text ff="monospace" size="sm" c="dimmed">
                  {formatTime(appointment.start)}
                </Text>
                <Stack gap={0}>
                  <Text fw={500}>{appointment.customerName}</Text>
                  <Text size="sm" c="dimmed">
                    {appointment.serviceName}
                  </Text>
                </Stack>
              </Group>
              <StatusBadge status={appointment.status} />
            </Group>
          </UnstyledButton>
        ))}
      </Stack>
    </Panel>
  );
}
