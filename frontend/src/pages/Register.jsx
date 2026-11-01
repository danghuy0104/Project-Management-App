import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { useDispatch } from "react-redux";
import toast from "react-hot-toast";
import { UserPlusIcon } from "lucide-react";
import { register } from "../features/authSlice";
import { loadTheme } from "../features/themeSlice";
import { getErrorMessage } from "../api/client";

const inputClasses =
    "w-full rounded border border-zinc-300 dark:border-zinc-700 dark:bg-zinc-900 text-zinc-900 dark:text-zinc-200 text-sm py-2 px-3 mt-1 focus:outline-none focus:border-blue-500";

const Register = () => {
    const dispatch = useDispatch();
    const [formData, setFormData] = useState({ name: "", email: "", password: "" });
    const [isSubmitting, setIsSubmitting] = useState(false);

    useEffect(() => {
        dispatch(loadTheme());
    }, [dispatch]);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setIsSubmitting(true);
        try {
            await dispatch(register(formData)).unwrap();
            // Đăng ký xong backend trả token luôn, GuestRoute tự chuyển sang trang chính.
        } catch (error) {
            toast.error(getErrorMessage(error));
            setIsSubmitting(false);
        }
    };

    return (
        <div className="min-h-screen flex items-center justify-center bg-white dark:bg-zinc-950 px-4">
            <div className="w-full max-w-md bg-white dark:bg-zinc-950 border border-zinc-300 dark:border-zinc-800 rounded-xl p-6 text-zinc-900 dark:text-zinc-200">
                <h1 className="text-xl font-bold flex items-center gap-2 mb-1">
                    <UserPlusIcon className="size-5" /> Create account
                </h1>
                <p className="text-sm text-zinc-600 dark:text-zinc-400 mb-5">Start managing your projects with your team</p>

                <form onSubmit={handleSubmit} className="space-y-4">
                    <div>
                        <label htmlFor="name" className="text-sm font-medium">Full name</label>
                        <input id="name" type="text" autoComplete="name" value={formData.name} onChange={(e) => setFormData({ ...formData, name: e.target.value })} placeholder="Your name" className={inputClasses} required />
                    </div>
                    <div>
                        <label htmlFor="email" className="text-sm font-medium">Email</label>
                        <input id="email" type="email" autoComplete="email" value={formData.email} onChange={(e) => setFormData({ ...formData, email: e.target.value })} placeholder="you@example.com" className={inputClasses} required />
                    </div>
                    <div>
                        <label htmlFor="password" className="text-sm font-medium">Password</label>
                        <input id="password" type="password" autoComplete="new-password" minLength={8} value={formData.password} onChange={(e) => setFormData({ ...formData, password: e.target.value })} placeholder="At least 8 characters" className={inputClasses} required />
                    </div>
                    <button type="submit" disabled={isSubmitting} className="w-full px-5 py-2 rounded text-sm bg-gradient-to-br from-blue-500 to-blue-600 text-white disabled:opacity-50 hover:opacity-90 transition">
                        {isSubmitting ? "Creating account..." : "Create account"}
                    </button>
                </form>

                <p className="text-sm text-zinc-600 dark:text-zinc-400 mt-5 text-center">
                    Already have an account?{" "}
                    <Link to="/login" className="text-blue-600 dark:text-blue-400 hover:underline">Sign in</Link>
                </p>
            </div>
        </div>
    );
};

export default Register;
