import { Component, type ErrorInfo, type PropsWithChildren, type ReactNode } from "react";

type ErrorBoundaryProps = PropsWithChildren<{
  title?: string;
  description?: string;
}>;

type ErrorBoundaryState = {
  hasError: boolean;
};

export class ErrorBoundary extends Component<ErrorBoundaryProps, ErrorBoundaryState> {
  state: ErrorBoundaryState = {
    hasError: false,
  };

  static getDerivedStateFromError(): ErrorBoundaryState {
    return { hasError: true };
  }

  componentDidCatch(error: Error, errorInfo: ErrorInfo) {
    console.error("FitBoard render error:", error, errorInfo);
  }

  render(): ReactNode {
    if (this.state.hasError) {
      return (
        <section className="rounded-3xl border border-rose-400/20 bg-rose-500/10 p-5 text-rose-50 shadow-xl shadow-black/20">
          <p className="text-xs uppercase tracking-[0.28em] text-rose-200/80">Render Error</p>
          <h2 className="mt-2 text-xl font-semibold">
            {this.props.title ?? "模块渲染失败"}
          </h2>
          <p className="mt-2 text-sm text-rose-100/85">
            {this.props.description ?? "该区域已降级显示，请刷新页面或继续使用其他功能。"}
          </p>
        </section>
      );
    }

    return this.props.children;
  }
}
