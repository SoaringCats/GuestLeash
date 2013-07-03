package ch.jamiete.guestleash;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

public class Config {
    public static String WARNING_MESSAGE = "&cYou cannot leave the direct vicinity of your inviter.";
    public static int LEASH_LENGTH = 30;

    public static void load(final Plugin plugin) {
        final FileConfiguration conf = plugin.getConfig();
        final ArrayList<String> added = new ArrayList<String>();
        for (final Field field : Config.class.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) && !Modifier.isTransient(field.getModifiers())) {
                final String path = field.getName().toLowerCase().replaceAll("__", ".");
                try {
                    if (conf.isSet(path)) {
                        field.set(null, conf.get(path));
                    } else {
                        conf.set(path, field.get(null));
                        added.add(path + " (" + field.get(null) + "), ");
                    }
                } catch (final IllegalAccessException ex) {
                    //
                }
            }
        }
        final StringBuilder sb = new StringBuilder();
        int left = 0;
        for (int i = 0; i < added.size(); i++) {
            sb.append(added.get(i));
            if (i == 49) {
                left = added.size() - 50;
            }
        }
        if (sb.length() != 0) {
            sb.setLength(sb.length() - 2);
            if (left != 0) {
                sb.append(" + " + left + " more...");
            }
            plugin.getLogger().info("New GuestLeash configuration options added to your configuration file: " + sb.toString());
        }
        plugin.saveConfig();
    }

}
