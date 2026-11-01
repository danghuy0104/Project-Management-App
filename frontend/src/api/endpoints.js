import { api } from "./client";

export const authApi = {
    login: (credentials) => api.post("/api/auth/login", credentials),
    register: (data) => api.post("/api/auth/register", data),
    me: () => api.get("/api/auth/me"),
};

export const workspaceApi = {
    list: () => api.get("/api/workspaces"),
    create: (data) => api.post("/api/workspaces", data),
    overview: (workspaceId) => api.get(`/api/workspaces/${workspaceId}/overview`),
    stats: (workspaceId) => api.get(`/api/workspaces/${workspaceId}/stats`),
    addMember: (workspaceId, data) => api.post(`/api/workspaces/${workspaceId}/members`, data),
};

export const projectApi = {
    create: (workspaceId, data) => api.post(`/api/workspaces/${workspaceId}/projects`, data),
    update: (projectId, data) => api.put(`/api/projects/${projectId}`, data),
    addMember: (projectId, data) => api.post(`/api/projects/${projectId}/members`, data),
};

export const taskApi = {
    create: (projectId, data) => api.post(`/api/projects/${projectId}/tasks`, data),
    updateStatus: (taskId, status) => api.patch(`/api/tasks/${taskId}/status`, { status }),
    bulkDelete: (ids) => api.post("/api/tasks/bulk-delete", { ids }),
};

export const commentApi = {
    list: (taskId) => api.get(`/api/tasks/${taskId}/comments`),
    add: (taskId, content) => api.post(`/api/tasks/${taskId}/comments`, { content }),
};
