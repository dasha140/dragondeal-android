package com.elvsf.dragondeal.util;

/**
 * Centralized resolver for backend image URLs.
 *
 * Derives the backend root (scheme + host) from the Retrofit BASE_URL and uses it
 * to construct full image URLs, replacing localhost/127.0.0.1 placeholders.
 */
public final class ImageUrlResolver {

    private ImageUrlResolver() {
    }

    /**
     * Resolves an image URL relative to the backend base URL.
     *
     * @param imageUrl nullable image path or URL from the backend DTO
     * @param baseUrl  the Retrofit BASE_URL (e.g. "https://host/api/" or "http://host:8081/api/")
     * @return resolved full image URL, or null if imageUrl is blank
     */
    public static String resolve(String imageUrl, String baseUrl) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            return null;
        }

        String backendRoot = extractBackendRoot(baseUrl);

        if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
            // Already a full URL — replace localhost/127.0.0.1 with backend root,
            // otherwise leave as-is (Railway URLs pass through unchanged).
            return imageUrl
                    .replace("http://127.0.0.1:8081", backendRoot)
                    .replace("http://localhost:8081", backendRoot);
        }

        // Relative path — prepend backend root.
        if (imageUrl.startsWith("/")) {
            return backendRoot + imageUrl;
        }

        return backendRoot + "/" + imageUrl;
    }

    /**
     * Extracts the scheme + host (no trailing path) from a base URL like
     * "https://host.up.railway.app/api/" → "https://host.up.railway.app"
     */
    private static String extractBackendRoot(String baseUrl) {
        if (baseUrl == null || baseUrl.isEmpty()) {
            return "";
        }

        // Remove trailing slash for consistent parsing
        String url = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;

        // Find the path separator after the host
        int pathStart = url.indexOf("/", url.indexOf("://") + 3);
        if (pathStart != -1) {
            return url.substring(0, pathStart);
        }

        return url;
    }
}
