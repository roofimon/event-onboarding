"use client";

import { FormEvent, useState } from "react";
import { useRouter } from "next/navigation";
import { Guard } from "@/components/guard";
import { StepIndicator } from "@/components/step-indicator";
import { useOnboarding } from "@/components/onboarding-provider";
import { verifyToken } from "@/lib/api";

export default function VerifyPage() {
  const router = useRouter();
  const { state } = useOnboarding();
  const [token, setToken] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  async function submit(event: FormEvent) {
    event.preventDefault();
    setError("");
    setLoading(true);
    try {
      await verifyToken(state.applicationId!, token);
      router.push("/fulfillment");
    } catch (error) {
      setError(error instanceof Error ? error.message : "Something went wrong");
    } finally {
      setLoading(false);
    }
  }

  return (
    <Guard>
      <StepIndicator current={2} />
      <p className="step-label">Step 2 of 4</p>
      <h2>Check your verification token</h2>
      <p className="hint">Enter the 6-digit token printed in the server console.</p>
      <form onSubmit={submit}>
        <label htmlFor="token">Verification token</label>
        <input id="token" value={token} onChange={(e) => setToken(e.target.value.replace(/\D/g, "").slice(0, 6))} inputMode="numeric" pattern="\d{6}" required placeholder="000000" autoComplete="one-time-code" autoFocus />
        {error && <p className="error">{error}</p>}
        <button disabled={loading}>{loading ? "Verifying…" : "Verify & continue"}</button>
      </form>
    </Guard>
  );
}
