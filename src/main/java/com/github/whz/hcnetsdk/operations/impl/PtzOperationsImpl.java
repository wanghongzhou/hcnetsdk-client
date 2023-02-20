package com.github.whz.hcnetsdk.operations.impl;


import com.github.whz.hcnetsdk.HCNetSDK;
import com.github.whz.hcnetsdk.operations.HikResult;
import com.github.whz.hcnetsdk.operations.PtzOperations;
import com.github.whz.hcnetsdk.model.Token;
import com.sun.jna.NativeLong;

/**
 * 云台操作.
 */
public class PtzOperationsImpl extends AbstractOperations implements PtzOperations {

    private final Token token;
    private static final int channel = 1;

    public PtzOperationsImpl(Token token, HCNetSDK hcNetSDK) {
        super(hcNetSDK);
        this.token = token;
    }

    @Override
    public HikResult<Void> control(int command, int stop, int speed) {
        if (!getHcnetsdk().NET_DVR_PTZControlWithSpeed_Other(token.getUserId(), channel, command, stop, speed)) {
            return lastError();
        }
        return HikResult.ok();
    }

    @Override
    public HikResult<Void> controlStart(int command, int speed) {
        return control(command, 0, speed);
    }

    @Override
    public HikResult<Void> controlStop(int command, int speed) {
        return control(command, 1, speed);
    }

    @Override
    public HikResult<Void> preset(int presetCommand, int presetIndex) {
        if (!getHcnetsdk().NET_DVR_PTZPreset_Other(token.getUserId(), channel, presetCommand, presetIndex)) {
            return lastError();
        }
        return HikResult.ok();
    }

    @Override
    public HikResult<Void> presetSet(int presetIndex) {
        return preset(8, presetIndex);
    }

    @Override
    public HikResult<Void> presetClean(int presetIndex) {
        return preset(9, presetIndex);
    }

    @Override
    public HikResult<Void> presetGoto(int presetIndex) {
        return preset(39, presetIndex);
    }


    @Override
    public HikResult<Void> cruise(int cruiseCommand, int cruiseRoute, int cruisePoint, int speed) {
        if (!getHcnetsdk().NET_DVR_PTZCruise_Other(token.getUserId(), channel, cruiseCommand, (byte) cruiseRoute, (byte) cruisePoint, (byte) speed)) {
            return lastError();
        }
        return HikResult.ok();
    }

    @Override
    public HikResult<Void> cruiseRun(int cruiseRoute) {
        return cruise(37, cruiseRoute, 0, 0);
    }

    @Override
    public HikResult<Void> cruiseStop(int cruiseRoute) {
        return cruise(38, cruiseRoute, 0, 0);
    }

    @Override
    public HikResult<Void> cruiseFillPreset(int cruiseRoute, int cruisePoint, int speed) {
        return cruise(30, cruiseRoute, cruisePoint, speed);
    }

    @Override
    public HikResult<Void> track(int trackCommand) {
        if (!getHcnetsdk().NET_DVR_PTZTrack_Other(token.getUserId(), channel, trackCommand)) {
            return lastError();
        }
        return HikResult.ok();
    }

    @Override
    public HikResult<Void> trackStartRecord() {
        return track(34);
    }

    @Override
    public HikResult<Void> trackStopRecord() {
        return track(35);
    }

    @Override
    public HikResult<Void> trackRun() {
        return track(35);
    }

    @Override
    public HikResult<Void> zoom(int xTop, int yTop, int xBottom, int yBottom) {
        HCNetSDK.NET_DVR_POINT_FRAME point = new HCNetSDK.NET_DVR_POINT_FRAME();
        point.xTop = xTop;
        point.yTop = yTop;
        point.xBottom = xBottom;
        point.yBottom = yBottom;
        point.write();
        if (!getHcnetsdk().NET_DVR_PTZSelZoomIn_EX(token.getUserId(), channel, point)) {
            return lastError();
        }
        return HikResult.ok();
    }
}
