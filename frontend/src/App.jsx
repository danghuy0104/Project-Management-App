import { useEffect } from "react";
import { Routes, Route, Navigate } from "react-router-dom";
import { useDispatch, useSelector } from "react-redux";
import Layout from "./pages/Layout";
import toast, { Toaster } from "react-hot-toast";
import Dashboard from "./pages/Dashboard";
import Projects from "./pages/Projects";
import Team from "./pages/Team";
import ProjectDetails from "./pages/ProjectDetails";
import TaskDetails from "./pages/TaskDetails";
import Login from "./pages/Login";
import Register from "./pages/Register";
import { ProtectedRoute, GuestRoute } from "./components/RouteGuards";
import { fetchMe, logout } from "./features/authSlice";
import { setUnauthorizedHandler } from "./api/client";

const App = () => {
    const dispatch = useDispatch();
    const { token, user } = useSelector((state) => state.auth);

    // Token expired/invalid (HTTP 401) -> log out, and ProtectedRoute will redirect to /login.
    useEffect(() => {
        setUnauthorizedHandler(() => {
            toast.error("Your session has expired. Please sign in again.");
            dispatch(logout());
        });
    }, [dispatch]);

    // Reload the page when a token exists: fetch user information from the backend.
    useEffect(() => {
        if (token && !user) dispatch(fetchMe());
    }, [token, user, dispatch]);

    return (
        <>
            <Toaster />
            <Routes>
                <Route element={<GuestRoute />}>
                    <Route path="login" element={<Login />} />
                    <Route path="register" element={<Register />} />
                </Route>
                <Route path="/" element={<ProtectedRoute><Layout /></ProtectedRoute>}>
                    <Route index element={<Dashboard />} />
                    <Route path="team" element={<Team />} />
                    <Route path="projects" element={<Projects />} />
                    <Route path="projectsDetail" element={<ProjectDetails />} />
                    <Route path="taskDetails" element={<TaskDetails />} />
                </Route>
                <Route path="*" element={<Navigate to="/" replace />} />
            </Routes>
        </>
    );
};

export default App;
