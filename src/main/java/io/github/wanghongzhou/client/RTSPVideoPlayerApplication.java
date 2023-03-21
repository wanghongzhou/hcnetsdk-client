package io.github.wanghongzhou.client;

import io.github.wanghongzhou.client.util.RTSPVideoPlayer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * @author Brian
 */
public class RTSPVideoPlayerApplication extends Application {

    private RTSPVideoPlayer videoPlayer;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        final ImageView imageView = new ImageView();
        imageView.fitWidthProperty().bind(primaryStage.widthProperty());
        imageView.fitHeightProperty().bind(primaryStage.heightProperty());

        videoPlayer = new RTSPVideoPlayer(imageView);
        final Button recordBtn = new Button("start recording");
        recordBtn.setOnAction(event -> {
            if (videoPlayer.getRecording().get()) {
                videoPlayer.stopRecording();
                recordBtn.setText("start recording");
            } else {
                videoPlayer.startRecording("video/" + System.currentTimeMillis() + ".mp4");
                recordBtn.setText("stop recording");
            }
        });

        primaryStage.setTitle("RTSP Video Player");
        primaryStage.setScene(new Scene(new StackPane(imageView, recordBtn), 640, 480));
        primaryStage.show();

        videoPlayer.startPlaying("rtsp://admin:JL654321@192.168.60.164:554/Streaming/Channels/1");
    }

    @Override
    public void stop() {
        videoPlayer.stopPlaying();
    }
}
