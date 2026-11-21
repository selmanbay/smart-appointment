import { Chip, SimpleGrid } from '@mantine/core';
import dayjs from 'dayjs';
import { upcomingWorkingDays } from '../../api/workingDays';

/** Same grid and chip style as the time slots, so both pickers line up on every screen width. */
export function DayPicker({
  value,
  onChange,
}: Readonly<{ value: string; onChange: (day: string) => void }>): React.JSX.Element {
  return (
    <Chip.Group multiple={false} value={value} onChange={onChange}>
      <SimpleGrid cols={{ base: 4, sm: 7 }} spacing="sm">
        {upcomingWorkingDays().map((day) => (
          <Chip key={day} value={day} radius="sm" variant="outline">
            {dayjs(day).format('ddd D')}
          </Chip>
        ))}
      </SimpleGrid>
    </Chip.Group>
  );
}
