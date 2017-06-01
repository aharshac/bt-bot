package harsha.btbot;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;
import app.akexorcist.bluetotohspp.library.DeviceList;

public class MainActivity extends AppCompatActivity {
    BluetoothSPP mBluetooth;

    private LinearLayout llConnection;
    private TextView tvDeviceName, tvDeviceAddress;
    private TextView tvBtState;
    private Button btScan, btDisconnect;
    private TableLayout tlBotCtrl;

    private String mLastMsg = "B";

    private Handler mPressHandler;
    private Runnable mPressRunnable =  new Runnable(){
        public void run(){
            sendData(mLastMsg);
            mPressHandler.postDelayed(this, 100);
        }
    };


    View.OnTouchListener mOnCtrlTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            view.setPressed(true);

            String msg = "B";
            int action = motionEvent.getAction();
            if (action == MotionEvent.ACTION_DOWN) {
                switch (view.getId()) {
                    case R.id.bt_ctrl_a:
                        msg = "A";
                        break;
                    case R.id.bt_ctrl_s:
                        msg = "S";
                        break;
                    case R.id.bt_ctrl_w:
                        msg = "W";
                        break;
                    case R.id.bt_ctrl_d:
                        msg = "D";
                }
                mLastMsg = msg;
                if (mPressHandler != null) return true;
                mPressHandler = new Handler();
                mPressHandler.post(mPressRunnable);
                return true;
            }

            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_OUTSIDE
                    || action == MotionEvent.ACTION_CANCEL) {
                view.setPressed(false);

                mLastMsg = "B";
                if (mPressHandler == null) return true;
                mPressHandler.removeCallbacks(mPressRunnable);
                mPressHandler = null;
                return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        llConnection  = (LinearLayout) findViewById(R.id.ll_connected_device);
        tvDeviceName = (TextView) findViewById(R.id.tv_device_name);
        tvDeviceAddress = (TextView) findViewById(R.id.tv_device_address);
        tvBtState = (TextView) findViewById(R.id.tv_state);
        btScan = (Button) findViewById(R.id.bt_scan);
        btDisconnect = (Button) findViewById(R.id.bt_disconnect);
        tlBotCtrl = (TableLayout) findViewById(R.id.tl_bot_ctrl);
        Button btCtrlW = (Button) findViewById(R.id.bt_ctrl_w);
        Button btCtrlA = (Button) findViewById(R.id.bt_ctrl_a);
        Button btCtrlS = (Button) findViewById(R.id.bt_ctrl_s);
        Button btCtrlD = (Button) findViewById(R.id.bt_ctrl_d);

        setActivityUiState(false);

        mBluetooth = new BluetoothSPP(this);

        if(!mBluetooth.isBluetoothAvailable()) {
            showToast("Bluetooth is not available");
            finish();
        }

        tvBtState.setText(btStateToHuman(mBluetooth.getServiceState()));

        mBluetooth.setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() {
            public void onDeviceConnected(String name, String address) {
                setActivityUiState(true);
                setConnectionText(name, address);
            }

            public void onDeviceDisconnected() {
                setActivityUiState(false);
                setConnectionText("", "");
                showToast("Disconnected.");
            }

            public void onDeviceConnectionFailed() {
                setActivityUiState(false);
                setConnectionText("", "");
                showToast("Connection failed");
            }
        });

        mBluetooth.setBluetoothStateListener(new BluetoothSPP.BluetoothStateListener() {
            public void onServiceStateChanged(int state) {
                tvBtState.setText(btStateToHuman(state));
                btScan.setEnabled(state != BluetoothState.STATE_CONNECTING);
            }
        });

        mBluetooth.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() {
            public void onDataReceived(byte[] data, String message) {
                Log.i("Check", "Length : " + data.length);
                Log.i("Check", "Message : " + message);
            }
        });

        btScan.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                if(mBluetooth != null && mBluetooth.getServiceState() == BluetoothState.STATE_CONNECTED) {
                    mBluetooth.disconnect();
                } else {
                    Intent intent = new Intent(MainActivity.this, DeviceList.class);
                    intent.putExtra("bluetooth_devices", "Bluetooth devices");
                    intent.putExtra("no_devices_found", "No devices available");
                    intent.putExtra("scanning", "Scanning");
                    intent.putExtra("scan_for_devices", "Search");
                    intent.putExtra("select_device", "Select");
                    startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
                }
            }
        });

        btDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mBluetooth != null) {
                    mBluetooth.disconnect();
                }
                setActivityUiState(false);
                setConnectionText("", "");
            }
        });

        btCtrlA.setOnTouchListener(mOnCtrlTouchListener);
        btCtrlS.setOnTouchListener(mOnCtrlTouchListener);
        btCtrlW.setOnTouchListener(mOnCtrlTouchListener);
        btCtrlD.setOnTouchListener(mOnCtrlTouchListener);
    }

    public void onDestroy() {
        super.onDestroy();
        if (mBluetooth != null) {
            if (mBluetooth.isBluetoothEnabled()) {
                mBluetooth.getBluetoothAdapter().disable();
            }
            mBluetooth.stopService();
        }
    }

    public void onStart() {
        super.onStart();

        if (mBluetooth != null) {
            if (!mBluetooth.isBluetoothEnabled()) {
                mBluetooth.enable();
            } else if(!mBluetooth.isServiceAvailable()) {
                mBluetooth.setupService();
                mBluetooth.startService(BluetoothState.DEVICE_OTHER);
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
            if(resultCode == Activity.RESULT_OK && mBluetooth != null
                    && data != null && data.hasExtra(BluetoothState.EXTRA_DEVICE_ADDRESS)) {

                String address = data.getExtras().getString(BluetoothState.EXTRA_DEVICE_ADDRESS);
                if (mBluetooth.getBluetoothAdapter() != null
                        && mBluetooth.getBluetoothAdapter().getRemoteDevice(address) != null) {
                    try {
                        mBluetooth.connect(data);
                        return;
                    } catch (Exception e) { }
                }
                showToast("Could not connect to device. Try again.");
            } else {
                showToast("Did not receive device data. Try again.");
            }
        } else if(requestCode == BluetoothState.REQUEST_ENABLE_BT) {
            if(resultCode == Activity.RESULT_OK) {
                mBluetooth.setupService();
                mBluetooth.startService(BluetoothState.DEVICE_OTHER);
            } else {
                showToast("Bluetooth was not enabled.");
                finish();
            }
        }

        /*
        if(requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
            if(resultCode == Activity.RESULT_OK)
                mBluetooth.connect(data);
        } else if(requestCode == BluetoothState.REQUEST_ENABLE_BT) {
            if(resultCode == Activity.RESULT_OK) {
                mBluetooth.setupService();
                mBluetooth.startService(BluetoothState.DEVICE_OTHER);
            } else {
                showToast("Bluetooth was not enabled.");
                finish();
            }
        }

        if(requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
            if(resultCode == Activity.RESULT_OK)
                mBluetooth.connect(data);
        } else if(requestCode == BluetoothState.REQUEST_ENABLE_BT) {
            if(resultCode == Activity.RESULT_OK) {
                mBluetooth.setupService();
                mBluetooth.startService(BluetoothState.DEVICE_OTHER);
            } else {
                showToast("Bluetooth was not enabled.");
                finish();
            }
        }*/
    }

    private void setActivityUiState(boolean connected) {
        llConnection.setVisibility(connected ? View.VISIBLE : View.GONE);
        tlBotCtrl.setVisibility(connected ? View.VISIBLE : View.GONE);
        btScan.setVisibility(!connected ? View.VISIBLE : View.GONE);
        btDisconnect.setVisibility(connected ? View.VISIBLE : View.GONE);
    }

    private void setConnectionText(String name, String address) {
        tvDeviceAddress.setText(address);
        tvDeviceName.setText(name);
    }

    private void sendData(String msg) {
        if (mBluetooth != null) {
            mBluetooth.send(msg, false);
        }
    }

    private String btStateToHuman(int state) {
        if(state == BluetoothState.STATE_CONNECTED) {
            return "Connected";
        } else if(state == BluetoothState.STATE_CONNECTING) {
            return "Connecting...";
        } else if(state == BluetoothState.STATE_LISTEN) {
            return "Standby";
        } else if(state == BluetoothState.STATE_NONE) {
            return "None";
        }
        return "Unknown";
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
