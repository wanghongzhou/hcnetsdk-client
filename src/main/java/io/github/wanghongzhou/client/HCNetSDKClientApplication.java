package io.github.wanghongzhou.client;

import io.github.wanghongzhou.client.view.MainView;
import io.github.wanghongzhou.javafx.application.AbstractApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author Brian
 */
@SpringBootApplication
public class HCNetSDKClientApplication extends AbstractApplication {

    public static void main(String[] args) {
        launch(HCNetSDKClientApplication.class, MainView.class, args);
    }
}
