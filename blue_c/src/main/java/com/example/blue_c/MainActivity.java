package com.example.blue_c;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.android.base.ui.SupportActivity;

import java.util.ArrayList;
import java.util.List;

import www.hjq.permissions.OnPermissionCallback;
import www.hjq.permissions.Permission;
import www.hjq.permissions.XXPermissions;

public class MainActivity extends SupportActivity {
    private ListView listView;
    private DevicesAdapter adapter;
    private ArrayList<BluetoothDevice> list;
    private  ScanDevices mScanDevices;

    public static final String[] permissions = {
            "android.permission.BLUETOOTH",
            "android.permission.BLUETOOTH_ADMIN",
            "android.permission.ACCESS_COARSE_LOCATION",
            "android.permission.BLUETOOTH_PRIVILEGED" };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        if(Build.VERSION.SDK_INT>=23){
//          //  requestPermissions(permissions,1);
//            getPermission();
//        }
        init();
    }

    private void getPermission() {
        XXPermissions.with(MainActivity.this)
                .permission(Permission.ACCESS_FINE_LOCATION)
                .request(new OnPermissionCallback() {

                    @Override
                    public void onGranted(List<String> permissions, boolean all) {
                        list.clear();
                        list.addAll(mScanDevices.getBondedDevices()); //添加已绑定的设备列表
                        adapter.notifyDataSetChanged();
                        mScanDevices.startDiscovery(); //搜索周围蓝牙设备，并通过广播返回

                    }

                    @Override
                    public void onDenied(List<String> permissions, boolean never) {

                    }
                });
    }

    private void init() {
        listView = (ListView) findViewById(R.id.listview);
        list = new ArrayList<>();
        adapter = new  DevicesAdapter(this, list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(icl);
        mScanDevices = new ScanDevices();
        initReceiver();
    }

    private void initReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);
    }

    public void onClick(View view){ //单击扫描按钮
        getPermission();

    }

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case BluetoothDevice.ACTION_FOUND: //接收搜索到的蓝牙设备
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    String address = device.getAddress();
                    for (int i = 0; i < list.size(); i++) { //避免接收重复的设备
                        if (address == null || address.equals(list.get(i).getAddress())) {
                            return;
                        }
                    }
                    list.add(device);
                    adapter.notifyDataSetChanged();
                    break;
            }
        }
    };

    AdapterView.OnItemClickListener icl = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String address = list.get(position).getAddress();
            startChat(address);
        }
    };

    private void startChat(String remoteAddress) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("remoteAddress", remoteAddress);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(receiver);
        super.onDestroy();
    }
}
