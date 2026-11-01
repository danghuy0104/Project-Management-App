// Shared API client class: reads VITE_API_BASE_URL, automatically attaches JWT, and normalizes errors.
const TOKEN_KEY = "accessToken";

export const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL || "http://localhost:8080").replace(/\/+$/, "");

export const tokenStorage = {
    get() {
        try {
            return localStorage.getItem(TOKEN_KEY);
        } catch {
            return null;
        }
    },
    set(token) {
        localStorage.setItem(TOKEN_KEY, token);
    },
    clear() {
        localStorage.removeItem(TOKEN_KEY);
    },
};

export class ApiError extends Error {
    constructor(message, status, errors) {
        super(message);
        this.name = "ApiError";
        this.status = status;
        this.errors = errors || null; // { fieldName: "message" } when the backend returns validation errors
    }
}

// App registers this function to automatically log out when the token expires (HTTP 401).
let unauthorizedHandler = null;
export const setUnauthorizedHandler = (handler) => {
    unauthorizedHandler = handler;
};

async function request(method, path, body) {
    const headers = { Accept: "application/json" };
    const token = tokenStorage.get();
    if (token) headers.Authorization = `Bearer ${token}`;
    if (body !== undefined) headers["Content-Type"] = "application/json";

    let response;
    try {
        response = await fetch(`${API_BASE_URL}${path}`, {
            method,
            headers,
            body: body !== undefined ? JSON.stringify(body) : undefined,
        });
    } catch {
        throw new ApiError("Cannot reach the server. Please check that the backend is running.", 0);
    }

    if (response.status === 204) return null;

    let data = null;
    const text = await response.text();
    if (text) {
        try {
            data = JSON.parse(text);
        } catch {
            data = null;
        }
    }

    if (!response.ok) {
        // 401 when a token is present means the token is expired/invalid -> log out. (Incorrect login credentials have no token, so they are not counted here.)
        if (response.status === 401 && token && unauthorizedHandler) unauthorizedHandler();
        throw new ApiError(data?.message || `Request failed (${response.status})`, response.status, data?.errors);
    }
    return data;
}

export const api = {
    get: (path) => request("GET", path),
    post: (path, body) => request("POST", path, body ?? {}),
    put: (path, body) => request("PUT", path, body ?? {}),
    patch: (path, body) => request("PATCH", path, body ?? {}),
    delete: (path) => request("DELETE", path),
};

/** Get a readable error message from ApiError / Error / string (Redux rejectWithValue). */
export const getErrorMessage = (error) => {
    if (!error) return "Something went wrong";
    if (typeof error === "string") return error;
    if (error.errors) {
        const first = Object.values(error.errors)[0];
        if (first) return String(first);
    }
    return error.message || "Something went wrong";
};
