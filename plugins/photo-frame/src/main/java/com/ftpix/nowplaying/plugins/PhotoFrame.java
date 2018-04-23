package com.ftpix.nowplaying.plugins;

import com.ftpix.nowplaying.NowPlayingPlugin;
import com.ftpix.nowplaying.Setting;
import com.ftpix.nowplaying.SettingType;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PhotoFrame implements NowPlayingPlugin<Path> {

    public static final String SETTINGS_LOCATION = "location";
    public static final String SETTING_REFRESH_RATE = "refreshRate";
    private final Logger logger = LogManager.getLogger();

    private Path path, currentImage;
    private int refreshRate = 5;
    private LocalDateTime lastFetched = LocalDateTime.now().minusMinutes(5);
    private List<String> allowedExtensions = List.of("png", "PNG", "JPG", "JPEG", "jpg", "jpeg", "bmp", "BMP");


    @Override
    public Path getNowPlayingContent() throws IOException {
        LocalDateTime now = LocalDateTime.now();

        if (currentImage == null || now.minus(refreshRate, ChronoUnit.MINUTES).isAfter(lastFetched)) {
            List<Path> pictures = Files.list(path)
                    .filter(Files::isRegularFile)
                    .peek(p -> logger.info("- {}", p.toAbsolutePath().toString()))
                    .filter(p -> allowedExtensions.stream().anyMatch(e -> p.toString().endsWith(e)))
                    .collect(Collectors.toList());

            if (pictures.size() > 0) {
                Collections.shuffle(pictures);
                currentImage = pictures.get(0);
                lastFetched = now;
                return currentImage;

            } else {
                return null;
            }
        } else {
            return currentImage;
        }
    }

    @Override
    public void getNowPlayingImage(Path content, Graphics2D graphics, Dimension dimension, double scale) throws Exception {
        graphics.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 50));

        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, dimension.width, dimension.height);

        //in case of error text needs to be white
        graphics.setColor(Color.white);
        if (currentImage != null) {
            try {
                BufferedImage image = ImageIO.read(currentImage.toFile());
                if (image != null) {
                    image = Thumbnails.of(image).size(dimension.width, dimension.height).asBufferedImage();
                    graphics.drawImage(image, null, dimension.width / 2 - image.getWidth() / 2, dimension.height / 2 - image.getHeight() / 2);
                } else {
                    graphics.drawString("Couldn't find image to display, check your settings", 10, 100);
                }
            } catch (Exception e) {
                graphics.drawString("Couldn't find image to display, check your settings", 10, 100);
            }
        }

    }


    @Override
    public String getName() {
        return "Photo Frame";
    }

    @Override
    public String getId() {
        return "com.ftpix.nowplaying.plugins.PhotoFrame";
    }

    @Override
    public void init(Map<String, String> settings) {

        path = Paths.get(settings.get(SETTINGS_LOCATION));

        try {
            refreshRate = Integer.parseInt(settings.get(SETTING_REFRESH_RATE));
        } catch (Exception e) {
            //using default value
        }

    }

    @Override
    public List<String> validateSettings(Map<String, String> settings) {

        Path path = Paths.get(settings.get(SETTINGS_LOCATION));

        if (Files.notExists(path)) {
            return List.of("Given path doesn't exist");
        }

        if (!Files.isReadable(path)) {
            return List.of("Given path is not readable");
        }

        if (!Files.isDirectory(path)) {
            return List.of("Given path is not a directory");
        }

        return null;
    }

    @Override
    public List<Setting> getSettings() {
        Setting path = new Setting();
        path.setLabel("Folder containing the pictures");
        path.setName(SETTINGS_LOCATION);
        path.setType(SettingType.TEXT);

        Setting refreshRate = new Setting();
        refreshRate.setLabel("Picture refresh rate");
        refreshRate.setDescription("How often in minutes the picture should change while on this mode");
        refreshRate.setType(SettingType.TEXT);
        refreshRate.setName(SETTING_REFRESH_RATE);


        return List.of(path, refreshRate);
    }

    @Override
    public void stop() {

    }
}
