import { Group, Stack, Text } from '@mantine/core';
import type { Appointment } from '../../api/types';
import { formatTime } from '../../components/format';
import { Panel } from '../../components/layout/Panel';

interface AgendaSummaryProps {
  total: number;
  remaining: Appointment[];
}

/** Only the numbers a staff member acts on (UI_GUIDELINES section 1). */
export function AgendaSummary({
  total,
  remaining,
}: Readonly<AgendaSummaryProps>): React.JSX.Element {
  const next = remaining[0];
  return (
    <Group gap="lg" grow>
      <Figure
        label="Appointments today"
        value={String(total)}
        detail={`${String(total - remaining.length)} done · ${String(remaining.length)} remaining`}
      />
      <Figure
        label="Next appointment"
        value={next === undefined ? 'None' : formatTime(next.start)}
        detail={next?.customerName ?? 'No more appointments today'}
      />
    </Group>
  );
}

function Figure({
  label,
  value,
  detail,
}: Readonly<{ label: string; value: string; detail: string }>): React.JSX.Element {
  return (
    <Panel>
      <Stack gap={4}>
        <Text size="sm" c="dimmed">
          {label}
        </Text>
        <Text size="xl" fw={600}>
          {value}
        </Text>
        <Text size="sm" c="dimmed">
          {detail}
        </Text>
      </Stack>
    </Panel>
  );
}
