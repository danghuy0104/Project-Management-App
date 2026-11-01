import { useDispatch, useSelector } from "react-redux";
import { logout } from "../features/authSlice";
import CreateWorkspaceDialog from "./CreateWorkspaceDialog";

/** Displayed when a user (just registered) is not part of any workspace yet. */
const NoWorkspace = () => {
    const dispatch = useDispatch();
    const user = useSelector((state) => state.auth.user);

    return (
        <div className="flex flex-col items-center justify-center min-h-screen bg-white dark:bg-zinc-950 text-zinc-900 dark:text-zinc-200 px-4 py-10">
            <div className="text-center mb-6">
                <h1 className="text-2xl font-semibold">Welcome{user ? `, ${user.name}` : ""}!</h1>
                <p className="text-sm text-zinc-600 dark:text-zinc-400 mt-1">
                    Create your first workspace to start managing projects.
                </p>
            </div>
            <CreateWorkspaceDialog isDialogOpen={true} setIsDialogOpen={() => { }} required />
            <button onClick={() => dispatch(logout())} className="mt-6 text-sm text-blue-600 dark:text-blue-400 hover:underline">
                Sign out
            </button>
        </div>
    );
};

export default NoWorkspace;
