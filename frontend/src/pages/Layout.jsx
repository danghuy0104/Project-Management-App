import { useState, useEffect } from 'react'
import Navbar from '../components/Navbar'
import Sidebar from '../components/Sidebar'
import NoWorkspace from '../components/NoWorkspace'
import { Outlet } from 'react-router-dom'
import { useDispatch, useSelector } from 'react-redux'
import { loadTheme } from '../features/themeSlice'
import { loadWorkspaces } from '../features/workspaceSlice'
import { logout } from '../features/authSlice'
import { Loader2Icon } from 'lucide-react'

const Layout = () => {
    const [isSidebarOpen, setIsSidebarOpen] = useState(false)
    const { loading, initialized, error, currentWorkspace } = useSelector((state) => state.workspace)
    const dispatch = useDispatch()

    // Initial load of theme
    useEffect(() => {
        dispatch(loadTheme())
    }, [dispatch])

    // Tải workspace của người dùng từ backend khi vào ứng dụng
    useEffect(() => {
        dispatch(loadWorkspaces())
    }, [dispatch])

    if (loading || !initialized) return (
        <div className='flex items-center justify-center h-screen bg-white dark:bg-zinc-950'>
            <Loader2Icon className="size-7 text-blue-500 animate-spin" />
        </div>
    )

    if (error) return (
        <div className='flex flex-col items-center justify-center h-screen gap-3 bg-white dark:bg-zinc-950 text-gray-900 dark:text-slate-100 px-4 text-center'>
            <p className='text-lg font-medium'>Could not load your workspaces</p>
            <p className='text-sm text-gray-500 dark:text-zinc-400'>{error}</p>
            <div className='flex gap-3'>
                <button onClick={() => dispatch(loadWorkspaces())} className='px-5 py-2 text-sm rounded bg-gradient-to-br from-blue-500 to-blue-600 text-white hover:opacity-90 transition'>
                    Try again
                </button>
                <button onClick={() => dispatch(logout())} className='px-5 py-2 text-sm rounded border border-gray-300 dark:border-zinc-700 hover:bg-gray-100 dark:hover:bg-zinc-800 transition'>
                    Sign out
                </button>
            </div>
        </div>
    )

    if (!currentWorkspace) return <NoWorkspace />

    return (
        <div className="flex bg-white dark:bg-zinc-950 text-gray-900 dark:text-slate-100">
            <Sidebar isSidebarOpen={isSidebarOpen} setIsSidebarOpen={setIsSidebarOpen} />
            <div className="flex-1 flex flex-col h-screen">
                <Navbar isSidebarOpen={isSidebarOpen} setIsSidebarOpen={setIsSidebarOpen} />
                <div className="flex-1 h-full p-6 xl:p-10 xl:px-16 overflow-y-scroll">
                    <Outlet />
                </div>
            </div>
        </div>
    )
}

export default Layout
