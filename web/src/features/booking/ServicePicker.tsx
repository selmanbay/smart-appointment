import { Group, Radio, Stack, Text } from '@mantine/core';
import type { Service } from '../../api/types';

interface ServicePickerProps {
  services: Service[];
  value: number | null;
  onChange: (serviceId: number) => void;
}

export function ServicePicker({
  services,
  value,
  onChange,
}: Readonly<ServicePickerProps>): React.JSX.Element {
  return (
    <Radio.Group
      value={value === null ? null : String(value)}
      onChange={(next) => {
        onChange(Number(next));
      }}
    >
      <Stack gap="sm">
        {services.map((service) => (
          <Radio.Card key={service.serviceId} value={String(service.serviceId)} p="md">
            <Group wrap="nowrap" align="flex-start" gap="sm">
              <Radio.Indicator />
              <Stack gap={4}>
                <Text fw={600}>{service.name}</Text>
                <Text size="sm" c="dimmed">
                  {service.description} · {service.durationMinutes} min
                </Text>
              </Stack>
            </Group>
          </Radio.Card>
        ))}
      </Stack>
    </Radio.Group>
  );
}
