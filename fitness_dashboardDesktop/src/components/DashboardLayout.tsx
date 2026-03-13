import type { PropsWithChildren } from "react";
import { AppSidebar, type AppView } from "./AppSidebar";

type DashboardLayoutProps = PropsWithChildren<{
  currentView: AppView;
  onChangeView: (view: AppView) => void;
  title: string;
  subtitle: string;
  dateLabel: string;
  error: string | null;
}>;

export function DashboardLayout({
  currentView,
  onChangeView,
  title,
  subtitle,
  dateLabel,
  error,
  children,
}: DashboardLayoutProps) {
  return (
    <main className="min-h-screen bg-transparent text-[#243127]">
      <div className="flex min-h-screen">
        <AppSidebar currentView={currentView} onChangeView={onChangeView} />
        <div className="min-w-0 flex-1">
          <div className="mx-auto flex min-h-screen max-w-[1480px] flex-col px-5 py-5 sm:px-6 lg:px-7">
            <header className="mb-4 flex items-center justify-between px-1">
              <div className="min-w-0">
                <h1 className="text-[26px] font-semibold tracking-tight text-[#243127]">
                  {title}
                </h1>
                <p className="mt-1 text-sm text-[#71816f]">{subtitle}</p>
              </div>
              <div className="flex items-center gap-3 text-sm">
                {error ? (
                  <span className="rounded-full border border-rose-200 bg-rose-50 px-3 py-1 text-rose-600">
                    {error}
                  </span>
                ) : null}
                <span className="text-[#849080]">{dateLabel}</span>
              </div>
            </header>
            {children}
          </div>
        </div>
      </div>
    </main>
  );
}
