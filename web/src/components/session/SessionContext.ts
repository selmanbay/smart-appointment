import { createContext, useContext } from 'react';
import type { Account } from '../../api/types';

export interface Session {
  account: Account | null;
  signIn: (account: Account) => void;
  signOut: () => void;
}

export const SessionContext = createContext<Session | null>(null);

export function useSession(): Session {
  const session = useContext(SessionContext);
  if (session === null) {
    throw new Error('useSession must be used inside SessionProvider');
  }
  return session;
}
