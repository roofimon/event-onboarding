export function StepIndicator({ current }: { current: number }) {
  return (
    <div className="indicator" aria-label={`Step ${current} of 4`}>
      {[1, 2, 3, 4].map((step) => (
        <span key={step} className={step <= current ? "active" : ""} />
      ))}
    </div>
  );
}
