package com.ftpix.nowplaying.plugins.gtsports;

import com.ftpix.nowplaying.NowPlayingPlugin;
import com.ftpix.nowplaying.Setting;
import com.ftpix.nowplaying.Utils;
import net.coobird.thumbnailator.Thumbnails;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import spark.utils.IOUtils;

import javax.imageio.ImageIO;
import javax.swing.text.html.Option;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.ftpix.nowplaying.Utils.getPercentOf;

public class GTSportsPlugin implements NowPlayingPlugin<GTSportRaces> {
    @Override
    public GTSportRaces getNowPlayingContent() throws Exception {
        return getRaces();
    }

    @Override
    public void getNowPlayingImage(GTSportRaces races, Graphics2D graphics, Dimension dimension, double scale) throws Exception {

        int middleWidth = dimension.width - getPercentOf(dimension, 66).width;
        int width33Percent = getPercentOf(dimension, 33).width;

        Optional<DailyRace> raceA = Optional.ofNullable(races.getRaceA());
        if (raceA.isPresent()) {
            drawSingleRace(raceA.get(), graphics, 0, 0, width33Percent, dimension.height);
        }

        Optional<DailyRace> raceB = Optional.ofNullable(races.getRaceB());
        if (raceB.isPresent()) {
            drawSingleRace(raceB.get(), graphics, width33Percent, 0, middleWidth, dimension.height);
        }

        Optional<DailyRace> raceC = Optional.ofNullable(races.getRaceC());
        if (raceC.isPresent()) {
            drawSingleRace(raceC.get(), graphics, width33Percent + middleWidth, 0, width33Percent, dimension.height);
        }


        graphics.setColor(Color.BLACK);
        graphics.drawLine(width33Percent, 0, width33Percent, dimension.height);
        graphics.drawLine(width33Percent + middleWidth, 0, width33Percent + middleWidth, dimension.height);

    }


    private void drawSingleRace(DailyRace race, Graphics2D g, int x, int y, int width, int height) throws IOException {
        Dimension dimension = new Dimension(width, height);
        //Drawing image
        BufferedImage image = ImageIO.read(new URL(race.getImageUrl()));
        image = Thumbnails.of(image).height(getPercentOf(dimension, 75).height).asBufferedImage();

        if (image.getWidth() > width) {
            image = image.getSubimage((image.getWidth() - width) / 2, 0, width, image.getHeight());
        }

        g.drawImage(image, x + width / 2 - image.getWidth() / 2, 0, null);

        int fontSize = getPercentOf(dimension, 4).height;
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, fontSize));
        Rectangle2D stringBounds = g.getFont().getStringBounds(race.getRaceType().getLabel(), g.getFontRenderContext());


        //overlay
        Color startColor = new Color(0, 0, 0, 0.5f);
        Color endColor = new Color(0, 0, 0, 0);

        GradientPaint gradient = new GradientPaint(0, 0, startColor, 0, (float) stringBounds.getHeight(), endColor);
        g.setPaint(gradient);

        g.fillRect(x, 0, width, (int) (stringBounds.getHeight() + getPercentOf(dimension, 1).height));

        g.setColor(Color.WHITE);

        g.drawString(race.getRaceType().getLabel(), (int) (width - stringBounds.getWidth() - getPercentOf(dimension, 5).width) + x, (int) (stringBounds.getHeight()));


        //car class
        fontSize = getPercentOf(dimension, 8).height;

        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, fontSize));
        stringBounds = g.getFont().getStringBounds(race.getCarClass(), g.getFontRenderContext());

        int textX = x + getPercentOf(dimension, 5).width;
        int textY = getPercentOf(dimension, 70).height;
        int textMaxWidth = width - getPercentOf(dimension, 10).width;
        Utils.fitString(race.getCarClass(), fontSize, textMaxWidth, g, textX, textY);

        fontSize = getPercentOf(dimension, 3).height;
        textY = getPercentOf(dimension, 74).height;
        Utils.fitString(race.getTrackName(), fontSize, textMaxWidth, g, textX, textY);


        //info
        g.setColor(new Color(25, 25, 25));
        g.fillRect(x, getPercentOf(dimension, 75).height, width, height - getPercentOf(dimension, 75).height);
        g.setColor(Color.white);

        fontSize = getPercentOf(dimension, 10).height;

        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, fontSize));
        stringBounds = g.getFont().getStringBounds(race.getTime(), g.getFontRenderContext());
        textY += getPercentOf(dimension, 11).height;
        Utils.fitString(race.getTime(), fontSize, textMaxWidth, g, x + (int) (width / 2 - stringBounds.getWidth() / 2), textY);

        //draw separator
        textY += getPercentOf(dimension, 2).height;

        g.setColor(new Color(50, 50, 50));
        g.drawLine(x, textY, width + x, textY);

        fontSize = getPercentOf(dimension, 2).height;
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, fontSize));
        stringBounds = g.getFont().getStringBounds("laps", g.getFontRenderContext());

        textY += getPercentOf(dimension, 1).height + stringBounds.getHeight();
        textMaxWidth = width / 3 - getPercentOf(dimension, 5).width;

        g.setColor(new Color(150, 150, 150));
        Utils.fitString("Laps", fontSize, textMaxWidth, g, textX, textY);
        Utils.fitString("No. of Cars", fontSize, textMaxWidth, g, textX + width / 3, textY);
        Utils.fitString("Duration", fontSize, textMaxWidth, g, textX + (width / 3) * 2, textY);

        g.setColor(Color.white);
        fontSize = getPercentOf(dimension, 4).height;
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, fontSize));
        stringBounds = g.getFont().getStringBounds("laps", g.getFontRenderContext());
        textY += getPercentOf(dimension, 1).height + stringBounds.getHeight();

        Utils.fitString(Integer.toString(race.getLaps()), fontSize, textMaxWidth, g, textX, textY);
        Utils.fitString(Integer.toString(race.getCars()), fontSize, textMaxWidth, g, textX + width / 3, textY);
        Utils.fitString(race.getDuration(), fontSize, textMaxWidth, g, textX + (width / 3) * 2, textY);

    }


    @Override
    public String getName() {
        return "GT Sport races";
    }

    @Override
    public String getId() {
        return "com.ftpix.nowplaying.gtsports";
    }

    @Override
    public void init(Map<String, String> settings) {

    }

    @Override
    public List<String> validateSettings(Map<String, String> settings) {
        return null;
    }

    @Override
    public List<Setting> getSettings() {
        return null;
    }

    @Override
    public void stop() {

    }


    private GTSportRaces getRaces() throws IOException {

        GTSportRaces races = new GTSportRaces();
        Document doc = Jsoup.connect("https://gtsportraces.com").get();

        Elements daily = doc.getElementsByClass("daily");

        daily.forEach(d -> {
            DailyRace race = new DailyRace();
            String image = d.getElementsByClass("track-image").get(0).attributes().get("style").split("url\\(")[1].replaceAll("\\)", "");
            race.setImageUrl(image);


            Optional.ofNullable(d.getElementsByClass("title"))
                    .filter(e -> e.size() == 1)
                    .map(e -> e.get(0).getElementsByTag("h3"))
                    .filter(e -> e.size() == 1)
                    .map(e -> e.text())
                    .ifPresent(e -> {
                        RaceType raceType = RaceType.valueOf(e.toUpperCase().replaceAll(" ", "_"));
                        race.setRaceType(raceType);
                    });

            Optional.ofNullable(d.getElementsByClass("carClass"))
                    .filter(e -> e.size() == 1)
                    .map(e -> e.get(0))
                    .map(e -> e.getElementsByTag("h2"))
                    .filter(e -> e.size() == 1)
                    .map(e -> e.get(0))
                    .map(Element::text)
                    .ifPresent(race::setCarClass);

            Optional.ofNullable(d.getElementsByClass("track"))
                    .filter(e -> e.size() == 1)
                    .map(e -> e.get(0))
                    .map(Element::text)
                    .ifPresent(race::setTrackName);

            Optional.ofNullable(d.getElementsByClass("start-time"))
                    .filter(e -> e.size() == 1)
                    .map(e -> e.get(0).getElementsByClass("col-4"))
                    .filter(e -> e.size() == 1)
                    .map(e -> e.get(0).getElementsByTag("h5"))
                    .filter(e -> e.size() == 1)
                    .map(e -> e.get(0).text())
                    .ifPresent(e -> {

                    });


            Optional.ofNullable(d.getElementsByClass("race-info-details"))
                    .filter(e -> e.size() == 1)
                    .map(e -> e.get(0))
                    .ifPresent(info -> {
                        Optional.ofNullable(info.getElementsByClass("col-3"))
                                .filter(e -> e.size() == 1)
                                .map(e -> e.get(0).getElementsByTag("h6"))
                                .filter(e -> e.size() == 1)
                                .map(e -> e.get(0).text())
                                .map(e -> Integer.parseInt(e))
                                .ifPresent(race::setLaps);

                        Optional.ofNullable(info.getElementsByClass("col-4"))
                                .filter(e -> e.size() == 1)
                                .map(e -> e.get(0).getElementsByTag("h6"))
                                .filter(e -> e.size() == 1)
                                .map(e -> e.get(0).text())
                                .map(e -> Integer.parseInt(e))
                                .ifPresent(race::setCars);


                        Optional.ofNullable(info.getElementsByClass("col-5"))
                                .filter(e -> e.size() == 1)
                                .map(e -> e.get(0).getElementsByTag("h6"))
                                .filter(e -> e.size() == 1)
                                .map(e -> e.get(0).text())
                                .ifPresent(race::setDuration);

                    });

            switch (race.getRaceType()) {
                case RACE_A:
                    races.setRaceA(race);
                    break;
                case RACE_B:
                    races.setRaceB(race);
                    break;
                case RACE_C:
                    races.setRaceC(race);
                    break;
            }

        });

        return races;
    }
}
