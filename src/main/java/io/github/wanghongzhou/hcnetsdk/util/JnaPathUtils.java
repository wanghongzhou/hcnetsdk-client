package io.github.wanghongzhou.hcnetsdk.util;

import com.sun.jna.Platform;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Jna加载本地库相关工具类。
 */
public class JnaPathUtils {

    public static final String JNA_PATH_PROPERTY_NAME = "jna.library.path";

    /**
     * 检查并设置本地库加载目录系统变量(jna.library.path), 仅开发环境下。
     */
    public static boolean initJnaLibraryPathDev() {
        return initJnaLibraryPath(null, false);
    }

    /**
     * 检查并设置本地库加载目录系统变量(jna.library.path)，
     * <p>
     * 设置的路径, 资源目录下： natives/{type}, 其中type在不同操作系统下对应值不一样(so, dll, dylib)
     */
    public static boolean initJnaLibraryPath(Class<?> target) {
        return initJnaLibraryPath(target, true);
    }

    /**
     * 检查并设置本地库加载目录系统变量(jna.library.path)，
     * <p>
     * 设置的路径, 资源目录下： natives/{type}, 其中type在不同操作系统下对应值不一样(so, dll, dylib)
     */
    public static boolean initJnaLibraryPath(Class<?> target, boolean effectiveJar) {
        boolean modifiedPath = false;
        String jnaLibPath = System.getProperty(JNA_PATH_PROPERTY_NAME);
        if (Objects.isNull(jnaLibPath)) {

            Map<Integer, String> libDirMap = new HashMap<>();
            libDirMap.put(Platform.WINDOWS, "natives/dll");
            libDirMap.put(Platform.LINUX, "natives/so");
            libDirMap.put(Platform.MAC, "natives/dylib");

            String libDir = libDirMap.get(Platform.getOSType());
            if (Objects.isNull(libDir)) {
                throw new RuntimeException("Unsupported operator system: " + Platform.ARCH);
            }

            if (!isRunJar()) {
                URL uri = JnaPathUtils.class.getClassLoader().getResource(libDir);
                if (Objects.isNull(uri)) {
                    throw new IllegalStateException("Not found relation library: " + libDir);
                }
                jnaLibPath = uri.getPath();
                System.setProperty(JNA_PATH_PROPERTY_NAME, jnaLibPath);
                modifiedPath = true;
            } else if (effectiveJar) {
                jnaLibPath = getJarDirectoryPath(target) + File.separator + libDir;
                System.setProperty(JNA_PATH_PROPERTY_NAME, jnaLibPath);
                modifiedPath = true;
            }
        }
        return modifiedPath;
    }

    /**
     * 是否以可执行jar方式启动
     */
    public static boolean isRunJar() {
        URL resource = JnaPathUtils.class.getResource("/");
        if (Objects.isNull(resource)) {
            resource = JnaPathUtils.class.getResource("");
        }
        if (Objects.isNull(resource)) {
            return false;
        }

        String protocol = resource.getProtocol();
        return "jar".equals(protocol);
    }

    /**
     * 获取执行jar所在目录
     */
    public static String getJarDirectoryPath(Class<?> target) {
        URL url = getLocation(target);
        File file = urlToFile(url);
        assert file != null;
        return file.getParent();
    }


    /**
     * Gets the base location of the given class.
     * <p>
     * If the class is directly on the file system (e.g., "/path/to/my/package/MyClass.class") then it will return the
     * base directory (e.g., "file:/path/to").
     * </p>
     * <p>
     * If the class is within a JAR file (e.g., "/path/to/my-jar.jar!/my/package/MyClass.class") then it will return the
     * path to the JAR (e.g., "file:/path/to/my-jar.jar").
     * </p>
     *
     * @param c The class whose location is desired.
     */
    private static URL getLocation(final Class<?> c) {
        if (Objects.isNull(c)) {
            return null; // could not load the class
        }

        // try the easy way first
        try {
            final URL codeSourceLocation = c.getProtectionDomain().getCodeSource().getLocation();
            if (codeSourceLocation != null) {
                return codeSourceLocation;
            }
        } catch (final SecurityException e) {
            // NB: Cannot access protection domain.
        } catch (final NullPointerException e) {
            // NB: Protection domain or code source is null.
        }

        // NB: The easy way failed, so we try the hard way. We ask for the class
        // itself as a resource, then strip the class's path from the URL string,
        // leaving the base path.

        // get the class's raw resource path
        final URL classResource = c.getResource(c.getSimpleName() + ".class");
        if (Objects.isNull(classResource)) {
            return null; // cannot find class resource
        }

        final String url = classResource.toString();
        final String suffix = c.getCanonicalName().replace('.', '/') + ".class";
        if (!url.endsWith(suffix)) {
            return null; // weird URL
        }

        // strip the class's path from the URL string
        String path = url.substring(0, url.length() - suffix.length());

        // remove the "jar:" prefix and "!/" suffix, if present
        if (path.startsWith("jar:")) {
            path = path.substring(4, path.length() - 2);
        }

        try {
            return new URL(path);
        } catch (final MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Converts the given {@link URL} to its corresponding {@link File}.
     * <p>
     * This method is similar to calling {@code new File(url.toURI())} except that it also handles "jar:file:" URLs,
     * returning the path to the JAR file.
     * </p>
     *
     * @param url The URL to convert.
     * @throws IllegalArgumentException if the URL does not correspond to a file.
     */
    private static File urlToFile(final URL url) {
        return Objects.isNull(url) ? null : urlToFile(url.toString());
    }

    /**
     * Converts the given URL string to its corresponding {@link File}.
     *
     * @param url The URL to convert.
     * @throws IllegalArgumentException if the URL does not correspond to a file.
     */
    private static File urlToFile(final String url) {
        String path = url;
        if (path.startsWith("jar:")) {
            // remove "jar:" prefix and "!/" suffix
            final int index = path.indexOf("!/");
            path = path.substring(4, index);
        }
        try {
            if (Platform.isWindows() && path.matches("file:[A-Za-z]:.*")) {
                path = "file:/" + path.substring(5);
            }
            return new File(new URL(path).toURI());
        } catch (final MalformedURLException | URISyntaxException e) {
            // NB: URL is not completely well-formed.
        }
        if (path.startsWith("file:")) {
            // pass through the URL as-is, minus "file:" prefix
            path = path.substring(5);
            return new File(path);
        }
        throw new IllegalArgumentException("Invalid URL: " + url);
    }

}
