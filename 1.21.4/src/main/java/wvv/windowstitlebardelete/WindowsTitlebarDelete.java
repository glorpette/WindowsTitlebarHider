package wvv.windowstitlebardelete;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WindowsTitlebarDelete implements ModInitializer {
	public static final String MOD_ID = "windowstitlebardelete";
	public static final String MOD_NAME = "Windows Titlebar Delete";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info(MOD_ID + " is initializing!");
	}
}