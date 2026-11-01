import { createSlice, createAsyncThunk } from "@reduxjs/toolkit";
import { workspaceApi, projectApi, taskApi } from "../api/endpoints";
import { getErrorMessage } from "../api/client";
import { logout } from "./authSlice";

const CURRENT_WORKSPACE_KEY = "currentWorkspaceId";

const initialState = {
    workspaces: [],        // list of user workspaces (with role)
    currentWorkspace: null, // selected workspace, full data: members, projects (tasks, members)
    loading: false,        // true while loading initially or switching workspaces (Layout shows spinner)
    initialized: false,    // loaded at least once
    error: null,
};

// ---------- Read data ----------

/** Load the workspace list and then load details for the currently used workspace (prefer the one selected previously). */
export const loadWorkspaces = createAsyncThunk("workspace/load", async (_, { rejectWithValue }) => {
    try {
        const workspaces = await workspaceApi.list();
        if (workspaces.length === 0) return { workspaces, current: null };

        const savedId = localStorage.getItem(CURRENT_WORKSPACE_KEY);
        const target = workspaces.find((w) => w.id === savedId) || workspaces[0];
        const current = await workspaceApi.overview(target.id);
        localStorage.setItem(CURRENT_WORKSPACE_KEY, target.id);
        return { workspaces, current };
    } catch (error) {
        return rejectWithValue(getErrorMessage(error));
    }
});

export const selectWorkspace = createAsyncThunk("workspace/select", async (workspaceId, { rejectWithValue }) => {
    try {
        const current = await workspaceApi.overview(workspaceId);
        localStorage.setItem(CURRENT_WORKSPACE_KEY, workspaceId);
        return current;
    } catch (error) {
        return rejectWithValue(getErrorMessage(error));
    }
});

/** Reload the currently selected workspace after each write action without showing a full-page spinner. */
export const refreshCurrentWorkspace = createAsyncThunk(
    "workspace/refresh",
    async (_, { getState, rejectWithValue }) => {
        const id = getState().workspace.currentWorkspace?.id;
        if (!id) return null;
        try {
            return await workspaceApi.overview(id);
        } catch (error) {
            return rejectWithValue(getErrorMessage(error));
        }
    }
);

// ---------- Write data ----------

export const createWorkspace = createAsyncThunk("workspace/create", async (data, { rejectWithValue }) => {
    try {
        const created = await workspaceApi.create(data);
        const workspaces = await workspaceApi.list();
        const current = await workspaceApi.overview(created.id);
        localStorage.setItem(CURRENT_WORKSPACE_KEY, created.id);
        return { workspaces, current };
    } catch (error) {
        return rejectWithValue(getErrorMessage(error));
    }
});

// Each write action: call API -> reload the current workspace -> return result. Errors are passed via rejectWithValue
// (component uses dispatch(...).unwrap() inside try/catch, then toast.error).
const writeThunk = (type, action) =>
    createAsyncThunk(type, async (arg, { dispatch, getState, rejectWithValue }) => {
        try {
            const result = await action(arg, getState().workspace);
            await dispatch(refreshCurrentWorkspace());
            return result;
        } catch (error) {
            return rejectWithValue(getErrorMessage(error));
        }
    });

export const createProject = writeThunk("workspace/createProject", (data, state) =>
    projectApi.create(state.currentWorkspace.id, data)
);
export const updateProject = writeThunk("workspace/updateProject", ({ projectId, data }) =>
    projectApi.update(projectId, data)
);
export const addProjectMember = writeThunk("workspace/addProjectMember", ({ projectId, email }) =>
    projectApi.addMember(projectId, { email })
);
export const inviteWorkspaceMember = writeThunk("workspace/inviteMember", (data, state) =>
    workspaceApi.addMember(state.currentWorkspace.id, data)
);
export const createTask = writeThunk("workspace/createTask", ({ projectId, data }) =>
    taskApi.create(projectId, data)
);
export const changeTaskStatus = writeThunk("workspace/changeTaskStatus", ({ taskId, status }) =>
    taskApi.updateStatus(taskId, status)
);
export const deleteTasks = writeThunk("workspace/deleteTasks", (ids) => taskApi.bulkDelete(ids));

// ---------- Slice ----------

const workspaceSlice = createSlice({
    name: "workspace",
    initialState,
    reducers: {},
    extraReducers: (builder) => {
        const applyLoaded = (state, action) => {
            state.workspaces = action.payload.workspaces;
            state.currentWorkspace = action.payload.current;
        };

        builder
            .addCase(loadWorkspaces.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(loadWorkspaces.fulfilled, (state, action) => {
                applyLoaded(state, action);
                state.loading = false;
                state.initialized = true;
            })
            .addCase(loadWorkspaces.rejected, (state, action) => {
                state.loading = false;
                state.initialized = true;
                state.error = action.payload || "Failed to load workspaces";
            })
            .addCase(selectWorkspace.pending, (state) => {
                state.loading = true;
            })
            .addCase(selectWorkspace.fulfilled, (state, action) => {
                state.currentWorkspace = action.payload;
                state.loading = false;
            })
            .addCase(selectWorkspace.rejected, (state) => {
                state.loading = false;
            })
            .addCase(createWorkspace.fulfilled, applyLoaded)
            .addCase(refreshCurrentWorkspace.fulfilled, (state, action) => {
                if (action.payload) state.currentWorkspace = action.payload;
            })
            // Logout: clear stale user data.
            .addCase(logout, () => initialState);
    },
});

export default workspaceSlice.reducer;
