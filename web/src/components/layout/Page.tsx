import { Container, Group, Stack, Text, Title } from '@mantine/core';
import type { ReactNode } from 'react';

interface PageProps {
  title: string;
  subtitle?: string;
  actions?: ReactNode;
  children: ReactNode;
}

/** Every screen starts with Page so titles and spacing match (UI_GUIDELINES section 5). */
export function Page({
  title,
  subtitle,
  actions,
  children,
}: Readonly<PageProps>): React.JSX.Element {
  return (
    <Container size={1200} px={0}>
      <Stack gap="lg">
        <Group justify="space-between" align="flex-start">
          <Stack gap={4}>
            <Title order={1}>{title}</Title>
            {subtitle !== undefined && <Text c="dimmed">{subtitle}</Text>}
          </Stack>
          {actions}
        </Group>
        <Stack gap="xl">{children}</Stack>
      </Stack>
    </Container>
  );
}
