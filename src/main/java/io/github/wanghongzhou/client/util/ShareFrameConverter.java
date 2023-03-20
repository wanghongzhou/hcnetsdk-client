package io.github.wanghongzhou.client.util;

import javafx.scene.image.*;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.Mat;

import java.nio.ByteBuffer;

import static org.opencv.imgproc.Imgproc.COLOR_BGR2BGRA;

/**
 * @author Brian
 */
public class ShareFrameConverter extends FrameConverter<Image> {

    protected ByteBuffer buffer;
    protected Mat javaCVMat = new Mat();
    protected OpenCVFrameConverter<Mat> javaCVConv = new OpenCVFrameConverter.ToMat();
    protected WritablePixelFormat<ByteBuffer> formatByte = PixelFormat.getByteBgraPreInstance();

    @Override
    public Frame convert(Image f) {
        throw new UnsupportedOperationException("conversion from Image to Frame not supported yet.");
    }

    @Override
    public Image convert(Frame frame) {
        int w = frame.imageWidth;
        int h = frame.imageHeight;

        Mat mat = javaCVConv.convert(frame);
        opencv_imgproc.cvtColor(mat, javaCVMat, COLOR_BGR2BGRA);

        if (buffer == null) {
            buffer = javaCVMat.createBuffer();
        }

        return new WritableImage(new PixelBuffer<>(w, h, buffer, formatByte));
    }
}
