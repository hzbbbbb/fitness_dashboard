import React from "react";
import ReactDOM from "react-dom/client";
import App from "./App";
import { ErrorBoundary } from "./components/ErrorBoundary";
import "./styles.css";

ReactDOM.createRoot(document.getElementById("root") as HTMLElement).render(
  <React.StrictMode>
    <ErrorBoundary
      title="FitBoard 启动异常"
      description="应用发生运行时错误，页面已被错误边界接管，避免直接黑屏。"
    >
      <App />
    </ErrorBoundary>
  </React.StrictMode>,
);
