package com.android.superli.btremote.receiver;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.android.superli.btremote.utils.ClsUtils;
import com.android.superli.btremote.utils.ToastUtils;
import com.youth.banner.util.LogUtils;

/**
 * Data :2022/9/1
 * Time:17:43
 * Author:shuij
 */
public  class BluetoothConnectReceiver extends BroadcastReceiver {
    String strPsw = "111111";

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
        if (intent.getAction().equals("android.bluetooth.device.action.PAIRING_REQUEST")) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                try {
                    /**
                     * cancelPairingUserInput（）取消用户输入密钥框，
                     * 个人觉得一般情况下不要和setPin（setPasskey、setPairingConfirmation、
                     * setRemoteOutOfBandData）一起用，
                     * 这几个方法都会remove掉map里面的key:value（<<<<<也就是互斥的>>>>>>）。
                     */

                    //1.确认配对
                    //ClsUtils.setPairingConfirmation(device.getClass(), device, true);
                    ClsUtils.setPin(device.getClass(), device, strPsw); // 手机和蓝牙采集器配对
                  //  ClsUtils.createBond(device.getClass(), device);
                    //ClsUtils.setPasskey(device.getClass(), device, strPsw);
                   // ClsUtils.cancelPairingUserInput(device.getClass(), device); //一般调用不成功，前言里面讲解过了
                    ToastUtils.showShort("配对信息===>>>>好像成功了~");
//                    if(!HidUitls.connect(device.getAddress())){;//如果配对，直接连接
//                        HidUitls.reConnect((Activity) context);
//                    }
                    abortBroadcast();//如果没有将广播终止，则会出现一个一闪而过的配对框。
                } catch (Exception e) {
                    LogUtils.e("反射异常："+e);
                    // TODO Auto-generated catch block
                    ToastUtils.showShort("请求连接错误");
                }
            }
        }
    }

}
