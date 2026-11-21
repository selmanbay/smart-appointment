// Lint rules for the web app. Limits and reasons: docs/decisions/0007-code-quality-gates.md
// Required dev dependencies (added with the Vite skeleton):
//   eslint @eslint/js typescript-eslint eslint-plugin-react-hooks eslint-plugin-react-refresh
//   eslint-plugin-sonarjs eslint-plugin-boundaries eslint-import-resolver-typescript
//   eslint-config-prettier globals
import js from '@eslint/js';
import tseslint from 'typescript-eslint';
import reactHooks from 'eslint-plugin-react-hooks';
import reactRefresh from 'eslint-plugin-react-refresh';
import sonarjs from 'eslint-plugin-sonarjs';
import boundaries from 'eslint-plugin-boundaries';
import prettier from 'eslint-config-prettier';
import globals from 'globals';

export default tseslint.config(
  { ignores: ['dist', 'coverage', 'src/api/generated/**'] },
  {
    files: ['src/**/*.{ts,tsx}'],
    extends: [
      js.configs.recommended,
      ...tseslint.configs.strictTypeChecked,
      ...tseslint.configs.stylisticTypeChecked,
      sonarjs.configs.recommended,
      prettier,
    ],
    languageOptions: {
      ecmaVersion: 2022,
      globals: globals.browser,
      parserOptions: { projectService: true, tsconfigRootDir: import.meta.dirname },
    },
    plugins: {
      'react-hooks': reactHooks,
      'react-refresh': reactRefresh,
      boundaries,
    },
    settings: {
      'import/resolver': { typescript: true },
      'boundaries/elements': [
        { type: 'app', pattern: 'src/app/**' },
        { type: 'feature', pattern: 'src/features/*', capture: ['feature'] },
        { type: 'components', pattern: 'src/components/**' },
        { type: 'api', pattern: 'src/api/**' },
      ],
    },
    rules: {
      ...reactHooks.configs.recommended.rules,

      // Feature boundaries (ADR-0001): a feature imports only itself, shared components and the API client.
      'boundaries/element-types': [
        'error',
        {
          default: 'disallow',
          rules: [
            { from: 'app', allow: ['app', 'feature', 'components', 'api'] },
            { from: 'feature', allow: [['feature', { feature: '${from.feature}' }], 'components', 'api'] },
            { from: 'components', allow: ['components'] },
            { from: 'api', allow: ['api'] },
          ],
        },
      ],

      // Size and complexity limits (AGENTS.md section 11).
      complexity: ['error', 10],
      'max-depth': ['error', 3],
      'max-params': ['error', 4],
      'max-lines-per-function': ['error', { max: 50, skipBlankLines: true, skipComments: true }],
      'max-lines': ['error', { max: 300, skipBlankLines: true, skipComments: true }],
      'max-nested-callbacks': ['error', 3],
      'sonarjs/cognitive-complexity': ['error', 12],

      // Naming (AGENTS.md section 12).
      '@typescript-eslint/naming-convention': [
        'error',
        { selector: 'default', format: ['camelCase'], leadingUnderscore: 'allow' },
        { selector: 'variable', format: ['camelCase', 'UPPER_CASE', 'PascalCase'] },
        { selector: 'function', format: ['camelCase', 'PascalCase'] },
        { selector: 'typeLike', format: ['PascalCase'] },
        { selector: 'enumMember', format: ['PascalCase', 'UPPER_CASE'] },
        {
          selector: 'variable',
          types: ['boolean'],
          format: ['PascalCase'],
          prefix: ['is', 'has', 'can', 'should', 'was', 'did'],
        },
        { selector: 'objectLiteralProperty', format: null },
        { selector: 'import', format: null },
      ],
      'id-length': ['error', { min: 2, exceptions: ['i', 'j', '_'], properties: 'never' }],
      'id-denylist': ['error', 'data', 'info', 'item', 'obj', 'temp', 'tmp', 'foo', 'bar', 'stuff', 'thing', 'helper', 'manager', 'util', 'utils'],

      // Single responsibility per module.
      'max-classes-per-file': ['error', 1],
      'react-refresh/only-export-components': ['error', { allowConstantExport: true }],

      // Hygiene.
      'no-console': 'error',
      'no-debugger': 'error',
      'no-warning-comments': ['error', { terms: ['fixme', 'xxx', 'hack'] }],
      'no-nested-ternary': 'error',
      'no-param-reassign': 'error',
      'prefer-const': 'error',
      eqeqeq: ['error', 'always'],
      '@typescript-eslint/no-explicit-any': 'error',
      '@typescript-eslint/no-non-null-assertion': 'error',
      '@typescript-eslint/no-unused-vars': ['error', { argsIgnorePattern: '^_' }],
      '@typescript-eslint/explicit-module-boundary-types': 'error',
      '@typescript-eslint/switch-exhaustiveness-check': 'error',
      '@typescript-eslint/ban-ts-comment': 'error',
    },
    linterOptions: { reportUnusedDisableDirectives: 'error', noInlineConfig: true },
  },
  {
    files: ['src/**/*.test.{ts,tsx}'],
    rules: { 'max-lines-per-function': 'off', 'max-nested-callbacks': 'off' },
  },
);
