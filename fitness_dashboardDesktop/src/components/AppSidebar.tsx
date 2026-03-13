export type AppView = "dashboard" | "settings";

type AppSidebarProps = {
  currentView: AppView;
  onChangeView: (view: AppView) => void;
};

const items: Array<{
  view: AppView;
  label: string;
  description: string;
  icon: string;
}> = [
  { view: "dashboard", label: "Dashboard", description: "训练概览", icon: "◫" },
  { view: "settings", label: "设置", description: "数据与补剂", icon: "⚙" },
];

export function AppSidebar({ currentView, onChangeView }: AppSidebarProps) {
  return (
    <aside className="sticky top-0 flex h-screen w-[220px] shrink-0 flex-col border-r border-[#d9dfd2] bg-[#eef2e9]/92 px-4 py-5 backdrop-blur xl:w-[232px]">
      <div className="mb-7 flex items-center gap-3 px-2 py-2">
        <div className="flex h-10 w-10 items-center justify-center rounded-2xl bg-[#8bc39a]/24 text-lg font-semibold text-[#2f6a42]">
          F
        </div>
        <div>
          <div className="text-base font-semibold tracking-tight text-[#223327]">FitBoard</div>
          <div className="text-xs text-[#7b8577]">健康记录</div>
        </div>
      </div>

      <nav className="space-y-2">
        {items.map((item) => {
          const isActive = item.view === currentView;

          return (
            <button
              key={item.view}
              type="button"
              onClick={() => onChangeView(item.view)}
              className={`flex w-full items-center gap-3 rounded-2xl border px-4 py-3 text-left transition ${
                isActive
                  ? "border-[#bfdac4] bg-[#ffffff] text-[#223327] shadow-sm"
                  : "border-transparent bg-transparent text-[#6f776a] hover:border-[#d7ded1] hover:bg-[#f7f8f3] hover:text-[#223327]"
              }`}
              aria-current={isActive ? "page" : undefined}
            >
              <span
                className={`flex h-9 w-9 items-center justify-center rounded-xl border text-sm ${
                  isActive
                    ? "border-[#cbe3cf] bg-[#edf7ef] text-[#2f6a42]"
                    : "border-[#dde4d9] bg-[#f5f7f2] text-[#75806f]"
                }`}
              >
                {item.icon}
              </span>
              <span className="min-w-0">
                <span className="block text-sm font-medium">{item.label}</span>
                <span className="block text-xs text-[#8a9285]">{item.description}</span>
              </span>
            </button>
          );
        })}
      </nav>
    </aside>
  );
}
