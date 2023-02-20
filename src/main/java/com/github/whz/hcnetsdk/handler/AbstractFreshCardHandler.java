package com.github.whz.hcnetsdk.handler;

import com.github.whz.hcnetsdk.HCNetSDK;
import com.github.whz.hcnetsdk.model.FreshCardEvent;
import com.github.whz.hcnetsdk.model.IDCardInfo;
import com.sun.jna.Pointer;

import java.util.Calendar;
import java.util.Date;

/**
 * 刷证消息处理方法.
 */
public abstract class AbstractFreshCardHandler extends AbstractHandler {

    /**
     * 处理刷证事件
     */
    public abstract void handle(FreshCardEvent event);

    @Override
    public boolean accept(long command) {
        return command == HCNetSDK.COMM_ID_INFO_ALARM;
    }

    @Override
    public void invoke(int lCommand, HCNetSDK.NET_DVR_ALARMER pAlarmer, Pointer pAlarmInfo, int dwBufLen, Pointer pUser) {
        if (accept(lCommand)) {
            FreshCardEvent event = new FreshCardEvent();
            event.setDeviceInfo(resolveDeviceInfo(pAlarmer));
            event.setCardInfo(resolveIdCardInfo(pAlarmInfo));
            this.handle(event);
        }
    }

    // 解析身份证信息
    private IDCardInfo resolveIdCardInfo(Pointer pAlarmInfoo) {
        IDCardInfo cardInfo = new IDCardInfo();

        HCNetSDK.NET_DVR_ID_CARD_INFO_ALARM idCardInfoAlarm = new HCNetSDK.NET_DVR_ID_CARD_INFO_ALARM();
        idCardInfoAlarm.write();
        Pointer pCardInfo = idCardInfoAlarm.getPointer();
        pCardInfo.write(0, pAlarmInfoo.getByteArray(0, idCardInfoAlarm.size()), 0, idCardInfoAlarm.size());
        idCardInfoAlarm.read();

        HCNetSDK.NET_DVR_ID_CARD_INFO idCardCfg = idCardInfoAlarm.struIDCardCfg;
        cardInfo.setIdNumber(new String(idCardCfg.byIDNum).trim());
        cardInfo.setName(new String(idCardCfg.byName).trim());
        cardInfo.setAddress(new String(idCardCfg.byAddr).trim());
        cardInfo.setSex(idCardCfg.bySex);
        cardInfo.setNation(idCardCfg.byNation);
        cardInfo.setIssuingAuthority(new String(idCardCfg.byIssuingAuthority).trim());
        cardInfo.setTermValidity(idCardCfg.byTermOfValidity);

        cardInfo.setBirth(convertToDate(idCardCfg.struBirth));
        cardInfo.setValidityStartTime(convertToDate(idCardCfg.struStartDate));
        if (idCardCfg.byTermOfValidity == 0) {
            cardInfo.setValidityEndTime(convertToDate(idCardCfg.struEndDate));
        }
        return cardInfo;
    }

    private Date convertToDate(HCNetSDK.NET_DVR_DATE dvrDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, dvrDate.wYear);
        calendar.set(Calendar.MONTH, dvrDate.byMonth - 1);
        calendar.set(Calendar.DATE, dvrDate.byDay);
        return calendar.getTime();
    }
}
