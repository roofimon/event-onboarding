"use client";

import { useRouter } from "next/navigation";
import { Guard } from "./guard";
import { StepIndicator } from "./step-indicator";
import { useOnboarding } from "./onboarding-provider";

export function Outcome({ approved }: { approved: boolean }) {
  const router = useRouter();
  const { state, reset } = useOnboarding();

  function restart() {
    reset();
    router.push("/");
  }

  return (
    <Guard>
      <StepIndicator current={4} />
      <div className={`outcome ${approved ? "approved" : "declined"}`}>
        <div className="outcome-icon">{approved ? "✓" : "!"}</div>
        <p className="step-label">Application result</p>
        <h2>{approved ? `Welcome aboard${state.name ? `, ${state.name}` : ""}!` : "Application not approved"}</h2>
        <p className="hint">{approved ? "Your application has been approved and your account is ready." : "Your score did not meet the approval threshold this time."}</p>
        <div className="score">{state.score}</div>
        <p className="score-label">Credit score · Approval requires more than 40</p>
        {approved && <button onClick={() => router.push("/login")}>Log in to your account</button>}
        <button className="secondary" onClick={restart}>{approved ? "Start a new application" : "Try again"}</button>
      </div>
    </Guard>
  );
}
