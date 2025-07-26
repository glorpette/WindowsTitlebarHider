package wvv.windowstitlebardelete;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class WindowsTitlebarDeleteClient implements ClientModInitializer {

    private static boolean titlebarHidden = false;
    private boolean lastFullscreenState = false;

    @Override
    public void onInitializeClient() {
        Keybinds.register();
        
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.world != null) {
                boolean currentFullscreenState = client.getWindow().isFullscreen();
                
                if (lastFullscreenState && !currentFullscreenState && titlebarHidden) {
                    hideTitlebar();
                }
                
                lastFullscreenState = currentFullscreenState;
            }
        });
    }

    public static void toggleTitlebar() {
        if (titlebarHidden) {
            showTitlebar();
            titlebarHidden = false;
        } else {
            hideTitlebar();
            titlebarHidden = true;
        }
    }

    private static void hideTitlebar() {
        if (isWindows()) {
            return;
        }

        try {
            WinDef.HWND hwnd = getMinecraftWindow();
            if (hwnd != null) {
                int currentStyle = User32.INSTANCE.GetWindowLong(hwnd, WinUser.GWL_STYLE);
                int newStyle = currentStyle & ~(WinUser.WS_CAPTION | WinUser.WS_SYSMENU | WinUser.WS_MINIMIZEBOX | WinUser.WS_MAXIMIZEBOX);
                newStyle |= (WinUser.WS_MAXIMIZEBOX | WinUser.WS_THICKFRAME);

                User32.INSTANCE.SetWindowLong(hwnd, WinUser.GWL_STYLE, newStyle);
                User32.INSTANCE.SetWindowPos(
                        hwnd,
                        null,
                        0,
                        0,
                        0,
                        0,
                        WinUser.SWP_FRAMECHANGED | WinUser.SWP_NOMOVE | WinUser.SWP_NOSIZE | WinUser.SWP_NOZORDER
                );
            }
        } catch (Exception e) {
            WindowsTitlebarDelete.LOGGER.error("Error hiding titlebar: {}", e.getMessage(), e);
        }
    }

    private static void showTitlebar() {
        if (isWindows()) {
            return;
        }

        try {
            WinDef.HWND hwnd = getMinecraftWindow();
            if (hwnd != null) {
                int currentStyle = User32.INSTANCE.GetWindowLong(hwnd, WinUser.GWL_STYLE);
                int newStyle = currentStyle | (WinUser.WS_CAPTION | WinUser.WS_SYSMENU | WinUser.WS_MINIMIZEBOX | WinUser.WS_MAXIMIZEBOX);

                User32.INSTANCE.SetWindowLong(hwnd, WinUser.GWL_STYLE, newStyle);
                User32.INSTANCE.SetWindowPos(
                        hwnd,
                        null,
                        0,
                        0,
                        0,
                        0,
                        WinUser.SWP_FRAMECHANGED | WinUser.SWP_NOMOVE | WinUser.SWP_NOSIZE | WinUser.SWP_NOZORDER
                );
            }
        } catch (Exception e) {
            WindowsTitlebarDelete.LOGGER.error("Error showing titlebar: {}", e.getMessage(), e);
        }
    }

    private static WinDef.HWND getMinecraftWindow() {
        try {
            WinDef.HWND hwnd = User32.INSTANCE.FindWindow("LWJGL", null);
            if (hwnd != null) {
                return hwnd;
            }

            final WinDef.HWND[] result = new WinDef.HWND[1];
            User32.INSTANCE.EnumWindows((hwnd1, data) -> {
                char[] windowText = new char[512];
                User32.INSTANCE.GetWindowText(hwnd1, windowText, 512);
                String title = Native.toString(windowText);

                if (title.contains("Minecraft")) {
                    result[0] = hwnd1;
                    return false;
                }
                return true;
            }, null);

            return result[0];
        } catch (Exception e) {
            WindowsTitlebarDelete.LOGGER.error("Error finding window: {}", e.getMessage(), e);
            return null;
        }
    }

    private static boolean isWindows() {
        return !System.getProperty("os.name").toLowerCase().contains("windows");
    }
}
