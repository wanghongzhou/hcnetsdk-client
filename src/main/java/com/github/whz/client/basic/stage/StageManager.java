package com.github.whz.client.basic.stage;

import com.github.whz.client.basic.application.AbstractApplication;
import com.github.whz.client.basic.view.AbstractFxmlView;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * @author Brian
 */
public class StageManager {

    @Getter
    protected final Stage primaryStage;

    protected final Map<Class<?>, Stage> popupWindows = new HashMap<>();

    public StageManager(Stage stage) {
        this.primaryStage = stage;
    }

    public void setIcons(List<Image> icons) {
        this.primaryStage.getIcons().addAll(icons);
    }

    public void setTitle(String title) {
        this.primaryStage.setTitle(title);
    }

    public void setWidth(double width) {
        this.primaryStage.setWidth(width);
    }

    public void setHeight(double height) {
        this.primaryStage.setHeight(height);
    }

    public void setResizable(boolean resizable) {
        this.primaryStage.setResizable(resizable);
    }

    public void setMaximized(boolean maximized) {
        this.primaryStage.setMaximized(maximized);
    }

    public void switchScene(final AbstractFxmlView<?> view) {
        switchScene(view.getView());
    }

    public void switchScene(final Parent parent) {
        switchScene(primaryStage, parent);
    }

    private void switchScene(final Stage stage, final Parent parent) {
        if (Objects.isNull(parent.getScene())) {
            stage.setScene(new Scene(parent));
        } else {
            stage.setScene(parent.getScene());
        }
    }

    public void showStage() {
        primaryStage.show();
    }

    public void showPopWindow(final AbstractFxmlView<?> view) {
        this.showPopWindow(view, stage -> {
        });
    }

    public void showPopWindow(final AbstractFxmlView<?> view, Consumer<Stage> settings) {
        Stage stage = new Stage();
        stage.initOwner(primaryStage);
        stage.centerOnScreen();
        stage.setOnHidden(event -> this.popupWindows.remove(view.getClass()));
        try {
            settings.accept(stage);
            stage.setTitle(view.getDefaultTitle());
            switchScene(stage, view.getView());
            stage.show();
            this.popupWindows.put(view.getClass(), stage);
        } catch (Exception exception) {
            AbstractApplication.showErrorAlert(exception);
        }
    }

    public void showModalPopWindow(final AbstractFxmlView<?> view) {
        this.showModalPopWindow(view, stage -> {
        });
    }

    public void showModalPopWindow(final AbstractFxmlView<?> view, Consumer<Stage> settings) {
        Stage stage = new Stage();
        stage.initOwner(primaryStage);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.centerOnScreen();
        stage.setOnHidden(event -> this.popupWindows.remove(view.getClass()));
        try {
            settings.accept(stage);
            stage.setTitle(view.getDefaultTitle());
            switchScene(stage, view.getView());
            stage.show();
            this.popupWindows.put(view.getClass(), stage);
        } catch (Exception exception) {
            AbstractApplication.showErrorAlert(exception);
        }
    }

    public void closePopWindow(final Class<? extends AbstractFxmlView<?>> viewClass) {
        Stage stage = this.popupWindows.remove(viewClass);
        if (Objects.nonNull(stage)) {
            stage.close();
        }
    }

    public Stage getOpenedPopWindow(final Class<? extends AbstractFxmlView<?>> viewClass) {
        return this.popupWindows.get(viewClass);
    }

    public boolean isOpenedPopWindow(final Class<? extends AbstractFxmlView<?>> viewClass) {
        return this.popupWindows.containsKey(viewClass);
    }
}
