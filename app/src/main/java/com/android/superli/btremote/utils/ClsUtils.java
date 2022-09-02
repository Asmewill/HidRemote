package com.android.superli.btremote.utils;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Data :2022/9/1
 * Time:17:24
 * Author:shuij
 */
public class ClsUtils {
    static BluetoothDevice remoteDevice;
    /**
     * 与设备配对 参考源码：platform/packages/apps/Settings.git
     * /Settings/src/com/android/settings/bluetooth/CachedBluetoothDevice.java
     */
    static public boolean createBond(Class btClass, BluetoothDevice btDevice)
            throws Exception {
        Method createBondMethod = btClass.getMethod("createBond");
        Boolean returnValue = (Boolean) createBondMethod.invoke(btDevice);
        return returnValue.booleanValue();
    }
    /**
     * 与设备解除配对 参考源码：platform/packages/apps/Settings.git
     * /Settings/src/com/android/settings/bluetooth/CachedBluetoothDevice.java
     */
    static public boolean removeBond(Class btClass, BluetoothDevice btDevice)
            throws Exception {
        Method removeBondMethod = btClass.getMethod("removeBond");
        Boolean returnValue = (Boolean) removeBondMethod.invoke(btDevice);
        return returnValue.booleanValue();
    }
    static public boolean setPin(Class btClass, BluetoothDevice btDevice,String str) throws Exception {
        try {
            Method removeBondMethod = btClass.getDeclaredMethod("setPin",new Class[]{byte[].class});
            Boolean returnValue = (Boolean) removeBondMethod.invoke(btDevice,
                    new Object[]
                            {str.getBytes()});
            Log.e("returnValue", "" + returnValue);
        } catch (SecurityException e) {
            // throw new RuntimeException(e.getMessage());
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            // throw new RuntimeException(e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return true;
    }
    static public boolean setPasskey (Class btClass, BluetoothDevice btDevice,String str) throws Exception {
        try {
            Method removeBondMethod = btClass.getDeclaredMethod("setPasskey", new Class[]{byte[].class});
            Boolean returnValue = (Boolean) removeBondMethod.invoke(btDevice, new Object[]{str.getBytes()});
            Log.e("returnValue", "" + returnValue);
        } catch (SecurityException e) {
            // throw new RuntimeException(e.getMessage());
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            // throw new RuntimeException(e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return true;
    }
    // 取消用户输入
    static public boolean cancelPairingUserInput(Class btClass,BluetoothDevice device)throws Exception {
        Method createBondMethod = btClass.getMethod("cancelPairingUserInput");
        // cancelBondProcess()
        Boolean returnValue = (Boolean) createBondMethod.invoke(device);
        return returnValue.booleanValue();
    }
    // 取消配对
    static public boolean cancelBondProcess(Class btClass, BluetoothDevice device)throws Exception {
        Method createBondMethod = btClass.getMethod("cancelBondProcess");
        Boolean returnValue = (Boolean) createBondMethod.invoke(device);
        return returnValue.booleanValue();
    }
    //确认配对
    static public void setPairingConfirmation(Class<?> btClass, BluetoothDevice device, boolean isConfirm) throws Exception {
        Method setPairingConfirmation = btClass.getDeclaredMethod("setPairingConfirmation", boolean.class);
        setPairingConfirmation.invoke(device, isConfirm);
    }
    /**
     * @param clsShow
     */
    static public void printAllInform(Class clsShow) {
        try {
            // 取得所有方法
            Method[] hideMethod = clsShow.getMethods();
            int i = 0;
            for (; i < hideMethod.length; i++) {
                Log.e("method name", hideMethod[i].getName() + ";and the i is:"
                        + i);
            }
            // 取得所有常量
            Field[] allFields = clsShow.getFields();
            for (i = 0; i < allFields.length; i++) {
                Log.e("Field name", allFields[i].getName());
            }
        } catch (SecurityException e) {
            // throw new RuntimeException(e.getMessage());
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            // throw new RuntimeException(e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    // 执行时直接使用：
    public static boolean pair(String strAddr, String strPsw) {
        boolean result = false;
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter .getDefaultAdapter();
        bluetoothAdapter.cancelDiscovery();
        if (!bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.enable();
        }
        if (!BluetoothAdapter.checkBluetoothAddress(strAddr)) { // 检查蓝牙地址是否有效
            Log.d("mylog", "devAdd un effient!");
        }
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(strAddr);
        if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
            try {
                Log.d("mylog", "NOT BOND_BONDED");
                ClsUtils.setPin(device.getClass(), device, strPsw); // 手机和蓝牙采集器配对
                ClsUtils.createBond(device.getClass(), device);
                remoteDevice = device; // 配对完毕就把这个设备对象传给全局的remoteDevice
                result = true;
            } catch (Exception e) {
                // TODO Auto-generated catch block
                Log.d("mylog", "setPiN failed!");
                e.printStackTrace();
            } //
        } else {
            Log.d("mylog", "HAS BOND_BONDED");
            try {
                ClsUtils.createBond(device.getClass(), device);
                ClsUtils.setPin(device.getClass(), device, strPsw); // 手机和蓝牙采集器配对
                ClsUtils.createBond(device.getClass(), device);
                remoteDevice = device; // 如果绑定成功，就直接把这个设备对象传给全局的remoteDevice
                result = true;
            } catch (Exception e) {
                // TODO Auto-generated catch block
                Log.d("mylog", "setPiN failed!");
                e.printStackTrace();
            }
        }
        return result;
    }
}
