package com.github.whz.hcnetsdk.handler;

import com.github.whz.hcnetsdk.HCNetSDK;
import com.github.whz.hcnetsdk.model.FaceSnapEvent;
import com.github.whz.hcnetsdk.model.FaceSnapInfo;
import com.github.whz.hcnetsdk.util.InnerUtils;
import com.github.whz.hcnetsdk.util.JnaUtils;
import com.sun.jna.Pointer;

/**
 * 人脸抓拍事件处理.
 */
public abstract class AbstractFaceSnapHandler extends AbstractHandler {

    public abstract void handle(FaceSnapEvent event);

    @Override
    public boolean accept(long command) {
        return command == HCNetSDK.COMM_UPLOAD_FACESNAP_RESULT;
    }

    @Override
    public void invoke(int lCommand, HCNetSDK.NET_DVR_ALARMER pAlarmer, Pointer pAlarmInfo, int dwBufLen, Pointer pUser) {
        if (accept(lCommand)) {
            FaceSnapEvent event = new FaceSnapEvent();
            event.setDeviceInfo(resolveDeviceInfo(pAlarmer));
            event.setFaceSnapInfo(resolveFaceSnapInfo(pAlarmInfo));
            this.handle(event);
        }
    }

    // 解析身份证信息
    private FaceSnapInfo resolveFaceSnapInfo(Pointer pAlarmInfo) {
        HCNetSDK.NET_VCA_FACESNAP_RESULT strFaceSnapInfo = new HCNetSDK.NET_VCA_FACESNAP_RESULT();
        JnaUtils.pointerToStructure(pAlarmInfo, strFaceSnapInfo);
        HCNetSDK.NET_VCA_RECT strRect = strFaceSnapInfo.struRect;
        HCNetSDK.NET_VCA_HUMAN_FEATURE strFaceFeature = strFaceSnapInfo.struFeature;

        FaceSnapInfo faceInfo = new FaceSnapInfo();

        // 抓拍信息
        faceInfo.setFaceScore(strFaceSnapInfo.dwFaceScore);
        if (strFaceSnapInfo.dwFacePicLen > 0) {
            byte[] faceBytes = JnaUtils.pointerToBytes(strFaceSnapInfo.pBuffer1, strFaceSnapInfo.dwFacePicLen);
            faceInfo.setFaceImageBytes(faceBytes);
        }
        if (strFaceSnapInfo.dwBackgroundPicLen > 0) {
            byte[] backgroundBytes = JnaUtils
                    .pointerToBytes(strFaceSnapInfo.pBuffer2, strFaceSnapInfo.dwBackgroundPicLen);
            faceInfo.setBackgroundImageBytes(backgroundBytes);
        }
        int absTime = strFaceSnapInfo.dwAbsTime;
        faceInfo.setSnapTimestamp(InnerUtils.hikAbsTimeToTimestamp(absTime));
        faceInfo.setFacePicId(strFaceSnapInfo.dwFacePicID);
        faceInfo.setStayDurationMs((long) (strFaceSnapInfo.fStayDuration * 1000));
        faceInfo.setRepeatTimes(strFaceSnapInfo.byRepeatTimes);

        // 人脸位置.
        FaceSnapInfo.FaceRect faceRect = new FaceSnapInfo.FaceRect(strRect.fX, strRect.fY, strRect.fWidth, strRect.fHeight);
        faceInfo.setFaceRect(faceRect);

        // 人脸特征
        FaceSnapInfo.FaceFuture faceFuture = new FaceSnapInfo.FaceFuture();
        faceInfo.setFaceFuture(faceFuture);
        faceFuture.setAge(strFaceFeature.byAge);
        faceFuture.setEyeGlass(strFaceFeature.byEyeGlass);
        faceFuture.setAgeGroup(strFaceFeature.byAgeGroup);
        faceFuture.setAgeDeviation(strFaceFeature.byAgeDeviation);
        return faceInfo;
    }
}
