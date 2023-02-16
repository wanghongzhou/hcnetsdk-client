package com.github.whz.client;

import com.github.whz.client.view.MainView;
import com.github.whz.client.basic.application.AbstractApplication;
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