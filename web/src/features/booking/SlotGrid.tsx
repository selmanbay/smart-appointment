import { Chip, SimpleGrid, Text } from '@mantine/core';
import type { Slot } from '../../api/types';
import { formatTime } from '../../components/format';

interface SlotGridProps {
  slots: Slot[];
  selectedStart: string | null;
  onSelect: (start: string) => void;
}

/** Free slots only; taken or break times are not shown (UI_GUIDELINES section 3). */
export function SlotGrid({
  slots,
  selectedStart,
  onSelect,
}: Readonly<SlotGridProps>): React.JSX.Element {
  if (slots.length === 0) {
    return <Text c="dimmed">No free slots on this day. Try another date.</Text>;
  }
  return (
    <Chip.Group multiple={false} value={selectedStart} onChange={onSelect}>
      <SimpleGrid cols={{ base: 3, sm: 4 }} spacing="sm">
        {slots.map((slot) => (
          <Chip key={slot.start} value={slot.start} radius="sm" variant="outline">
            {formatTime(slot.start)}
          </Chip>
        ))}
      </SimpleGrid>
    </Chip.Group>
  );
}
