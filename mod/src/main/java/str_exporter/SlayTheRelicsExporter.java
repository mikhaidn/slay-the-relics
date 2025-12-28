package str_exporter;

import basemod.*;
import basemod.interfaces.PostInitializeSubscriber;
import basemod.interfaces.PostRenderSubscriber;
import basemod.interfaces.StartGameSubscriber;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import str_exporter.client.EBSClient;
import str_exporter.config.AuthManager;
import str_exporter.config.Config;
import str_exporter.game_state.GameState;
import str_exporter.game_state.GameStateManager;
import str_exporter.game_state.integrations.Integrations;

import java.io.IOException;

@SpireInitializer
public class SlayTheRelicsExporter implements StartGameSubscriber, PostInitializeSubscriber,
        PostRenderSubscriber {
    public static final Logger logger = LogManager.getLogger(SlayTheRelicsExporter.class.getName());
    public static SlayTheRelicsExporter instance = null;
    private final Config config;
    private final EBSClient ebsClient;
    private final AuthManager authManager;
    private int tmpDelay = 0;
    private int tmpOffsetX = 0;
    private int tmpOffsetY = 0;
    private int tmpScaleX = 100;
    private int tmpScaleY = 100;
    private GameState gameState;
    private final GameStateManager gameStateManager;
    private Integrations integrations;


    public SlayTheRelicsExporter() {
        logger.info("Slay The Relics Exporter initialized!");
        BaseMod.subscribe(this);
        try {
            config = new Config();
            tmpDelay = config.getDelay();
            tmpOffsetX = (int) config.getTransformOffsetX();
            tmpOffsetY = (int) config.getTransformOffsetY();
            tmpScaleX = (int) config.getTransformScaleX();
            tmpScaleY = (int) config.getTransformScaleY();
            ebsClient = new EBSClient(config);
            authManager = new AuthManager(ebsClient, config);
            gameStateManager = new GameStateManager(ebsClient, config);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void initialize() {
        logger.info("initialize() called!");
        instance = new SlayTheRelicsExporter();
        instance.makeNewGameState();
    }

    public void makeNewGameState() {
        String user = this.config.getUser();
        logger.info("makeNewGameState() called with user: {}", user);
        this.gameState = new GameState(user);
        this.gameStateManager.setGameState(this.gameState);
    }


    @Override
    public void receivePostInitialize() {
        ModPanel settingsPanel = new ModPanel();

        ModLabel
                label1 =
                new ModLabel("Use the slider below to set encoding delay of your PC.",
                        400.0f,
                        700.0f,
                        settingsPanel,
                        (me) -> {
                        });
        ModLabel
                label2 =
                new ModLabel("With this set to 0, the extension will be ahead of what the stream displays.",
                        400.0f,
                        650.0f,
                        settingsPanel,
                        (me) -> {
                        });
        ModSlider slider = new ModSlider("Delay", 500f, 600, 10000f, "ms", settingsPanel, (me) -> {
            tmpDelay = (int) (me.value * me.multiplier);
        });

        ModLabel transformLabel = new ModLabel("Transform (for non-fullscreen layouts):",
                400.0f, 550.0f, settingsPanel, (me) -> {});

        ModSlider offsetXSlider = new ModSlider("X Offset", 400f, 500, 100f, "%", settingsPanel, (me) -> {
            tmpOffsetX = (int) (me.value * me.multiplier);
        });

        ModSlider offsetYSlider = new ModSlider("Y Offset", 400f, 450, 100f, "%", settingsPanel, (me) -> {
            tmpOffsetY = (int) (me.value * me.multiplier);
        });

        ModSlider scaleXSlider = new ModSlider("Width Scale", 700f, 500, 200f, "%", settingsPanel, (me) -> {
            tmpScaleX = (int) (me.value * me.multiplier);
        });

        ModSlider scaleYSlider = new ModSlider("Height Scale", 700f, 450, 200f, "%", settingsPanel, (me) -> {
            tmpScaleY = (int) (me.value * me.multiplier);
        });

        ModLabeledButton btn = new ModLabeledButton("Save", 400f, 380f, settingsPanel, (me) -> {
            try {
                config.setDelay(tmpDelay);
                config.setTransformOffsetX(tmpOffsetX);
                config.setTransformOffsetY(tmpOffsetY);
                config.setTransformScaleX(tmpScaleX);
                config.setTransformScaleY(tmpScaleY);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        ModLabeledButton oauthBtn = new ModLabeledButton("Connect with Twitch", 575f, 380f, settingsPanel, (me) -> {
            authManager.updateAuth(this::makeNewGameState);
        });

        ModStatusImage statusImage = new ModStatusImage(950f, 380f, authManager.healthy, authManager.inProgress);

        settingsPanel.addUIElement(label1);
        settingsPanel.addUIElement(slider);
        settingsPanel.addUIElement(label2);
        settingsPanel.addUIElement(transformLabel);
        settingsPanel.addUIElement(offsetXSlider);
        settingsPanel.addUIElement(offsetYSlider);
        settingsPanel.addUIElement(scaleXSlider);
        settingsPanel.addUIElement(scaleYSlider);
        settingsPanel.addUIElement(btn);
        settingsPanel.addUIElement(oauthBtn);
        settingsPanel.addUIElement(statusImage);

        slider.setValue(config.getDelay() * 1.0f / slider.multiplier);
        offsetXSlider.setValue(config.getTransformOffsetX() * 1.0f / offsetXSlider.multiplier);
        offsetYSlider.setValue(config.getTransformOffsetY() * 1.0f / offsetYSlider.multiplier);
        scaleXSlider.setValue(config.getTransformScaleX() * 1.0f / scaleXSlider.multiplier);
        scaleYSlider.setValue(config.getTransformScaleY() * 1.0f / scaleYSlider.multiplier);

        BaseMod.registerModBadge(ImageMaster.loadImage("SlayTheRelicsExporterResources/img/str_32x32.png"),
                "Slay the Relics Exporter",
                "vmService",
                "This mod exports data to Slay the Relics Twitch extension. See the extension config on Twitch for setup instructions.",
                settingsPanel);

        this.integrations = new Integrations();
        this.gameStateManager.start();
    }

    @Override
    public void receivePostRender(SpriteBatch spriteBatch) {
        long lastSuccessRequest = ebsClient.lastSuccessRequest.get();
        if (this.authManager.inProgress.get()) {
            this.authManager.healthy.set(true);
        } else {
            this.authManager.healthy.set(System.currentTimeMillis() - lastSuccessRequest < 2000);
        }
        this.gameStateManager.postRender();
    }

    @Override
    public void receiveStartGame() {
        this.gameState.resetState();
    }
}
