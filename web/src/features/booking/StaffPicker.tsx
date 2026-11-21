import { Radio, Stack } from '@mantine/core';
import type { StaffOption } from '../../api/types';
import { StaffOptionCard } from './StaffOptionCard';

interface StaffPickerProps {
  staffOptions: StaffOption[];
  value: number | null;
  onChange: (staffId: number) => void;
}

export function StaffPicker({
  staffOptions,
  value,
  onChange,
}: Readonly<StaffPickerProps>): React.JSX.Element {
  return (
    <Radio.Group
      value={value === null ? null : String(value)}
      onChange={(next) => {
        onChange(Number(next));
      }}
    >
      <Stack gap="sm">
        {staffOptions.map((staff) => (
          <StaffOptionCard key={staff.staffId} staff={staff} />
        ))}
      </Stack>
    </Radio.Group>
  );
}
