package com.android.superli.btremote.hid;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHidDevice;
import android.bluetooth.BluetoothHidDeviceAppSdpSettings;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.android.superli.btremote.utils.ClsUtils;
import com.android.superli.btremote.utils.ToastUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.concurrent.Executors;

public class HidUitls {
    public static String SelectedDeviceMac = "";
    public static boolean _connected = false;
    public static boolean IsRegisted = false;

    public static BluetoothAdapter mBluetoothAdapter;
    public static BluetoothProfile bluetoothProfile;
    public static BluetoothDevice BtDevice;
    public static BluetoothHidDevice HidDevice;

    /****
     * RemoteApplication中初始化注册
     * @param context
     */
    public static void RegistApp(Context context) {
        try {
//            if (IsRegisted) {
//
//            } else {
                BluetoothAdapter.getDefaultAdapter().getProfileProxy(context, mProfileServiceListener, BluetoothProfile.HID_DEVICE);
            //}
        } catch (Exception e) {
            e.printStackTrace();
            ToastUtils.showShort("当前系统不支持蓝牙遥控!");
        }
    }

    /***
     * 检测蓝牙是否配对
     * @param deviceAddress
     * @return
     */
    public static boolean Pair(String deviceAddress) {
        if (BluetoothAdapter.checkBluetoothAddress(deviceAddress)) {
            try {
                mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//                if (BtDevice == null) {
//                    BtDevice = mBluetoothAdapter.getRemoteDevice(deviceAddress);
//                }
                BtDevice = mBluetoothAdapter.getRemoteDevice(deviceAddress);
                if (BtDevice.getBondState() == BluetoothDevice.BOND_NONE) {
                    BtDevice.createBond();
                    //ClsUtils.createBond(BtDevice.getClass(),BtDevice);
                    return false;
                } else if (BtDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
                    return true;
                } else if (BtDevice.getBondState() == BluetoothDevice.BOND_BONDING) {
                    return false;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return false;
    }




    public static boolean autoPair(String strAddr, String strPsw)
    {
        boolean result = false;
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter
                .getDefaultAdapter();

        bluetoothAdapter.cancelDiscovery();

        if (!bluetoothAdapter.isEnabled())
        {
            bluetoothAdapter.enable();
        }

        if (!BluetoothAdapter.checkBluetoothAddress(strAddr))
        { // 检查蓝牙地址是否有效

            Log.d("mylog", "devAdd un effient!");
        }

        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(strAddr);

        if (device.getBondState() != BluetoothDevice.BOND_BONDED)
        {
            try
            {
                Log.d("mylog", "NOT BOND_BONDED");
                ClsUtils.setPin(device.getClass(), device, strPsw); // 手机和蓝牙采集器配对
                ClsUtils.createBond(device.getClass(), device);
                BtDevice = device; // 配对完毕就把这个设备对象传给全局的remoteDevice
                result = true;
            }
            catch (Exception e)
            {
                // TODO Auto-generated catch block

                Log.d("mylog", "setPiN failed!");
                e.printStackTrace();
            } //

        }
        else
        {
            Log.d("mylog", "HAS BOND_BONDED");
            try
            {
                ClsUtils.createBond(device.getClass(), device);
                ClsUtils.setPin(device.getClass(), device, strPsw); // 手机和蓝牙采集器配对
                ClsUtils.createBond(device.getClass(), device);
                BtDevice = device; // 如果绑定成功，就直接把这个设备对象传给全局的remoteDevice
                result = true;
            }
            catch (Exception e) {
                // TODO Auto-generated catch block
                Log.d("mylog", "setPiN failed!");
                e.printStackTrace();
            }
        }
        return result;
    }

    /***
     * 获取蓝牙连接状态
     * @param
     */
    public static boolean IsConnected() {
        try {
            return HidUitls._connected;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    /***
     * 设置蓝牙状态为已连接
     * @param _connected
     */
    private static void IsConnected(boolean _connected) {
        HidUitls._connected = _connected;
    }
    /***
     * 根据设备mac地址，连接蓝牙
     * @param deviceAddress
     * @return
     */
    public static boolean connect(String deviceAddress) {
        if (TextUtils.isEmpty(deviceAddress)) {
            ToastUtils.showShort("获取mac地址失败");
            return false;
        }
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            ToastUtils.showShort("当前设备不支持蓝牙HID");
            return false;
        }
        if (BtDevice == null) {
            BtDevice = mBluetoothAdapter.getRemoteDevice(deviceAddress);
        }
        boolean ret = HidDevice.connect(BtDevice);
        HidConsts.BtDevice = BtDevice;
        HidConsts.HidDevice = HidDevice;
        return ret;
    }

    /***
     * 根据设备实例连接蓝牙
     * @param device
     * @return
     */
    public static boolean connect(BluetoothDevice device) {
        boolean ret = HidDevice.connect(device);
        HidConsts.BtDevice = device;
        HidConsts.HidDevice = HidDevice;
        return ret;
    }

    /****
     * ActLifecycleCallbacks
     * 重新连接蓝牙
     * @param context
     */
    public static void reConnect( Activity context) {
        if (TextUtils.isEmpty(SelectedDeviceMac)) {
            return;
        }
        try {
            if (HidUitls.HidDevice != null) {
                if (HidUitls.BtDevice == null) {
                    HidUitls.BtDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(HidUitls.SelectedDeviceMac);
                }
                int state = HidUitls.HidDevice.getConnectionState(HidUitls.BtDevice);
                if (state == BluetoothProfile.STATE_DISCONNECTED) {
                    if (TextUtils.isEmpty(HidUitls.SelectedDeviceMac)) {
                    } else {
                        if (HidUitls.Pair(HidUitls.SelectedDeviceMac)) {
                            HidUitls.RegistApp(context.getApplicationContext());
                            UtilCls.DelayTask(new Runnable() {
                                @Override
                                public void run() {
                                    context.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            HidUitls.connect(HidUitls.SelectedDeviceMac);
                                        }
                                    });
                                }
                            }, 1000, true);
                        }
                    }
                }
            }
        } catch (Exception ex) {
        }
    }

    /***
     * 蓝牙配置服务监听
     */
    public static BluetoothProfile.ServiceListener mProfileServiceListener = new BluetoothProfile.ServiceListener() {
        @Override
        public void onServiceDisconnected(int profile) {
        }

        @SuppressLint("NewApi")
        @Override
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            bluetoothProfile = proxy;
            if (profile == BluetoothProfile.HID_DEVICE) {
                HidDevice = (BluetoothHidDevice) proxy;//将BluetoothProfile  转换为  BluetoothHidDevice
                HidConsts.HidDevice = HidDevice;   //保存为静态
                BluetoothHidDeviceAppSdpSettings sdp = new BluetoothHidDeviceAppSdpSettings(HidConsts.NAME, HidConsts.DESCRIPTION, HidConsts.PROVIDER, BluetoothHidDevice.SUBCLASS1_COMBO, HidConsts.Descriptor);
                HidDevice.registerApp(sdp, null, null, Executors.newCachedThreadPool(), mCallbacks);//注册监听蓝牙设备连接状态
            }
        }
    };

    public static final BluetoothHidDevice.Callback mCallbacks = new BluetoothHidDevice.Callback() {
        @Override
        public void onAppStatusChanged(BluetoothDevice pluggedDevice, boolean registered) {
            IsRegisted = registered;
        }

        @Override
        public void onConnectionStateChanged(BluetoothDevice device, int state) {
            if (state == BluetoothProfile.STATE_DISCONNECTED) {
                IsConnected(false);
                EventBus.getDefault().post(new HidEvent(HidEvent.tcpType.onDisConnected));//未连接
            } else if (state == BluetoothProfile.STATE_CONNECTED) {
                IsConnected(true);
                EventBus.getDefault().post(new HidEvent(HidEvent.tcpType.onConnected));//已连接
            } else if (state == BluetoothProfile.STATE_CONNECTING) {
                EventBus.getDefault().post(new HidEvent(HidEvent.tcpType.onConnecting));//连接中...
            }
        }
    };
}
