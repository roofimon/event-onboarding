"use client";

import { FormEvent, useState } from "react";
import { useRouter } from "next/navigation";
import { Guard } from "@/components/guard";
import { StepIndicator } from "@/components/step-indicator";
import { useOnboarding } from "@/components/onboarding-provider";
import { fulfill, score } from "@/lib/api";

export default function FulfillmentPage() {
  const router = useRouter();
  const { state, update } = useOnboarding();
  const [name, setName] = useState(state.name);
  const [email, setEmail] = useState(state.email);
  const [phone, setPhone] = useState(state.phone);
  const [salary, setSalary] = useState(state.salary?.toLocaleString("en-US") ?? "");
  const [years, setYears] = useState(state.yearsOfExperience?.toString() ?? "");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  function changeSalary(value: string) {
    const digits = value.replace(/\D/g, "");
    setSalary(digits ? Number(digits).toLocaleString("en-US") : "");
  }

  async function submit(event: FormEvent) {
    event.preventDefault();
    setError("");
    setLoading(true);
    const salaryValue = Number(salary.replace(/,/g, ""));
    const yearsValue = Number(years);
    try {
      await fulfill(state.applicationId!, { name, email, phone, salary: salaryValue, yearsOfExperience: yearsValue });
      const result = await score(state.applicationId!);
      update({ name, email, phone, salary: salaryValue, yearsOfExperience: yearsValue, score: result.score, approved: result.approved });
      router.push(result.approved ? "/welcome" : "/declined");
    } catch (error) {
      setError(error instanceof Error ? error.message : "Something went wrong");
    } finally {
      setLoading(false);
    }
  }

  return (
    <Guard>
      <StepIndicator current={3} />
      <p className="step-label">Step 3 of 4</p>
      <h2>Tell us about yourself</h2>
      <p className="hint">A few final details will help us review your application.</p>
      <form onSubmit={submit} className="form-grid">
        <div className="full"><label htmlFor="name">Full name</label><input id="name" value={name} onChange={(e) => setName(e.target.value)} required placeholder="Ada Lovelace" autoFocus /></div>
        <div className="full"><label htmlFor="email">Email address</label><input id="email" value={email} onChange={(e) => setEmail(e.target.value)} type="email" required /></div>
        <div className="full"><label htmlFor="phone">Phone number</label><input id="phone" value={phone} onChange={(e) => setPhone(e.target.value)} type="tel" required placeholder="+1 555 0100" /></div>
        <div><label htmlFor="salary">Annual salary</label><div className="money-input"><span>$</span><input id="salary" value={salary} onChange={(e) => changeSalary(e.target.value)} inputMode="numeric" required placeholder="120,000" /></div></div>
        <div><label htmlFor="years">Experience</label><div className="suffix-input"><input id="years" value={years} onChange={(e) => setYears(e.target.value)} type="number" min="0" step="1" required placeholder="7" /><span>years</span></div></div>
        {error && <p className="error full">{error}</p>}
        <button className="full" disabled={loading}>{loading ? "Reviewing…" : "Submit application"}</button>
      </form>
    </Guard>
  );
}
