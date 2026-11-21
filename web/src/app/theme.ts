import { createTheme, type MantineColorsTuple } from '@mantine/core';
import classes from './theme.module.css';

// The only place for visual values (docs/design/UI_GUIDELINES.md, ADR-0013).
const accent: MantineColorsTuple = [
  '#e6f1ec',
  '#cfe3da',
  '#a6cbb9',
  '#7bb197',
  '#56987a',
  '#3c8466',
  '#2c775b',
  '#1f6b52',
  '#165541',
  '#0d3f30',
];

export const theme = createTheme({
  primaryColor: 'accent',
  primaryShade: 7,
  colors: { accent },
  black: '#1c2426',
  fontFamily: '"IBM Plex Sans", system-ui, sans-serif',
  fontFamilyMonospace: 'ui-monospace, SFMono-Regular, Menlo, monospace',
  defaultRadius: 'sm',
  fontSizes: { xs: '12px', sm: '13px', md: '15px', lg: '18px', xl: '24px' },
  headings: {
    fontFamily: '"IBM Plex Sans", system-ui, sans-serif',
    fontWeight: '600',
    sizes: {
      h1: { fontSize: '24px', lineHeight: '1.3' },
      h2: { fontSize: '18px', lineHeight: '1.35' },
      h3: { fontSize: '15px', lineHeight: '1.4' },
    },
  },
  other: { canvas: '#f6f7f6', line: '#dfe4e3', dimmed: '#5b6669' },
  components: {
    Button: { defaultProps: { radius: 'sm' } },
    Paper: { defaultProps: { radius: 'sm' } },
    Badge: { defaultProps: { radius: 'sm', variant: 'light' } },
    RadioCard: { classNames: { card: classes.radioCard } },
    Chip: { classNames: { root: classes.chipRoot, label: classes.chipLabel } },
  },
});
