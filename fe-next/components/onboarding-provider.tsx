"use client";

import { createContext, useContext, useEffect, useState } from "react";
import type { OnboardingState } from "@/lib/types";

const emptyState: OnboardingState = {
  applicationId: null,
  email: "",
  name: "",
  phone: "",
  salary: null,
  yearsOfExperience: null,
  score: null,
  approved: null,
  profile: null,
};

type ContextValue = {
  state: OnboardingState;
  ready: boolean;
  update: (values: Partial<OnboardingState>) => void;
  reset: () => void;
};

const OnboardingContext = createContext<ContextValue | null>(null);

export function OnboardingProvider({ children }: { children: React.ReactNode }) {
  const [state, setState] = useState(emptyState);
  const [ready, setReady] = useState(false);

  useEffect(() => {
    const saved = sessionStorage.getItem("onboarding");
    if (saved) {
      try {
        // Browser storage is only available after hydration.
        // eslint-disable-next-line react-hooks/set-state-in-effect
        setState({ ...emptyState, ...JSON.parse(saved) });
      } catch {
        sessionStorage.removeItem("onboarding");
      }
    }
    setReady(true);
  }, []);

  useEffect(() => {
    if (ready) sessionStorage.setItem("onboarding", JSON.stringify(state));
  }, [ready, state]);

  return (
    <OnboardingContext.Provider
      value={{
        state,
        ready,
        update: (values) => setState((current) => ({ ...current, ...values })),
        reset: () => setState(emptyState),
      }}
    >
      {children}
    </OnboardingContext.Provider>
  );
}

export function useOnboarding() {
  const context = useContext(OnboardingContext);
  if (!context) throw new Error("useOnboarding must be used inside OnboardingProvider");
  return context;
}
