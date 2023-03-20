package io.github.wanghongzhou.client;

import io.github.wanghongzhou.client.util.RTSPVideoPlayer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * @author Brian
 */
public class RTSPVideoPlayerApplication extends Application {

    private static volatile Thread playThread;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        final ImageView imageView = new ImageView();
        imageView.fitWidthProperty().bind(primaryStage.widthProperty());
        imageView.fitHeightProperty().bind(primaryStage.heightProperty());

        primaryStage.setTitle("RTSP Video Player");
        primaryStage.setScene(new Scene(new StackPane(imageView), 640, 480));
        primaryStage.show();

        playThread = new Thread(new RTSPVideoPlayer("rtsp://admin:JL654321@192.168.60.164:554/Streaming/Channels/1", "video/test.mp4", imageView));
        playThread.start();
    }

    @Override
    public void stop() {
        playThread.interrupt();
    }
}
