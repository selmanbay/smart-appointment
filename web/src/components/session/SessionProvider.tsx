import { useMemo, useState, type ReactNode } from 'react';
import type { Account } from '../../api/types';
import { SessionContext } from './SessionContext';

/** Holds the signed-in account in memory. Replaced by JWT handling when the auth API exists (F1). */
export function SessionProvider({
  children,
}: Readonly<{ children: ReactNode }>): React.JSX.Element {
  const [account, setAccount] = useState<Account | null>(null);
  const session = useMemo(
    () => ({
      account,
      signIn: setAccount,
      signOut: () => {
        setAccount(null);
      },
    }),
    [account],
  );
  return <SessionContext.Provider value={session}>{children}</SessionContext.Provider>;
}
