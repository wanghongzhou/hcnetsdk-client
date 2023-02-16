package com.github.whz.client.basic.application;

import com.github.whz.client.basic.stage.StageManager;
import com.github.whz.client.basic.view.AbstractFxmlView;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * @author Brian
 */
@Slf4j
public class AbstractApplication extends Application {

    protected CompletableFuture<Runnable> splashIsShowing;

    protected static String[] savedArgs;
    protected static ConfigurableApplicationContext applicationContext;
    protected static Class<? extends AbstractFxmlView<?>> savedInitialView;
    protected static final List<Image> icons = new ArrayList<>();

    protected static SystemTray systemTray;
    protected static HostServices hostServices;
    protected static StageManager stageManager;

    public final static String KEY_TITLE = "javafx.title";
    public final static String KEY_APP_ICONS = "javafx.appicons";
    public final static String KEY_STAGE_WIDTH = "javafx.stage.width";
    public final static String KEY_STAGE_HEIGHT = "javafx.stage.height";
    public final static String KEY_STAGE_RESIZABLE = "javafx.stage.resizable";
    public final static String KEY_STAGE_MAXIMIZED = "javafx.stage.maximized";

    public AbstractApplication() {
        splashIsShowing = new CompletableFuture<>();
    }

    @Override
    public void init() {
        CompletableFuture.supplyAsync(() -> applicationContext = SpringApplication.run(this.getClass(), savedArgs)).whenComplete((ctx, throwable) -> {
            if (Objects.nonNull(throwable)) {
                log.error("Failed to load spring application context: ", throwable);
                Platform.runLater(() -> showErrorAlert(throwable));
            } else {
                Platform.runLater(() -> {
                    loadIcons(ctx);
                    launchApplicationView(ctx);
                });
            }
        }).thenAcceptBothAsync(splashIsShowing, (ctx, closeSplash) -> Platform.runLater(closeSplash));
    }

    @Override
    public void start(Stage stage) {
        stageManager = new StageManager(stage);
        hostServices = this.getHostServices();
        splashIsShowing.complete(() -> showStage(savedInitialView));
    }

    public static void showStage(final Class<? extends AbstractFxmlView<?>> viewClass) {
        try {
            Optional.ofNullable(applicationContext.getEnvironment().getProperty(KEY_TITLE, String.class)).ifPresent(stageManager::setTitle);
            Optional.ofNullable(applicationContext.getEnvironment().getProperty(KEY_STAGE_WIDTH, Double.class)).ifPresent(stageManager::setWidth);
            Optional.ofNullable(applicationContext.getEnvironment().getProperty(KEY_STAGE_HEIGHT, Double.class)).ifPresent(stageManager::setHeight);
            Optional.ofNullable(applicationContext.getEnvironment().getProperty(KEY_STAGE_RESIZABLE, Boolean.class)).ifPresent(stageManager::setResizable);
            Optional.ofNullable(applicationContext.getEnvironment().getProperty(KEY_STAGE_MAXIMIZED, Boolean.class)).ifPresent(stageManager::setMaximized);
            stageManager.setIcons(icons);
            stageManager.switchScene(applicationContext.getBean(viewClass));
            stageManager.showStage();
        } catch (Throwable t) {
            log.error("Failed to load application: ", t);
            showErrorAlert(t);
        }
    }

    public static void switchScene(final Class<? extends AbstractFxmlView<?>> viewClass) {
        stageManager.switchScene(applicationContext.getBean(viewClass));
    }

    public static void showPopWindow(final Class<? extends AbstractFxmlView<?>> viewClass) {
        stageManager.showPopWindow(applicationContext.getBean(viewClass));
    }

    public static void showPopWindow(final Class<? extends AbstractFxmlView<?>> viewClass, Consumer<Stage> settings) {
        stageManager.showPopWindow(applicationContext.getBean(viewClass));
    }

    public static void showModalPopWindow(final Class<? extends AbstractFxmlView<?>> viewClass) {
        stageManager.showModalPopWindow(applicationContext.getBean(viewClass));
    }

    public static void showModalPopWindow(final Class<? extends AbstractFxmlView<?>> viewClass, Consumer<Stage> settings) {
        stageManager.showModalPopWindow(applicationContext.getBean(viewClass), settings);
    }

    public static void closePopWindow(final Class<? extends AbstractFxmlView<?>> viewClass) {
        stageManager.closePopWindow(viewClass);
    }

    public static Stage getOpenedPopWindow(final Class<? extends AbstractFxmlView<?>> viewClass) {
        return stageManager.getOpenedPopWindow(viewClass);
    }

    public static boolean isOpenedPopWindow(final Class<? extends AbstractFxmlView<?>> viewClass) {
        return stageManager.isOpenedPopWindow(viewClass);
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        if (Objects.nonNull(applicationContext)) {
            applicationContext.close();
        }
    }

    public void launchApplicationView(final ConfigurableApplicationContext ctx) {

    }

    private void loadIcons(ConfigurableApplicationContext ctx) {
        try {
            @SuppressWarnings("unchecked") List<String> appIcons = ctx.getEnvironment().getProperty(KEY_APP_ICONS, List.class);
            if (!(Objects.isNull(appIcons) || appIcons.isEmpty())) {
                appIcons.forEach((s) -> icons.add(new Image(Objects.requireNonNull(getClass().getResource(s)).toExternalForm())));
            }
        } catch (Exception e) {
            log.error("Failed to load icons: ", e);
        }
    }

    public Collection<Image> loadDefaultIcons() {
        return Arrays.asList(new Image(Objects.requireNonNull(getClass().getResource("/icons/logo/logo_16x16.png")).toExternalForm()), new Image(Objects.requireNonNull(getClass().getResource("/icons/logo/logo_24x24.png")).toExternalForm()), new Image(Objects.requireNonNull(getClass().getResource("/icons/logo/logo_36x36.png")).toExternalForm()), new Image(Objects.requireNonNull(getClass().getResource("/icons/logo/logo_42x42.png")).toExternalForm()), new Image(Objects.requireNonNull(getClass().getResource("/icons/logo/logo_64x64.png")).toExternalForm()));
    }

    public static void launch(final Class<? extends AbstractApplication> appClass, final Class<? extends AbstractFxmlView<?>> view, final String[] args) {
        AbstractApplication.savedArgs = args;
        AbstractApplication.savedInitialView = view;

        if (SystemTray.isSupported()) {
            AbstractApplication.systemTray = SystemTray.getSystemTray();
        }

        Application.launch(appClass, args);
    }

    public static StageManager getStageManager() {
        return stageManager;
    }

    public static SystemTray getSystemTray() {
        return systemTray;
    }

    public static HostServices getAppHostServices() {
        return hostServices;
    }

    public static void showInfoAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message);
        alert.show();
    }

    public static void showErrorAlert(String message) {
        log.error("Error: {}", message);
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.show();
    }

    public static void showErrorAlert(Throwable throwable) {
        log.error("Error: ", throwable);
        Alert alert = new Alert(Alert.AlertType.ERROR, "Oops! An unrecoverable error occurred.\n" + "Please contact your software vendor.\n\n" + "The application will stop now.\n\n" + "Error: " + throwable.getMessage());
        alert.showAndWait().ifPresent(response -> Platform.exit());
    }
}
