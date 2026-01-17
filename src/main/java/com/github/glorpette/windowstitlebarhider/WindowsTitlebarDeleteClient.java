package com.github.glorpette.windowstitlebarhider;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFWNativeWin32;

public class WindowsTitlebarHiderClient implements ClientModInitializer {
            private static final int STYLE_FLAGS_TO_REMOVE =
                0x00C00000 | // WS_CAPTION (includes border + dlgframe)
                0x00080000 | // WS_SYSMENU
                0x00020000 | // WS_MINIMIZEBOX
                0x00010000 | // WS_MAXIMIZEBOX
                0x00040000;  // WS_THICKFRAME

            private static final int STYLE_FLAGS_TO_ADD =
                0x80000000 | // WS_POPUP
                0x04000000 | // WS_CLIPSIBLINGS
                0x02000000 | // WS_CLIPCHILDREN
                0x10000000;  // WS_VISIBLE

            private static final int EXT_STYLE_FLAGS_TO_REMOVE =
                0x00000001 | // WS_EX_DLGMODALFRAME
                0x00000200 | // WS_EX_CLIENTEDGE
                0x00000100 | // WS_EX_WINDOWEDGE
                0x00020000 | // WS_EX_STATICEDGE
                0x00000300;  // WS_EX_OVERLAPPEDWINDOW (windowedge | clientedge)

    private boolean lastFullscreenState = false;
    private static boolean titlebarHidden = false;

    @Override
    public void onInitializeClient() {
        if (!isRunningOnWindows()) {
            WindowsTitlebarHider.LOGGER.info("Non-Windows OS detected; skipping titlebar removal.");
            return;
        }

        enforceHiddenTitlebar();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.getWindow() == null) {
                return;
            }

            WinDef.HWND hwnd = getMinecraftWindow();
            if (hwnd == null) {
                return;
            }

            boolean currentFullscreenState = client.getWindow().isFullscreen();
            boolean titlebarRestored = (User32.INSTANCE.GetWindowLong(hwnd, WinUser.GWL_STYLE) & STYLE_FLAGS_TO_REMOVE) != 0;

            if (currentFullscreenState != lastFullscreenState || titlebarRestored || !titlebarHidden) {
                enforceHiddenTitlebar(hwnd);
                lastFullscreenState = currentFullscreenState;
            }
        });
    }

    private static void enforceHiddenTitlebar() {
        WinDef.HWND hwnd = getMinecraftWindow();
        if (hwnd != null) {
            enforceHiddenTitlebar(hwnd);
        }
    }

    private static void enforceHiddenTitlebar(WinDef.HWND hwnd) {
        try {
                int currentStyle = User32.INSTANCE.GetWindowLong(hwnd, WinUser.GWL_STYLE);
                int currentExStyle = User32.INSTANCE.GetWindowLong(hwnd, WinUser.GWL_EXSTYLE);

                    int newStyle = (currentStyle & ~STYLE_FLAGS_TO_REMOVE) | STYLE_FLAGS_TO_ADD;
                    int newExStyle = currentExStyle & ~EXT_STYLE_FLAGS_TO_REMOVE;

                User32.INSTANCE.SetWindowLong(hwnd, WinUser.GWL_STYLE, newStyle);
                User32.INSTANCE.SetWindowLong(hwnd, WinUser.GWL_EXSTYLE, newExStyle);
                User32.INSTANCE.SetWindowPos(
                    hwnd,
                    null,
                    0,
                    0,
                    0,
                    0,
                    WinUser.SWP_FRAMECHANGED | WinUser.SWP_NOMOVE | WinUser.SWP_NOSIZE | WinUser.SWP_NOZORDER | WinUser.SWP_NOOWNERZORDER | WinUser.SWP_NOACTIVATE
                );

            titlebarHidden = true;
        } catch (Exception e) {
            WindowsTitlebarHider.LOGGER.error("Error applying titlebar removal: {}", e.getMessage(), e);
        }
    }

    private static WinDef.HWND getMinecraftWindow() {
        try {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client == null || client.getWindow() == null) {
                return null;
            }

            long glfwHandle = client.getWindow().getHandle();
            long win32Handle = GLFWNativeWin32.glfwGetWin32Window(glfwHandle);
            if (win32Handle == 0L) {
                return null;
            }

            return new WinDef.HWND(new Pointer(win32Handle));
        } catch (UnsatisfiedLinkError | NoClassDefFoundError nativeMissing) {
            WindowsTitlebarHider.LOGGER.warn("Win32 native lookup failed; cannot modify titlebar.");
            return null;
        } catch (Exception e) {
            WindowsTitlebarHider.LOGGER.error("Error finding Minecraft window: {}", e.getMessage(), e);
            return null;
        }
    }

    private static boolean isRunningOnWindows() {
        return System.getProperty("os.name", "").toLowerCase().contains("windows");
    }
}
