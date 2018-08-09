package com.webprint.module.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.tbruyelle.rxpermissions2.RxPermissions;
import com.webprint.module.R;
import com.webprint.module.broadcast.PrintBluetoothModuleReceiver;
import com.webprint.module.broadcast.RootBroadcastReceiver;
import com.webprint.module.utils.PrintSharedPreferences;
import com.webprint.module.utils.Utils;

import java.lang.reflect.Method;
import java.util.Set;

public class BluetoothActivity extends FragmentActivity {

    //UI
    private Button mScanButton;
    private RecyclerView mDevicesRecyclerView;
    private BluetoothDevicesAdapter mBluetoothDevicesAdapter;
    private Dialog mDialog;
    private TextView mTextViewStatus;

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice mDevice;
    private RootBroadcastReceiver mCloseReceiver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_print);
        setActionBar(null);
        mCloseReceiver = new RootBroadcastReceiver(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(RootBroadcastReceiver.CLOSE_ACTIVITY_ACTION);
        registerReceiver(mCloseReceiver, filter);
        initView();
        loadPairedDevices();
    }

    @SuppressLint("CheckResult")
    private void initView() {
        mDialog = Utils.getProgressDialog(this);
        mScanButton = findViewById(R.id.btn_scan);
        mDevicesRecyclerView = findViewById(R.id.recycler_view);
        mTextViewStatus = findViewById(R.id.text_view_status);
        mScanButton.setOnClickListener(click -> new RxPermissions(BluetoothActivity.this)
                .request(Manifest.permission.BLUETOOTH,
                        Manifest.permission.BLUETOOTH_ADMIN,
                        Manifest.permission.ACCESS_COARSE_LOCATION)
                .subscribe(granted -> {
                    if (granted) {
                        if (!mBluetoothAdapter.isEnabled()) {
                            mBluetoothAdapter.enable();
                        }
                        scanDevices();
                    } else {
                    }
                }, error -> {

                }));
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            Toast.makeText(BluetoothActivity.this,
                    R.string.bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        mBluetoothDevicesAdapter = new BluetoothDevicesAdapter(mBluetoothDeviceClickListener);
        mDevicesRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        mDevicesRecyclerView.setAdapter(mBluetoothDevicesAdapter);
    }

    private OnBluetoothDeviceClickListener mBluetoothDeviceClickListener = new OnBluetoothDeviceClickListener() {
        @Override
        public void onBluetoothClicked(BluetoothDevice device) {
            mDevice = device;
            showProgressBar();
            if (mDevice.getBondState() == BluetoothDevice.BOND_NONE) {
                pairOrRePairDevice(false, mDevice);
            } else if (mDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
                pairOrRePairDevice(true, mDevice);
            }
        }
    };

    @SuppressLint("CheckResult")
    private void loadPairedDevices() {
        new RxPermissions(BluetoothActivity.this)
                .request(Manifest.permission.BLUETOOTH,
                        Manifest.permission.BLUETOOTH_ADMIN,
                        Manifest.permission.ACCESS_COARSE_LOCATION)
                .subscribe(granted -> {
                    if (granted) {
                        if (!mBluetoothAdapter.isEnabled()) {
                            mBluetoothAdapter.enable();
                        }
                        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
                        if (pairedDevices.size() > 0) {
                            for (BluetoothDevice device : pairedDevices) {
                                mBluetoothDevicesAdapter.addItems(device);
                            }
                        }
                    } else {
                    }
                }, error -> {

                });
    }

    private void scanDevices() {
        showProgressBar();
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
        mBluetoothDevicesAdapter.clearData();
        mTextViewStatus.setText("");
        mBluetoothAdapter.startDiscovery();
    }

    @Override
    protected void onResume() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mBluetoothReceiver, filter);
        super.onResume();
    }

    @Override
    protected void onStop() {
        if (mBluetoothAdapter != null && mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
            hideProgressBar();
        }
        this.unregisterReceiver(mBluetoothReceiver);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
        unregisterReceiver(mCloseReceiver);
        super.onDestroy();
    }

    public void showProgressBar() {
        if (mDialog != null) {
            mDialog.show();
        }
    }

    public void hideProgressBar() {
        if (mDialog != null) {
            mDialog.hide();
        }
    }

    private boolean pairOrRePairDevice(boolean rePair, BluetoothDevice device) {
        boolean success = false;
        try {
            mDevice = device;
            IntentFilter boundFilter = new IntentFilter(
                    BluetoothDevice.ACTION_BOND_STATE_CHANGED);
            this.registerReceiver(mBoundDeviceReceiver, boundFilter);

            if (rePair) {
                // cancel bond
                Method removeBondMethod = BluetoothDevice.class
                        .getMethod("removeBond");
                success = (Boolean) removeBondMethod.invoke(device);
            } else {
                // Input password
                // Method setPinMethod =
                // BluetoothDevice.class.getMethod("setPin");
                // setPinMethod.invoke(device, 1234);
                // create bond
                Method createBondMethod = BluetoothDevice.class
                        .getMethod("createBond");
                hideProgressBar();
                success = (Boolean) createBondMethod.invoke(device);
            }
        } catch (Exception e) {
            e.printStackTrace();
            success = false;
            hideProgressBar();
        }
        return success;
    }

    private final BroadcastReceiver mBluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                mBluetoothDevicesAdapter.addItems(device);
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                hideProgressBar();
                if (mBluetoothDevicesAdapter.getItemCount() == 0) {
                    Toast.makeText(BluetoothActivity.this,
                            R.string.no_devices_found, Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    private BroadcastReceiver mBoundDeviceReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (mDevice == null) return;
            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                BluetoothDevice device = intent
                        .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (!mDevice.equals(device)) {
                    return;
                }
                switch (device.getBondState()) {
                    case BluetoothDevice.BOND_BONDING:
                        PrintSharedPreferences.saveAddress(context, device.getAddress());
                        String stateBonding = String.format(context.getString(R.string.device_bonding), mDevice.getName(), mDevice.getAddress());
                        mTextViewStatus.setText(stateBonding);
                        hideProgressBar();
                        sendBroadcast(new Intent(PrintBluetoothModuleReceiver.ACTION_DEVICES_PAIRED));
                        break;
                    case BluetoothDevice.BOND_BONDED:
                        String stateBonded = String.format(context.getString(R.string.device_bonded), mDevice.getName(), mDevice.getAddress());
                        mTextViewStatus.setText(stateBonded);
                        BluetoothActivity.this.unregisterReceiver(mBoundDeviceReceiver);
                        hideProgressBar();
                        break;
                    case BluetoothDevice.BOND_NONE:
                        Toast.makeText(BluetoothActivity.this,
                                R.string.failed_bonded, Toast.LENGTH_SHORT).show();
                        pairOrRePairDevice(false, device);
                    default:
                        break;
                }
            }
        }
    };
}
