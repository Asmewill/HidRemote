package com.android.superli.btremote;

import android.app.Activity;
import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.CheckedTextView;
import android.widget.ImageView;

import com.android.base.SharedPreferencesUtil;
import com.android.base.router.Router;
import com.android.superli.btremote.bean.MBluetoothDevice;
import com.android.superli.btremote.config.ActivityTack;
import com.android.superli.btremote.hid.HidEvent;
import com.android.superli.btremote.hid.HidUitls;
import com.android.superli.btremote.ui.activity.MainActivity;
import com.android.superli.btremote.ui.adapter.BleDeviceAdpter;
import com.android.superli.btremote.ui.views.dialog.AlertDialog;
import com.android.superli.btremote.utils.ToastUtils;
import com.gyf.immersionbar.ImmersionBar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ScanActivity extends Activity {
    public static final int REQUEST_ENABLE_BT = 1;//开启蓝牙的请求回调
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice mBluetoothDevice;

    private RecyclerView mRecyclerview;
    private BleDeviceAdpter mBleDeviceAdpter;
    private List<MBluetoothDevice> datas;
    private LoadingDialog mLoadingDialog;
    CheckedTextView ct_open;
    ImageView iv_refresh;
    private BluetoothGatt mBluetoothGatt;

    //循环开启扫描功能
    private Handler handler = new Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            startDiscovery();
        }
    };

    //显示无法连接
    private Handler handler2 = new Handler();
    private Runnable runnable2 = new Runnable() {
        @Override
        public void run() {
            if (mLoadingDialog != null) {
                mLoadingDialog.dismiss();
            }
            showDisConnectDialog();
        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        bindUI();
    }

    public void bindUI() {
       // super.bindUI(rootView);
        int theme = (int) SharedPreferencesUtil.getData("theme", 0);

        ImmersionBar.with(this).titleBar(R.id.llt_title)
                .statusBarDarkFont(theme == 0 ? true : false, 0.2f)
                .keyboardEnable(true)
                .init();
        iv_refresh=findViewById(R.id.iv_refresh);
        mLoadingDialog = new LoadingDialog(this);
        datas = new ArrayList<>();
        mRecyclerview = findViewById(R.id.recyclerview);
        ct_open=findViewById(R.id.ct_open);
        ct_open.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!ct_open.isChecked()){
                    //方式一
//                    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE); // 设置蓝牙可见性，最多300秒
//                    intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 20);
//                    startActivityForResult(intent, REQUEST_ENABLE_BT);
                    //方式二
                    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                    bluetoothAdapter.enable();
                }else{
                    mBluetoothAdapter.disable();
                }
                ct_open.setChecked(!ct_open.isChecked());
            }
        });
        mBleDeviceAdpter = new BleDeviceAdpter(this);
        mBleDeviceAdpter.setmOnClickListener(new BleDeviceAdpter.OnClickListener() {
            @Override
            public void onClick(MBluetoothDevice item) {
                if (mLoadingDialog != null) {
                    mLoadingDialog.show();
                }
                mBluetoothDevice = item.mBluetoothDevice;
                 connect();
                //建立蓝牙连接


//                HidUitls.SelectedDeviceMac = mBluetoothDevice.getAddress();
//                HidUitls.reConnect(ScanActivity.this);
//                if(HidUitls.autoPair(mBluetoothDevice.getAddress(),"111111")){
//                       ToastUtils.showLong("自动配对OK");
//                }

//                int connectState = mBluetoothDevice.getBondState();
//                switch (connectState){
//                    //未配对
//                    case BluetoothDevice.BOND_NONE:
//                        //开始配对
//
//                        break;
//                }
//                try{
//                    ClsUtils.createBond(mBluetoothDevice.getClass(),mBluetoothDevice);
//                }catch (Exception e){
//                    e.printStackTrace();
//                }
//                mBluetoothGatt = mBluetoothDevice.connectGatt(ScanActivity.this, false, mGattCallback);
            }
        });
        mRecyclerview.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerview.setLayoutManager(layoutManager);
        mBleDeviceAdpter.setData(datas);
        mRecyclerview.setAdapter(mBleDeviceAdpter);
        registerBlue();
    }
    /**
     * gatt连接结果的返回
     */
    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (status == BluetoothProfile.STATE_DISCONNECTED) { //蓝牙连接
                System.out.println("onConnectionStateChange" + "连接成功");
                ToastUtils.showLong("连接成功6666");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {

        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
        }

        /**
         * Callback triggered as a result of a remote characteristic notification.
         *
         * @param gatt
         * @param characteristic
         */
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            System.out.println("onCharacteristicChanged");

        }

        /**
         * 写入数据时操作
         * @param gatt
         * @param characteristic
         * @param status
         */
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            System.out.println("onCharacteristicWrite");
        }

        /**
         * 读取返回值时操作
         * @param gatt
         * @param characteristic
         * @param status
         */
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            System.out.println("onCharacteristicRead");
        }

        /**
         * Callback indicating the result of a descriptor write operation.
         *
         * @param gatt
         * @param descriptor
         * @param status
         */
        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            System.out.println("onDescriptorWrite");
        }

    };



    /***
     * 注册蓝牙
     */
    public void registerBlue(){
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        //蓝牙注册
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);//发现设备
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);//绑定状态改变
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);//搜索状态改变
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);//开始搜索
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);//搜索完成
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);//连接完成
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);//断开连接
      //  intentFilter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);//蓝牙配对
        intentFilter.addAction("android.bluetooth.input.profile.action.CONNECTION_STATE_CHANGED");
        registerReceiver(mReceiver, intentFilter);
        //EventBus注册
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        datas.clear();
        mBleDeviceAdpter.notifyDataSetChanged();
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        List<BluetoothDevice> bluetoothDevices = Arrays.asList(pairedDevices.toArray(new BluetoothDevice[0]));
        for (BluetoothDevice device : bluetoothDevices) {
            MBluetoothDevice bean = new MBluetoothDevice();
            bean.mBluetoothDevice = device;
            bean.type = 1;
            addDevice(bean);
        }
        //开始搜索蓝牙
        startDiscovery();
        ct_open.setChecked(isOpenBluetooth());
        iv_refresh.setOnClickListener(v -> {
            if(isOpenBluetooth()) {
                iv_refresh.setAnimation(getImgAnimation());
                iv_refresh.startAnimation(getImgAnimation());
                startDiscovery();
            }else{
                ToastUtils.showLong("请先开启蓝牙");
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

//    @Override
//    public void initData() {
//        int initDialog = (int) SharedPreferencesUtil.getData("initDialog", 0);
//        if (initDialog == 0) {
//            new AlertDialog(this).init()
//                    .setTitle("声明")
//                    .setCancelable(true)
//                    .setMsg("由于本软件的蓝牙遥控功能是基于HID协议实现，但是部分手机厂家移除了hid模块，" +
//                            "所以导致了不兼容情况的出现。如果你的" +
//                            "手机出现了闪退等问题，大概率是该机型不被支持。\n\n" +
//                            "更多好玩软件：www.wnkong.com")
//                    .setPositiveButton("了解，不再提示！", new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            SharedPreferencesUtil.putData("initDialog", 1);
//                        }
//                    }).show();
//        }
//    }

//    @Override
//    protected void onStart() {
//        super.onStart();
//
//    }
//
//
//
//    @Override
//    protected void onStop() {
//        super.onStop();
//
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //取消注册
        unregisterReceiver(mReceiver);
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        //取消蓝牙搜索
        cancelDiscovery();

        if (mLoadingDialog != null) {
            mLoadingDialog.dismiss();
        }

        if (alertDialog2 != null) {
            alertDialog2.dismiss();
        }
        //移除开始蓝牙搜索的runnable任务
        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
        }
        //移除---显示蓝牙连接失败的Dialog提示
        if (handler2 != null && runnable2 != null) {
            handler2.removeCallbacks(runnable2);
        }
    }


    /***
     * 连接蓝牙
     */
    private void connect() {
        if (mBluetoothDevice == null) {
            return;
        }
        //移除---显示蓝牙连接失败的Dialog提示
        handler2.removeCallbacks(runnable2);
        //如果20s内没有连上蓝牙，显示蓝牙连接失败的Dialog提示
        handler2.postDelayed(runnable2, 20000);
        //获取设备mac地址
        String deviceAddress = mBluetoothDevice.getAddress();
        if (TextUtils.isEmpty(deviceAddress)) {
            boolean pair = HidUitls.Pair(deviceAddress);
            if (pair) {
                HidUitls.connect(mBluetoothDevice);//有蓝牙设备，直接连接
            }
            return;
        }
        //检测蓝牙是否配对
        HidUitls.SelectedDeviceMac = deviceAddress;//保存静态的mac地址，跟随生命周期
        boolean pair = HidUitls.Pair(deviceAddress);
        if (pair) {
            if(!HidUitls.connect(deviceAddress)){;//如果配对，直接连接
                HidUitls.reConnect(ScanActivity.this);
            }
        }
    }


    /***
     * 开始搜索蓝牙设备
     */
    private void startDiscovery() {
        handler.removeCallbacks(runnable);
        if (mBluetoothAdapter == null) {
            return;
        }
        if (mBluetoothAdapter.isDiscovering()) {
            return;
        }
        mBluetoothAdapter.startDiscovery();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleEvent(HidEvent message) {
        if (message.mtcpType == HidEvent.tcpType.onConnected) {
            if (mLoadingDialog != null) {
                mLoadingDialog.dismiss();
            }
            handler2.removeCallbacks(runnable2);
            ToastUtils.showShort("连接成功");

            Router.newIntent(this).to(MainActivity.class).launch();
            finish();
        } else if (message.mtcpType == HidEvent.tcpType.onDisConnected) {
            if (mLoadingDialog != null) {
                mLoadingDialog.dismiss();
            }
            ToastUtils.showShort("连接失败");
            handler2.removeCallbacks(runnable2);
            showDisConnectDialog();
        }
    }

    /***
     * 取消蓝牙搜索
     */
    private void cancelDiscovery() {
        handler.removeCallbacks(runnable);
        if (mBluetoothAdapter == null) {
            return;
        }
        if (!mBluetoothAdapter.isDiscovering()) {
            return;
        }
        mBluetoothAdapter.cancelDiscovery();
        mBleDeviceAdpter = null;
    }

    /***
     * 蓝牙搜索
     * 定义广播接收
     */
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                MBluetoothDevice bean = new MBluetoothDevice();
                bean.mBluetoothDevice = device;
                if (device.getBondState() == BluetoothDevice.BOND_BONDED) {    //显示已配对设备
                    bean.type = 1;
                } else {
                    bean.type = 0;
                }
                addDevice(bean);
            }else if(action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
                //间隔一段时间,重新扫描
                //handler.postDelayed(runnable, 5000);
                ToastUtils.showLong("扫描结束");
                iv_refresh.setAnimation(null);
            }else if(action.equals(BluetoothDevice.ACTION_ACL_CONNECTED)){
                if (mLoadingDialog != null) {
                    mLoadingDialog.dismiss();
                }
                ToastUtils.showLong("Socket已连接上了");
                handler2.removeCallbacks(runnable2);

            } else if(action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)){
                ToastUtils.showLong("Socket断开连接了");
            } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {

                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                switch (device.getBondState()) {
                    case BluetoothDevice.BOND_BONDING://正在配对
                        ToastUtils.showLong("正在配对");
                        Log.e("BlueToothTestActivity", "正在配对......");
                        break;
                    case BluetoothDevice.BOND_BONDED://配对结束
                        ToastUtils.showLong("完成配对");
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                connect();
                            }
                        },1000);

                        Log.e("BlueToothTestActivity", "完成配对");
//                        AlertDialog dialog1 = new AlertDialog(ScanActivity.this);
//                        dialog1.init();
//                        dialog1.setMsg("恭喜您，已完成配置，可以开始连接设备了");
//                        dialog1.setPositiveButton("取消", new View.OnClickListener() {
//                            @Override
//                            public void onClick(View v) {
//                                dialog1.dismiss();
//                            }
//                        });
//                        dialog1.setNegativeButton("开始连接设备", new View.OnClickListener() {
//                            @Override
//                            public void onClick(View v) {
//                                connect();
//                            }
//                        });
//                        dialog1.show();
                        break;
                    case BluetoothDevice.BOND_NONE://取消配对/未配对
                        ToastUtils.showLong("取消配对");
                        Log.e("BlueToothTestActivity", "取消配对");
                    default:
                        break;
                }
            }

//            else if(BluetoothDevice.ACTION_PAIRING_REQUEST.equals(action)){
//                try {
//
//
//                    //顺序一定要这样，否则会出问题
//                     ClsUtils.setPin(mBluetoothDevice.getClass(), mBluetoothDevice, "1234");
//                    //取消用户输入
//                     ClsUtils.cancelPairingUserInput(mBluetoothDevice.getClass(), mBluetoothDevice);
//                    //确认配对
//                   //  ClsUtils.setPairingConfirmation(mBluetoothDevice.getClass(), mBluetoothDevice, true);
//                    //如果不结束广播接收，配对界面会闪出
//                    abortBroadcast();
//
//                }catch (Exception e){
//                    e.printStackTrace();
//                }
//            }
        }
    };

    /***
     * 添加设备
     * @param bean
     */
    private void addDevice(MBluetoothDevice bean) {
        boolean deviceFound = false;
        for (MBluetoothDevice tmp : datas) {
            if (tmp.mBluetoothDevice.getAddress().equals(bean.mBluetoothDevice.getAddress())) {
                deviceFound = true;
            }
        }
        if (!deviceFound) {
            datas.add(bean);
            Collections.sort(datas);
            mBleDeviceAdpter.notifyDataSetChanged();
        }
    }

    private AlertDialog alertDialog2;

    private void showDisConnectDialog() {
        if (alertDialog2 == null) {
            alertDialog2 = new AlertDialog(this);
            alertDialog2
                    .init()
                    .setMsg("蓝牙似乎已经连接上了,但是APP接收不到系统反馈,你可以重启APP,或者点击重新连接")
                    .setPositiveButton("重启APP", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ActivityTack.finishAllActivity();
                            killAppProcess();
                        }
                    })
                    .setNegativeButton("重新连接", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (mLoadingDialog != null) {
                                mLoadingDialog.show();
                            }
                            HidUitls.reConnect(ScanActivity.this);
                        }
                    });
        }
        alertDialog2.show();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        ToastUtils.showLong("exit");
        ActivityTack.finishAllActivity();
        killAppProcess();
    }

    public void killAppProcess() {
        //注意：不能先杀掉主进程，否则逻辑代码无法继续执行，需先杀掉相关进程最后杀掉主进程
        ActivityManager mActivityManager = (ActivityManager)ScanActivity.this.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo>   mList = mActivityManager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo runningAppProcessInfo : mList) {
            if (runningAppProcessInfo.pid != android.os.Process.myPid()) {
                android.os.Process.killProcess(runningAppProcessInfo.pid);
            }
        }
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }

    /***
     * 是否开启蓝牙
     * @return
     */
    public boolean isOpenBluetooth() {
        if (mBluetoothAdapter != null) {
            return mBluetoothAdapter.isEnabled();
        } else {
            return false;
        }
    }
    private RotateAnimation mRotateAnimation;
    private Animation getImgAnimation() {
        if (mRotateAnimation == null) {
            mRotateAnimation = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            LinearInterpolator lin = new LinearInterpolator();
            mRotateAnimation.setInterpolator(lin);
            mRotateAnimation.setDuration(1000);//设置动画持续时间
            mRotateAnimation.setRepeatCount(-1);//设置重复次数
            mRotateAnimation.setFillAfter(true);//动画执行完后是否停留在执行完的状态
            mRotateAnimation.setStartOffset(10);//执行前的等待时间
        }
        return mRotateAnimation;
    }

    /**
     * 连接线程
     */
    private class ConnectThread extends Thread {

        private BluetoothSocket socket;
        private boolean activeConnect;

        private ConnectThread(BluetoothSocket socket, boolean connect) {
            this.socket = socket;
            this.activeConnect = connect;
        }

        @Override
        public void run() {
            //如果是自动连接 则调用连接方法
            if (activeConnect) {
                try {
                  new Thread(new Runnable() {
                      @Override
                      public void run() {
                          try {
                              socket.connect();
                          } catch (Exception e) {
                              e.printStackTrace();
                          }
                      }
                  }).start();

                } catch (Exception e) {

                }
            }
        }
    }
}