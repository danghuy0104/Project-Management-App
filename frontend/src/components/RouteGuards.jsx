import { Navigate, Outlet, useLocation } from "react-router-dom";
import { useSelector } from "react-redux";
import { Loader2Icon } from "lucide-react";

const Spinner = () => (
    <div className="flex items-center justify-center h-screen bg-white dark:bg-zinc-950">
        <Loader2Icon className="size-7 text-blue-500 animate-spin" />
    </div>
);

/** Restrict pages that require login: if there is no token, redirect to /login while remembering the page being visited so the user can return later. */
export function ProtectedRoute({ children }) {
    const { token, status } = useSelector((state) => state.auth);
    const location = useLocation();

    if (!token || status === "unauthenticated") {
        return <Navigate to="/login" replace state={{ from: location }} />;
    }
    if (status === "loading") return <Spinner />;
    return children;
}

/** Pages /login and /register: if the user is already logged in, redirect them back to the previous page (or Dashboard). */
export function GuestRoute() {
    const { token, status } = useSelector((state) => state.auth);
    const location = useLocation();

    if (token && status === "authenticated") {
        const from = location.state?.from;
        const target = from ? `${from.pathname}${from.search || ""}` : "/";
        return <Navigate to={target} replace />;
    }
    if (token && status === "loading") return <Spinner />;
    return <Outlet />;
}
