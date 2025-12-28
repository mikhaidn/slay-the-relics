package str_exporter.config;

import com.evacipated.cardcrawl.modthespire.lib.SpireConfig;
import com.google.gson.Gson;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

public class Config {
    private static final String API_URL_SETTINGS = "api_url";
    private static final String DELAY_SETTINGS = "delay";
    private static final String OAUTH_SETTINGS = "oauth";
    private static final String USER_SETTINGS = "user";
    private static final String TRANSFORM_OFFSET_X = "transform_offset_x";
    private static final String TRANSFORM_OFFSET_Y = "transform_offset_y";
    private static final String TRANSFORM_SCALE_X = "transform_scale_x";
    private static final String TRANSFORM_SCALE_Y = "transform_scale_y";
    public final Gson gson = new Gson();
    private final SpireConfig config;

    public Config() throws IOException {
        Properties strDefaultSettings = new Properties();
        strDefaultSettings.setProperty(DELAY_SETTINGS, "150");
        strDefaultSettings.setProperty(API_URL_SETTINGS, "https://slay-the-relics.baalorlord.tv");
        strDefaultSettings.setProperty(TRANSFORM_OFFSET_X, "0");
        strDefaultSettings.setProperty(TRANSFORM_OFFSET_Y, "0");
        strDefaultSettings.setProperty(TRANSFORM_SCALE_X, "100");
        strDefaultSettings.setProperty(TRANSFORM_SCALE_Y, "100");

        config = new SpireConfig("slayTheRelics", "slayTheRelicsExporterConfig", strDefaultSettings);
        config.load();
    }

    public int getDelay() {
        return config.getInt(DELAY_SETTINGS);
    }

    public void setDelay(int delay) throws IOException {
        config.setInt(DELAY_SETTINGS, delay);
        config.save();
    }

    public URL getApiUrl() throws MalformedURLException {
        return new URL(config.getString(API_URL_SETTINGS));
    }

    public String getOathToken() {
        return config.getString(OAUTH_SETTINGS);
    }

    public void setOathToken(String oathToken) throws IOException {
        config.setString(OAUTH_SETTINGS, oathToken);
        config.save();
    }

    public String getUser() {
        return config.getString(USER_SETTINGS);
    }

    public void setUser(String user) throws IOException {
        config.setString(USER_SETTINGS, user);
        config.save();
    }

    public boolean areCredentialsValid() {
        String token = getOathToken();
        String user = getUser();
        return token != null && user != null && !token.isEmpty() && !user.isEmpty();
    }

    public float getTransformOffsetX() {
        return config.getInt(TRANSFORM_OFFSET_X);
    }

    public void setTransformOffsetX(int offsetX) throws IOException {
        config.setInt(TRANSFORM_OFFSET_X, offsetX);
        config.save();
    }

    public float getTransformOffsetY() {
        return config.getInt(TRANSFORM_OFFSET_Y);
    }

    public void setTransformOffsetY(int offsetY) throws IOException {
        config.setInt(TRANSFORM_OFFSET_Y, offsetY);
        config.save();
    }

    public float getTransformScaleX() {
        return config.getInt(TRANSFORM_SCALE_X);
    }

    public void setTransformScaleX(int scaleX) throws IOException {
        config.setInt(TRANSFORM_SCALE_X, scaleX);
        config.save();
    }

    public float getTransformScaleY() {
        return config.getInt(TRANSFORM_SCALE_Y);
    }

    public void setTransformScaleY(int scaleY) throws IOException {
        config.setInt(TRANSFORM_SCALE_Y, scaleY);
        config.save();
    }
}
