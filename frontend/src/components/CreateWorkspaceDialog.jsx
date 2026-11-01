import { useState } from "react";
import { useDispatch } from "react-redux";
import { useNavigate } from "react-router-dom";
import toast from "react-hot-toast";
import { Building2Icon } from "lucide-react";
import { createWorkspace } from "../features/workspaceSlice";
import { getErrorMessage } from "../api/client";

const inputClasses =
    "w-full rounded border border-zinc-300 dark:border-zinc-700 dark:bg-zinc-900 text-zinc-900 dark:text-zinc-200 text-sm placeholder-zinc-400 dark:placeholder-zinc-500 py-2 px-3 mt-1 focus:outline-none focus:border-blue-500";

/**
 * Tạo workspace mới. required = true: dùng ở màn hình "chưa có workspace nào" (hiện trực tiếp, không có nút Cancel).
 */
const CreateWorkspaceDialog = ({ isDialogOpen, setIsDialogOpen, required = false }) => {
    const dispatch = useDispatch();
    const navigate = useNavigate();
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [formData, setFormData] = useState({ name: "", description: "", image_url: "" });

    const handleSubmit = async (e) => {
        e.preventDefault();
        setIsSubmitting(true);
        try {
            await dispatch(
                createWorkspace({
                    name: formData.name.trim(),
                    description: formData.description,
                    image_url: formData.image_url.trim(),
                })
            ).unwrap();
            toast.success("Workspace created");
            setFormData({ name: "", description: "", image_url: "" });
            setIsDialogOpen(false);
            navigate("/");
        } catch (error) {
            toast.error(getErrorMessage(error));
        } finally {
            setIsSubmitting(false);
        }
    };

    if (!isDialogOpen) return null;

    const card = (
        <div className="bg-white dark:bg-zinc-950 border border-zinc-300 dark:border-zinc-800 rounded-xl p-6 w-full max-w-md text-zinc-900 dark:text-zinc-200">
            <div className="mb-4">
                <h2 className="text-xl font-bold flex items-center gap-2">
                    <Building2Icon className="size-5" /> Create Workspace
                </h2>
                <p className="text-sm text-zinc-700 dark:text-zinc-400">
                    A workspace holds your projects, tasks and team members.
                </p>
            </div>

            <form onSubmit={handleSubmit} className="space-y-4">
                <div>
                    <label htmlFor="workspace-name" className="text-sm font-medium">Workspace name</label>
                    <input id="workspace-name" value={formData.name} onChange={(e) => setFormData({ ...formData, name: e.target.value })} placeholder="e.g. My Company" className={inputClasses} required maxLength={100} />
                </div>
                <div>
                    <label htmlFor="workspace-description" className="text-sm font-medium">Description (optional)</label>
                    <textarea id="workspace-description" value={formData.description} onChange={(e) => setFormData({ ...formData, description: e.target.value })} placeholder="What is this workspace for?" className={inputClasses + " h-20"} maxLength={2000} />
                </div>
                <div>
                    <label htmlFor="workspace-image" className="text-sm font-medium">Logo URL (optional)</label>
                    <input id="workspace-image" type="url" value={formData.image_url} onChange={(e) => setFormData({ ...formData, image_url: e.target.value })} placeholder="https://..." className={inputClasses} />
                </div>

                <div className="flex justify-end gap-3 pt-2">
                    {!required && (
                        <button type="button" onClick={() => setIsDialogOpen(false)} className="px-5 py-2 rounded text-sm border border-zinc-300 dark:border-zinc-700 text-zinc-900 dark:text-zinc-200 hover:bg-zinc-100 dark:hover:bg-zinc-800 transition">
                            Cancel
                        </button>
                    )}
                    <button type="submit" disabled={isSubmitting} className="px-5 py-2 rounded text-sm bg-gradient-to-br from-blue-500 to-blue-600 text-white disabled:opacity-50 hover:opacity-90 transition">
                        {isSubmitting ? "Creating..." : "Create Workspace"}
                    </button>
                </div>
            </form>
        </div>
    );

    if (required) return card;

    return (
        <div className="fixed inset-0 bg-black/20 dark:bg-black/50 backdrop-blur flex items-center justify-center z-50">
            {card}
        </div>
    );
};

export default CreateWorkspaceDialog;
