import type { Metadata } from "next";
import { OnboardingProvider } from "@/components/onboarding-provider";
import "./globals.css";

export const metadata: Metadata = {
  title: "Event Onboarding",
  description: "Apply for an account in a few simple steps.",
};

export default function RootLayout({ children }: Readonly<{ children: React.ReactNode }>) {
  return (
    <html lang="en">
      <body>
        <OnboardingProvider>
          <div className="page">
            <header className="header">
              <p className="eyebrow">A better way to get started</p>
              <h1>Event Onboarding</h1>
            </header>
            <main className="card">{children}</main>
            <p className="footer">Secure application · Takes about 3 minutes</p>
          </div>
        </OnboardingProvider>
      </body>
    </html>
  );
}
