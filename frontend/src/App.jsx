import { Routes, Route } from "react-router-dom";
import Layout from "./pages/Layout";
import { Toaster } from "react-hot-toast";
import Dashboard from "./pages/Dashboard";
import Projects from "./pages/Projects";

const App = () => {
    return (
        <>
            <Toaster />
            <Routes>
                <Route path="/" element={<Layout />}>
                    <Route index element={<Dashboard />} />
                    <Route path="projects" element={<Projects />} />
                </Route>
            </Routes>
        </>
    );
};

export default App;