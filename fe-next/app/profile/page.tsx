"use client";

import { FormEvent, useState } from "react";
import { useRouter } from "next/navigation";
import { Guard } from "@/components/guard";
import { useOnboarding } from "@/components/onboarding-provider";
import { updateProfile } from "@/lib/api";

export default function ProfilePage() {
  const router = useRouter();
  const { state, update } = useOnboarding();
  const profile = state.profile!;
  const [editing, setEditing] = useState(false);
  const [name, setName] = useState(profile?.name ?? "");
  const [phone, setPhone] = useState(profile?.phone ?? "");
  const [salary, setSalary] = useState(profile?.salary?.toLocaleString("en-US") ?? "");
  const [years, setYears] = useState(profile?.yearsOfExperience?.toString() ?? "");
  const [password, setPassword] = useState("");
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");

  function startEditing() {
    setName(profile.name);
    setPhone(profile.phone);
    setSalary(profile.salary.toLocaleString("en-US"));
    setYears(profile.yearsOfExperience.toString());
    setPassword("");
    setError("");
    setEditing(true);
  }

  async function save(event: FormEvent) {
    event.preventDefault();
    setError("");
    setSaving(true);
    try {
      const updated = await updateProfile({ email: profile.email, password, name, phone, salary: Number(salary.replace(/,/g, "")), yearsOfExperience: Number(years) });
      update({ profile: updated });
      setEditing(false);
      setPassword("");
    } catch (error) {
      setError(error instanceof Error ? error.message : "Something went wrong");
    } finally {
      setSaving(false);
    }
  }

  return (
    <Guard profile>
      <p className="step-label">Your account</p>
      <h2>Your profile</h2>
      <p className="hint">Signed in as {profile?.email}.</p>
      {!editing ? (
        <>
          <dl className="profile">
            <div><dt>Name</dt><dd>{profile?.name}</dd></div>
            <div><dt>Email</dt><dd>{profile?.email}</dd></div>
            <div><dt>Phone</dt><dd>{profile?.phone}</dd></div>
            <div><dt>Salary</dt><dd>${profile?.salary.toLocaleString("en-US")}</dd></div>
            <div><dt>Experience</dt><dd>{profile?.yearsOfExperience} years</dd></div>
          </dl>
          <button onClick={startEditing}>Edit profile</button>
          <button className="secondary" onClick={() => { update({ profile: null }); router.push("/login"); }}>Log out</button>
        </>
      ) : (
        <form onSubmit={save}>
          <label htmlFor="edit-name">Full name</label><input id="edit-name" value={name} onChange={(e) => setName(e.target.value)} required />
          <label htmlFor="edit-phone">Phone number</label><input id="edit-phone" value={phone} onChange={(e) => setPhone(e.target.value)} required />
          <label htmlFor="edit-salary">Annual salary</label><input id="edit-salary" value={salary} onChange={(e) => setSalary(e.target.value.replace(/\D/g, "") ? Number(e.target.value.replace(/\D/g, "")).toLocaleString("en-US") : "")} inputMode="numeric" required />
          <label htmlFor="edit-years">Years of experience</label><input id="edit-years" value={years} onChange={(e) => setYears(e.target.value)} type="number" min="0" required />
          <label htmlFor="edit-password">Confirm password</label><input id="edit-password" value={password} onChange={(e) => setPassword(e.target.value)} type="password" required />
          {error && <p className="error">{error}</p>}
          <button disabled={saving}>{saving ? "Saving…" : "Save changes"}</button>
          <button type="button" className="secondary" onClick={() => setEditing(false)}>Cancel</button>
        </form>
      )}
    </Guard>
  );
}
