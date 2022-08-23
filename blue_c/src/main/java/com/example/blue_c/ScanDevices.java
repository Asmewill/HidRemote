package com.example.blue_c;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

public class ScanDevices {
    private BluetoothAdapter mBluetoothAdapter;

    ScanDevices() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public ArrayList<BluetoothDevice> getBondedDevices() { //获取已绑定设备列表
        Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
        ArrayList bondedDevices = new ArrayList();
        Iterator<BluetoothDevice> iterator = devices.iterator();
        while (iterator.hasNext()) {
            bondedDevices.add(iterator.next());
        }
        return bondedDevices;
    }

    public void startDiscovery() { //搜索周围蓝牙设备，并通过广播返回
        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable(); // 打开蓝牙功能
            return;
        }
        mBluetoothAdapter.startDiscovery();
    }
}
