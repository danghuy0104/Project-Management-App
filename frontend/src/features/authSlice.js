import { createSlice, createAsyncThunk } from "@reduxjs/toolkit";
import { authApi } from "../api/endpoints";
import { getErrorMessage, tokenStorage } from "../api/client";

export const login = createAsyncThunk("auth/login", async (credentials, { rejectWithValue }) => {
    try {
        const data = await authApi.login(credentials);
        tokenStorage.set(data.accessToken);
        return data.user;
    } catch (error) {
        return rejectWithValue(getErrorMessage(error));
    }
});

export const register = createAsyncThunk("auth/register", async (payload, { rejectWithValue }) => {
    try {
        const data = await authApi.register(payload);
        tokenStorage.set(data.accessToken);
        return data.user;
    } catch (error) {
        return rejectWithValue(getErrorMessage(error));
    }
});

// Khi mở lại trang: có token trong localStorage thì hỏi backend "tôi là ai" để lấy thông tin user.
export const fetchMe = createAsyncThunk("auth/fetchMe", async (_, { rejectWithValue }) => {
    try {
        return await authApi.me();
    } catch (error) {
        return rejectWithValue(getErrorMessage(error));
    }
});

const savedToken = tokenStorage.get();

const initialState = {
    token: savedToken,
    user: null,
    // loading: đang xác thực token | authenticated | unauthenticated
    status: savedToken ? "loading" : "unauthenticated",
};

const authSlice = createSlice({
    name: "auth",
    initialState,
    reducers: {
        logout: (state) => {
            tokenStorage.clear();
            localStorage.removeItem("currentWorkspaceId");
            state.token = null;
            state.user = null;
            state.status = "unauthenticated";
        },
    },
    extraReducers: (builder) => {
        const signedIn = (state, action) => {
            state.user = action.payload;
            state.token = tokenStorage.get();
            state.status = "authenticated";
        };
        builder
            .addCase(login.fulfilled, signedIn)
            .addCase(register.fulfilled, signedIn)
            .addCase(fetchMe.pending, (state) => {
                state.status = "loading";
            })
            .addCase(fetchMe.fulfilled, (state, action) => {
                state.user = action.payload;
                state.status = "authenticated";
            })
            .addCase(fetchMe.rejected, (state) => {
                tokenStorage.clear();
                state.token = null;
                state.user = null;
                state.status = "unauthenticated";
            });
    },
});

export const { logout } = authSlice.actions;
export default authSlice.reducer;
