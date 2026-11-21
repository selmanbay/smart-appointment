import { Group, Radio, Stack, Text } from '@mantine/core';
import type { StaffOption } from '../../api/types';
import { formatMinutes, formatTime } from '../../components/format';

/** One selectable staff member with the facts behind the recommendation (UC-18, UC-19). */
export function StaffOptionCard({ staff }: Readonly<{ staff: StaffOption }>): React.JSX.Element {
  return (
    <Radio.Card value={String(staff.staffId)} p="md">
      <Group wrap="nowrap" align="flex-start" gap="sm">
        <Radio.Indicator />
        <Stack gap="sm" flex={1}>
          <Group justify="space-between" align="flex-start" wrap="nowrap">
            <Stack gap={4} flex={1}>
              <Text fw={600}>{staff.fullName}</Text>
              <Text size="sm" c="dimmed">
                {staff.title} · next free {formatTime(staff.nextFreeTime)}
              </Text>
            </Stack>
            <Stack gap={0} align="flex-end" w="max-content">
              <Text fw={600}>{formatMinutes(staff.estimatedWaitMinutes)}</Text>
              <Text size="xs" c="dimmed">
                estimated wait
              </Text>
            </Stack>
          </Group>
          <Group justify="space-between">
            <Text size="sm" c="dimmed">
              {staff.bookedSlots} of {staff.totalSlots} slots booked
            </Text>
            {staff.meanRating !== null && (
              <Text size="sm" c="dimmed">
                Rating {staff.meanRating.toFixed(1)}
              </Text>
            )}
          </Group>
          {staff.isRecommended && (
            <Text size="sm" c="accent.7">
              Recommended: shortest estimated wait
            </Text>
          )}
        </Stack>
      </Group>
    </Radio.Card>
  );
}
