import { MantineProvider } from '@mantine/core';
import { render, type RenderResult } from '@testing-library/react';
import type { ReactElement } from 'react';
import { theme } from '../app/theme';

export function renderWithTheme(element: ReactElement): RenderResult {
  return render(<MantineProvider theme={theme}>{element}</MantineProvider>);
}
