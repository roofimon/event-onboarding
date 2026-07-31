"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";
import { useOnboarding } from "./onboarding-provider";

export function Guard({
  children,
  profile = false,
}: {
  children: React.ReactNode;
  profile?: boolean;
}) {
  const router = useRouter();
  const { state, ready } = useOnboarding();
  const allowed = profile ? Boolean(state.profile) : Boolean(state.applicationId);

  useEffect(() => {
    if (ready && !allowed) router.replace(profile ? "/login" : "/");
  }, [allowed, profile, ready, router]);

  if (!ready || !allowed) return <p className="hint">Loading…</p>;
  return children;
}
