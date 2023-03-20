package io.github.wanghongzhou.hcnetsdk.operations;


/**
 * 云台操作.
 */
public interface PtzOperations extends Operations {

    /**
     * 云台控制.
     */
    HikResult<Void> control(int command, int stop, int speed);

    /**
     * 云台控制开始
     */
    HikResult<Void> controlStart(int command, int speed);

    /**
     * 云台控制停止
     */
    HikResult<Void> controlStop(int command, int speed);

    /**
     * 云台点位控制.
     */
    HikResult<Void> preset(int presetCommand, int presetIndex);

    /**
     * 云台点位设置.
     */
    HikResult<Void> presetSet(int presetIndex);

    /**
     * 云台点位清除.
     */
    HikResult<Void> presetClean(int presetIndex);

    /**
     * 云台点位跳转.
     */
    HikResult<Void> presetGoto(int presetIndex);

    /**
     * 云台巡航。
     */
    HikResult<Void> cruise(int cruiseCommand, int cruiseRoute, int cruisePoint, int speed);

    /**
     * 云台巡航运行.
     */
    HikResult<Void> cruiseRun(int cruiseRoute);

    /**
     * 云台巡航运行.
     */
    HikResult<Void> cruiseStop(int cruiseRoute);

    /**
     * 云台巡航添加点位.
     */
    HikResult<Void> cruiseFillPreset(int cruiseRoute, int cruisePoint, int speed);

    /**
     * 云台轨迹操作。
     */
    HikResult<Void> track(int trackCommand);/**/

    /**
     * 云台轨迹开始记录.
     */
    HikResult<Void> trackStartRecord();

    /**
     * 云台轨迹停止记录.
     */
    HikResult<Void> trackStopRecord();

    /**
     * 云台轨迹运行.
     */
    HikResult<Void> trackRun();

    /**
     * 云台图像缩放.
     */
    HikResult<Void> zoom(int xTop, int yTop, int xBottom, int yBottom);
}
