package com.github.whz.client.basic.view;

import com.google.common.base.CaseFormat;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.StageStyle;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * @author Brian
 */
@Slf4j
@Setter
@Getter
@ToString
public class AbstractFxmlView<T> implements ApplicationContextAware {

    protected FXMLLoader fxmlLoader;
    protected ApplicationContext applicationContext;

    protected final URL resource;
    protected final String fxmlRoot;
    protected final FXMLView annotation;
    protected final ObjectProperty<T> controller;
    protected final ResourceBundle bundle;

    public AbstractFxmlView() {
        this.controller = new SimpleObjectProperty<>();
        this.annotation = this.getClass().getAnnotation(FXMLView.class);
        this.fxmlRoot = "/" + this.getClass().getPackage().getName().replace('.', '/') + "/";
        this.resource = getURLResource(annotation);
        this.bundle = getResourceBundle(annotation);
    }

    private URL getURLResource(final FXMLView annotation) {
        if (Objects.nonNull(annotation) && StringUtils.hasText(annotation.value())) {
            return getClass().getResource(annotation.value());
        } else {
            return getClass().getResource(fxmlRoot + getConventionalName(".fxml"));
        }
    }

    private ResourceBundle getResourceBundle(final FXMLView annotation) {
        if (Objects.nonNull(annotation) && StringUtils.hasText(annotation.bundle())) {
            return ResourceBundle.getBundle(annotation.bundle());
        } else {
            try {
                return ResourceBundle.getBundle(getClass().getPackage().getName() + "." + getConventionalName());
            } catch (MissingResourceException e) {
                return null;
            }
        }
    }

    private String getConventionalName(String suffix) {
        return getConventionalName() + suffix;
    }

    private String getConventionalName() {
        return CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_HYPHEN, getClass().getSimpleName());
    }

    public Parent getView() {
        if (Objects.isNull(fxmlLoader)) {
            this.fxmlLoader = new FXMLLoader(resource, bundle);
            this.fxmlLoader.setControllerFactory((Class<?> type) -> applicationContext.getBean(type));
            try {
                fxmlLoader.load();
            } catch (final IOException | IllegalStateException e) {
                throw new IllegalStateException("Cannot load " + getConventionalName(), e);
            }
            Parent parent = fxmlLoader.getRoot();
            controller.set(fxmlLoader.getController());

            // add global css
            @SuppressWarnings("unchecked")
            List<String> cssList = applicationContext.getEnvironment().getProperty("javafx.css", List.class);
            if (Objects.nonNull(cssList)) {
                cssList.forEach(css -> parent.getStylesheets().add(Objects.requireNonNull(getClass().getResource(css)).toExternalForm()));
            }

            // add annotation css
            if (Objects.nonNull(annotation) && annotation.css().length > 0) {
                for (final String cssFile : annotation.css()) {
                    final URL uri = getClass().getResource(cssFile);
                    if (Objects.nonNull(uri)) {
                        parent.getStylesheets().add(uri.toExternalForm());
                        log.debug("css file added to parent: {}", cssFile);
                    } else {
                        log.warn("referenced {} css file could not be located", cssFile);
                    }
                }
            }

            // add default path css
            final URL uri = getClass().getResource(fxmlRoot + getConventionalName(".css"));
            if (Objects.nonNull(uri)) {
                parent.getStylesheets().add(uri.toExternalForm());
            }
        }
        return fxmlLoader.getRoot();
    }

    public void getView(final Consumer<Parent> consumer) {
        CompletableFuture.supplyAsync(this::getView, Platform::runLater).thenAccept(consumer);
    }

    public String getDefaultTitle() {
        if (Objects.nonNull(bundle)) {
            return bundle.getString(annotation.title());
        } else {
            return annotation.title();
        }
    }

    public StageStyle getDefaultStyle() {
        return StageStyle.valueOf(annotation.stageStyle().toUpperCase());
    }

    @Override
    public void setApplicationContext(@Nullable ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
