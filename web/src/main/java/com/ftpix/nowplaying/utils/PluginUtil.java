package com.ftpix.nowplaying.utils;

import com.ftpix.nowplaying.NowPlayingPlugin;
import com.ftpix.nowplaying.Plugin;
import com.ftpix.nowplaying.WebApp;
import com.ftpix.nowplaying.activities.MediaActivityPlugin;
import com.ftpix.nowplaying.harmony.HarmonyPlugin;
import com.ftpix.nowplaying.models.Config;
import com.ftpix.nowplaying.plugins.Blackscreen;
import com.ftpix.nowplaying.plugins.Spotify;
import com.ftpix.nowplaying.plugins.defaultactivity.DefaultActivity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.ftpix.nowplaying.WebApp.*;

public class PluginUtil {

    public final static List<Class<? extends MediaActivityPlugin>> ACTIVITY_PLUGINS = List.of(HarmonyPlugin.class, DefaultActivity.class, Spotify.class);
    public final static List<Class<? extends NowPlayingPlugin>> NOWPLAYING_PLUGINS = List.of(Blackscreen.class, Spotify.class);
    public final static List<Class<? extends Plugin>> ALL_PLUGINS = Stream.concat(ACTIVITY_PLUGINS.stream(), NOWPLAYING_PLUGINS.stream()).distinct().collect(Collectors.toList());
    public final static Map<String, ? extends Plugin> PLUGIN_INSTANCES;
    private static final Logger logger = LogManager.getLogger();


    public final static Function<Class<? extends Plugin>, Plugin> CLASS_TO_PLUGIN = c -> {
        try {
            return (Plugin) c.getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            logger.error("Couldn't create new instance of plugin [{}], ignoring it", c.getCanonicalName(), e);
            return null;
        }
    };


    public final static Function<Plugin, Map<String, Object>> PLUGIN_TO_ID_NAME = p -> {
        Map<String, Object> plugin = new HashMap<>();
        plugin.put("name", p.getName());
        plugin.put("id", p.getId());

        List<String> tags = new ArrayList<>();
        if (p instanceof MediaActivityPlugin) {
            tags.add("Activity");
        }

        if (p instanceof NowPlayingPlugin) {
            tags.add("Now Playing");
        }

        plugin.put("tags", Strings.join(tags, ','));
        plugin.put("settings", p.getSettings());


        if (CONFIG.pluginSettings.containsKey(p.getId())) {
            plugin.put("settingsValues", CONFIG.pluginSettings.get(p.getId()));
        }
        return plugin;
    };

    static {
        PLUGIN_INSTANCES = ALL_PLUGINS.stream()
                .map(p -> {
                    try {
                        return (Plugin) p.getConstructor().newInstance();
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(p -> p != null)
                .map(p -> {
                    if (CONFIG.pluginSettings.containsKey(p.getId())) {
                        p.init(CONFIG.pluginSettings.get(p.getId()));
                    }
                    return p;
                })
                .collect(Collectors.toMap(Plugin::getId, Function.identity()));
    }

    /**
     * Will check if a given string is a valid plugin id
     *
     * @param plugin the id of the plugin to check against
     * @return true if the plugin exists
     */
    public static boolean pluginExists(String plugin) {
        System.out.println(plugin);
        return ALL_PLUGINS.stream()
                .map(c -> {
                    try {
                        Plugin p = c.getConstructor().newInstance();
                        return p;
                    } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                        e.printStackTrace();
                        return null;
                    }
                })
                .filter(p -> p != null)
                .peek(p -> System.out.println(p.getClass()))
                .map(p -> p.getId())
                .filter(plugin::equalsIgnoreCase)
                .findFirst().isPresent();

    }

}
