import { Group, Stack, Text, Title } from '@mantine/core';
import type { ReactNode } from 'react';

interface SectionProps {
  title: string;
  aside?: string;
  children: ReactNode;
}

export function Section({ title, aside, children }: Readonly<SectionProps>): React.JSX.Element {
  return (
    <Stack gap="sm">
      <Group justify="space-between">
        <Title order={2}>{title}</Title>
        {aside !== undefined && (
          <Text size="sm" c="dimmed">
            {aside}
          </Text>
        )}
      </Group>
      {children}
    </Stack>
  );
}
