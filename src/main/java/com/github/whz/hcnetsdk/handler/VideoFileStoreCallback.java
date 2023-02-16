package com.github.whz.hcnetsdk.handler;

import com.github.whz.hcnetsdk.HCNetSDK;
import com.github.whz.hcnetsdk.util.InnerUtils;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.ByteByReference;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;

/**
 * 视频存储消息回调
 */
public class VideoFileStoreCallback implements HCNetSDK.FRealDataCallBack_V30 {

    /**
     * 音频头数据.
     */
    private byte[] header;

    /**
     * 基本目录.
     */
    private final String baseDir;

    public VideoFileStoreCallback(String baseDir) {
        this.baseDir = baseDir;
    }

    @Override
    public void invoke(NativeLong lRealHandle, int dwDataType, ByteByReference pBuffer, int dwBufSize, Pointer pUser) {
        byte[] bytes = pBuffer.getPointer().getByteArray(0, dwBufSize);
        if (dwDataType == HCNetSDK.NET_DVR_SYSHEAD) {
            // 头数据
            header = bytes;
        } else if (dwDataType == HCNetSDK.NET_DVR_STREAMDATA && dwBufSize > 0) {
            // 视频流
            String videoPath = getVideoFilePath();
            if (!Files.exists(Path.of(videoPath))) {
                InnerUtils.writeFile(header, videoPath);
            }
            InnerUtils.writeFile(bytes, videoPath);
        }
    }

    /**
     * 获取视频路径.
     */
    protected String getVideoFilePath() {
        return baseDir + File.separator + InnerUtils.formatDate(new Date(), "yyyyMMdd") + ".mp4";
    }
}
