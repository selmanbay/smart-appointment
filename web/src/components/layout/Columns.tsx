import { SimpleGrid } from '@mantine/core';
import type { ReactNode } from 'react';

/** Two columns on desktop, one below 900px. */
export function Columns({ children }: Readonly<{ children: ReactNode }>): React.JSX.Element {
  return (
    <SimpleGrid cols={{ base: 1, md: 2 }} spacing="lg">
      {children}
    </SimpleGrid>
  );
}
