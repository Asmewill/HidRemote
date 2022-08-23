package com.example.blue_c;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class DevicesAdapter extends BaseAdapter {

    private ArrayList<BluetoothDevice> list;
    private Context mContext;

    public DevicesAdapter(Context context, ArrayList<BluetoothDevice> list){
        mContext = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = LayoutInflater.from(mContext).inflate(R.layout.device_list, null);
        ViewHolder viewHolder = new ViewHolder(convertView);
        BluetoothDevice device = list.get(position);
        viewHolder.name.setText(device.getName());
        viewHolder.mac.setText(device.getAddress());
        return convertView;
    }

    public class ViewHolder{
        private TextView name;
        private TextView mac;
        public ViewHolder(View view){
            name = (TextView) view.findViewById(R.id.device_name);
            mac = (TextView) view.findViewById(R.id.device_mac);
        }
    }
}

