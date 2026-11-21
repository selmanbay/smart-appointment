import { Paper } from '@mantine/core';
import type { ReactNode } from 'react';

/** A bordered surface for a standalone group of content. */
export function Panel({ children }: Readonly<{ children: ReactNode }>): React.JSX.Element {
  return (
    <Paper withBorder p="md">
      {children}
    </Paper>
  );
}
