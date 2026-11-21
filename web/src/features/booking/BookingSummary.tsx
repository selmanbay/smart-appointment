import { Button, Group, Stack, Text } from '@mantine/core';
import { formatDay, formatTime } from '../../components/format';
import { Panel } from '../../components/layout/Panel';

interface BookingSummaryProps {
  slotStart: string | null;
  serviceName: string | undefined;
  staffName: string | undefined;
  isSaving: boolean;
  onConfirm: () => void;
}

export function BookingSummary(props: Readonly<BookingSummaryProps>): React.JSX.Element {
  const heading =
    props.slotStart === null
      ? 'Pick a time to continue'
      : `${formatDay(props.slotStart)} · ${formatTime(props.slotStart)}`;
  return (
    <Panel>
      <Stack gap="md">
        <Stack gap={4}>
          <Text fw={600}>{heading}</Text>
          <Text size="sm" c="dimmed">
            {props.serviceName} with {props.staffName}
          </Text>
        </Stack>
        <Group justify="flex-end">
          <Button
            onClick={props.onConfirm}
            disabled={props.slotStart === null}
            loading={props.isSaving}
          >
            Confirm appointment
          </Button>
        </Group>
      </Stack>
    </Panel>
  );
}
