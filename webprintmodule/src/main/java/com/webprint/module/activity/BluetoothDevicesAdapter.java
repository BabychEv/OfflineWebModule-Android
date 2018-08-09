package com.webprint.module.activity;

import android.bluetooth.BluetoothDevice;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.webprint.module.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BluetoothDevicesAdapter extends RecyclerView.Adapter<BluetoothDevicesAdapter.ViewHolder> {

    private List<BluetoothDevice> mDevices;
    private OnBluetoothDeviceClickListener mClickListener;

    public BluetoothDevicesAdapter(OnBluetoothDeviceClickListener listener) {
        mDevices = new ArrayList<>();
        mClickListener = listener;
    }

    public BluetoothDevicesAdapter(List<BluetoothDevice> devices, OnBluetoothDeviceClickListener listener) {
        mDevices = devices;
        this.notifyDataSetChanged();
        mClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_device_item, parent, false);
        ViewHolder vh = new ViewHolder(view);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BluetoothDevice item = getItem(position);
        if (item != null) {
            final String name = !TextUtils.isEmpty(item.getName()) ? item.getName() : holder.itemView.getResources().getString(R.string.no_name);
            final String address = !TextUtils.isEmpty(item.getAddress()) ? item.getAddress() : "";
            String state = "";
            switch (item.getBondState()) {
                case BluetoothDevice.BOND_NONE:
                    state = holder.itemView.getResources().getString(R.string.bond_none);
                    break;
                case BluetoothDevice.BOND_BONDING:
                    state = holder.itemView.getResources().getString(R.string.bond_bonding);
                    break;
                case BluetoothDevice.BOND_BONDED:
                    state = holder.itemView.getResources().getString(R.string.bond_bonded);
                    break;
            }
            holder.mTextViewName.setText(name);
            holder.mTextViewAddress.setText(address);
            holder.mButtonAction.setText(state);
            holder.mButtonAction.setOnClickListener(click -> {
                mClickListener.onBluetoothClicked(item);
            });
        }
    }

    @Override
    public int getItemCount() {
        return mDevices != null ? mDevices.size() : 0;
    }

    public BluetoothDevice getItem(int position) {
        return mDevices != null && mDevices.size() > position && position > -1 ? mDevices.get(position) : null;
    }

    public void addItems(BluetoothDevice... items) {
        mDevices.addAll(Arrays.asList(items));
        this.notifyDataSetChanged();
    }

    public void addItems(List<BluetoothDevice> items) {
        mDevices.addAll(items);
        this.notifyDataSetChanged();
    }

    public void setItems(List<BluetoothDevice> items) {
        mDevices.clear();
        mDevices.addAll(items);
        this.notifyDataSetChanged();
    }

    public void clearData() {
        mDevices.clear();
        this.notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView mTextViewName;
        public TextView mTextViewAddress;
        public Button mButtonAction;

        public ViewHolder(View v) {
            super(v);
            mTextViewName = v.findViewById(R.id.text_view_name);
            mTextViewAddress = v.findViewById(R.id.text_view_address);
            mButtonAction = v.findViewById(R.id.btn_action);
        }
    }
}
