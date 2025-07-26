package wvv.windowstitlebardelete;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class Keybinds {
    private static final String CATEGORY = WindowsTitlebarDelete.MOD_NAME;

    public static final KeyBinding TOGGLE_TITLEBAR_DELETE = new KeyBinding(
            "Toggle Titlebar Delete",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_F12,
            CATEGORY
    );

    public static void register() {
        KeyBindingHelper.registerKeyBinding(TOGGLE_TITLEBAR_DELETE);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (TOGGLE_TITLEBAR_DELETE.wasPressed()) {
                toggleTitlebarDelete();
            }
        });
    }

    private static void toggleTitlebarDelete() {
        WindowsTitlebarDeleteClient.toggleTitlebar();
    }
}
