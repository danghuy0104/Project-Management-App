// Avatar component; if the backend returns an empty image, show the first letter of the name instead of a broken image.
const Avatar = ({ name = "", image, className = "size-7 rounded-full" }) => {
    if (image) {
        return <img src={image} alt={name} className={className} />;
    }
    const initial = (name || "?").trim().charAt(0).toUpperCase() || "?";
    return (
        <div
            title={name}
            className={`${className} flex items-center justify-center shrink-0 select-none text-xs font-medium bg-blue-500/15 text-blue-600 dark:bg-blue-400/20 dark:text-blue-300`}
        >
            {initial}
        </div>
    );
};

export default Avatar;
