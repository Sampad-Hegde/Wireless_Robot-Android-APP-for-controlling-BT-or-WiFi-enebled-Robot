package com.example.wirelessrobot;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity implements SensorEventListener, AdapterView.OnItemSelectedListener
{

    private View decorView;
    private Button connect_btn,ok_ip;
    private ImageView forward,backward,left,right,close;
    private Spinner modesSelector;
    private TextView connectedText;
    private SeekBar speedbar;
    public TextView speed_text,bt_status;
    private View popupView;
    private PopupWindow popupWindow;
    private LayoutInflater layoutInflater;
    private RadioGroup radioGroup;
    private EditText ipAddressEdit;
    private LinearLayout clientDetailsLayout;
    private ToggleButton opMode;
    private ListView pairedDeviceList;

    public String ipAddress = "192.168.4.1";
    private RadioButton server_mode,client_mode;
    private int operation_Mode = 1;
    public int wifi_mode = 1;
    public int mode = 0;
    private String address;

    private String[] Modes = {"Buttons Control",
            "JoyStick Control",
            "GyroScope Control",
            "Obstacle Avoidance",
            "Line Follower",
    };
    private ArrayAdapter mode_adapter;
    private WifiManager wifiManager;

    private UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private Set<BluetoothDevice> devices = null;

    public boolean is_forward_btn_pressed = false,
            is_backward_btn_pressed = false,
            is_left_btn_pressed = false,
            is_right_btn_pressed = false,
            is_connected = false,
            is_btn_pressed = false;

    private Retrofit.Builder retroBuilder;
    private Retrofit retrofit;
    private SendData dataSender;

    private BluetoothAdapter btAdapter ;
    private BluetoothDevice hc05 = null;
    private BluetoothSocket btSocket = null;

    private SensorManager sensorManager;
    private Sensor accelerometer;

    private static final int REQUEST_ENABLE_BT = 1;

    public OutputStream outputStream = null;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        decorView = getWindow().getDecorView();

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if(!wifiManager.isWifiEnabled())
        {
            buildDialog();
        }

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;


        btAdapter = BluetoothAdapter.getDefaultAdapter();

        connect_btn = (Button) findViewById(R.id.connect_btn);
        connectedText = (TextView) findViewById(R.id.connect_text);

        modesSelector = (Spinner) findViewById(R.id.mode_dropdown);

        forward = (ImageView) findViewById(R.id.up_btn);
        backward = (ImageView) findViewById(R.id.down_btn);
        left = (ImageView) findViewById(R.id.left_btn);
        right = (ImageView) findViewById(R.id.right_btn);

        speedbar = (SeekBar) findViewById(R.id.speed_seek);
        speed_text = (TextView) findViewById(R.id.speed_text);

        opMode = (ToggleButton) findViewById(R.id.op_mode);


        mode_adapter = new ArrayAdapter(MainActivity.this, android.R.layout.simple_spinner_item, Modes);
        mode_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        modesSelector.setAdapter(mode_adapter);
        modesSelector.setOnItemSelectedListener(MainActivity.this);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);


        if (!btAdapter.isEnabled())
        {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        devices = btAdapter.getBondedDevices();


        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                sensorManager.registerListener( MainActivity.this,accelerometer,SensorManager.SENSOR_DELAY_NORMAL);
            }
        }).start();

        connect_btn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

                if (operation_Mode == 1)
                {
                    if (!btAdapter.isEnabled())
                    {
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                    }
                     List<String> pairedNameList = new ArrayList<String>();
                     List<String> pairedAddrsList = new ArrayList<String>();
                    devices = btAdapter.getBondedDevices();

                    for(BluetoothDevice device: devices)
                    {
                        pairedNameList.add(device.getName());
                        pairedAddrsList.add(device.getAddress());
                    }

                    layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                    popupView = layoutInflater.inflate(R.layout.popup_bt_paired, null);

                    close = (ImageView) popupView.findViewById(R.id.close_btn);
                    pairedDeviceList = (ListView) popupView.findViewById(R.id.paired_device_list);

                    bt_status = (TextView) popupView.findViewById(R.id.bt_status);

                    popupWindow = new PopupWindow(popupView, width - 40, height - 40, true);
                    popupWindow.showAtLocation(view, Gravity.CENTER, 20, 20);


                    ArrayAdapter aAdapter = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_list_item_1, pairedNameList);
                    pairedDeviceList.setAdapter(aAdapter);


                    new Thread(new Runnable()
                    {

                        @Override
                        public void run()
                        {
                            if (aAdapter.isEmpty())
                            {
                                bt_status.setText("!! No Paired Devices Found");
                            }


                            pairedDeviceList.setOnItemClickListener(new AdapterView.OnItemClickListener()
                            {
                                @Override
                                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
                                {
                                    new Thread(new Runnable()
                                    {
                                        @Override
                                        public void run()
                                        {

                                            address = pairedAddrsList.get(i);
                                            hc05 = btAdapter.getRemoteDevice(pairedAddrsList.get(i));

                                            int counter = 0;
                                            do
                                            {
                                                try
                                                {
                                                    btSocket = hc05.createRfcommSocketToServiceRecord(uuid);
                                                    btSocket.connect();
                                                    is_connected = true;
                                                    outputStream = btSocket.getOutputStream();
                                                    SendData('b');

                                                    runOnUiThread(new Runnable()
                                                    {
                                                        @Override
                                                        public void run()
                                                        {
                                                            connectedText.setText("Connected To: "+pairedNameList.get(i));
                                                            popupWindow.dismiss();
                                                            is_connected = true;
                                                        }
                                                    });

                                                } catch (IOException e)
                                                {
                                                    e.printStackTrace();
                                                }
                                                counter++;
                                            } while (!btSocket.isConnected() && counter <= 10);
                                        }
                                    }).start();

                                    if (is_connected)
                                    {
                                        connectedText.setText("Connected To: " + pairedNameList.get(i));
                                        popupWindow.dismiss();
                                    }


                                }
                            });

                            close.setOnClickListener(new View.OnClickListener()
                            {
                                @Override
                                public void onClick(View view)
                                {
                                    popupWindow.dismiss();

                                }
                            });
                        }
                    }).start();


                }
                else
                {
                    layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                    popupView = layoutInflater.inflate(R.layout.popup_wifi, null);

                    close = (ImageView) popupView.findViewById(R.id.close_btn);
                    radioGroup = (RadioGroup) popupView.findViewById(R.id.radio_robot_type);
                    ipAddressEdit = (EditText) popupView.findViewById(R.id.ipEdit);
                    ok_ip = (Button) popupView.findViewById(R.id.ok_ip);
                    clientDetailsLayout = (LinearLayout) popupView.findViewById(R.id.client_details_layout);
                    client_mode = (RadioButton) popupView.findViewById(R.id.client_mode);
                    server_mode = (RadioButton) popupView.findViewById(R.id.server_mode);

                    popupWindow = new PopupWindow(popupView, width - 40, height - 40, true);
                    popupWindow.showAtLocation(view, Gravity.CENTER, 20, 20);


                    if (wifi_mode == 1)
                    {
                        server_mode.setChecked(true);
                    } else
                    {
                        client_mode.setChecked(true);
                    }


                    new Thread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
                            {
                                @Override
                                public void onCheckedChanged(RadioGroup radioGroup, int i)
                                {

                                    switch (i)
                                    {
                                        case R.id.server_mode:
                                            clientDetailsLayout.setVisibility(LinearLayout.INVISIBLE);
                                            ipAddress = "192.168.4.1";
                                            wifi_mode = 1;
                                            break;

                                        case R.id.client_mode:
                                            clientDetailsLayout.setVisibility(LinearLayout.VISIBLE);
                                            wifi_mode = 2;
                                            break;
                                    }
                                }
                            });

                            ok_ip.setOnClickListener(new View.OnClickListener()
                            {
                                @Override
                                public void onClick(View view)
                                {
                                    String ip = String.valueOf(ipAddressEdit.getText());

                                    if (ip.length() == 0)
                                    {
                                        Toast.makeText(MainActivity.this, "The entered IP is cannot be blank", Toast.LENGTH_LONG).show();
                                    } else
                                    {
                                        ipAddress = ip;
                                        is_connected = true;
                                        retroBuilder = new Retrofit.Builder().baseUrl("http://" + ipAddress + "/").addConverterFactory(GsonConverterFactory.create());

                                        retrofit = retroBuilder.build();
                                        dataSender = retrofit.create(SendData.class);
                                        connectedText.setText("Connected To: " + ipAddress);
                                        popupWindow.dismiss();
                                    }
                                }
                            });

                            close.setOnClickListener(new View.OnClickListener()
                            {
                                @Override
                                public void onClick(View view)
                                {
                                    if (wifi_mode == 1)
                                    {
                                        is_connected = true;
                                        ipAddress = "192.168.4.1";
                                        retroBuilder = new Retrofit.Builder().baseUrl("http://" + ipAddress + "/").addConverterFactory(GsonConverterFactory.create());

                                        retrofit = retroBuilder.build();
                                        dataSender = retrofit.create(SendData.class);
                                        connectedText.setText("Connected To: " + ipAddress);
                                        popupWindow.dismiss();
                                    }

                                }
                            });
                        }
                    }).start();

                }
            }
        });

        speedbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b)
            {
                speed_text.setText(i + "/9");
                SendData((char) (i + '0'));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar)
            {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
            {
            }
        });

        forward.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent)
            {


                switch(motionEvent.getAction())
                {
                    case 0:
                        forward.setBackgroundColor(Color.RED);
                        if(!is_btn_pressed && mode == 0)
                        {
                            is_btn_pressed = true;
                            is_forward_btn_pressed =true;
                            SendData('F');
                        }
                        break;

                    case 1:
                        forward.setBackgroundColor(0x101010);
                        if(is_btn_pressed && is_forward_btn_pressed)
                        {
                            SendData('S');
                            is_btn_pressed = false;
                            is_forward_btn_pressed = false;
                        }
                        break;
                }
                return true;
            }
        });

        backward.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent)
            {


                switch(motionEvent.getAction())
                {
                    case 0:
                        backward.setBackgroundColor(Color.RED);
                        if(!is_btn_pressed && mode == 0)
                        {
                            is_btn_pressed = true;
                            SendData('B');
                            is_backward_btn_pressed = true;
                        }
                        break;

                    case 1:
                        backward.setBackgroundColor(0x101010);
                        if(is_btn_pressed && is_backward_btn_pressed)
                        {
                            SendData('S');
                            is_btn_pressed = false;
                            is_backward_btn_pressed = false;
                        }
                        break;
                }
                return true;
            }
        });

        left.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent)
            {

                switch(motionEvent.getAction())
                {
                    case 0:
                        left.setBackgroundColor(Color.RED);
                        if(!is_btn_pressed && mode == 0)
                        {
                            is_btn_pressed = true;
                            is_left_btn_pressed = true;
                            SendData('L');
                        }
                        break;

                    case 1:
                        left.setBackgroundColor(0x101010);
                        if(is_btn_pressed && is_left_btn_pressed)
                        {
                            SendData('S');
                            is_left_btn_pressed = false;
                            is_btn_pressed = false;
                        }
                        break;
                }
                return true;
            }
        });

        right.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent)
            {

                switch(motionEvent.getAction())
                {
                    case 0:
                        right.setBackgroundColor(Color.RED);
                        if(!is_btn_pressed && mode == 0)
                        {
                            is_btn_pressed = true;
                            is_right_btn_pressed = true;
                            SendData('R');
                        }
                        break;

                    case 1:
                        right.setBackgroundColor(0x101010);
                        if(is_btn_pressed && is_right_btn_pressed)
                        {
                            SendData('S');
                            is_btn_pressed = false;
                            is_right_btn_pressed = false;

                        }
                        break;
                }
                return true;
            }
        });

        opMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b)
            {
                is_connected = false;
                connectedText.setText("Connected To: None");
                if(b)
                {
                    operation_Mode = 2;
                }
                else
                {
                    operation_Mode = 1;
                }
            }
        });




    }




    @Override
    public void onWindowFocusChanged(boolean hasFocus)
    {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus)
        {
            decorView.setSystemUiVisibility
                    (
                            View.SYSTEM_UI_FLAG_IMMERSIVE
                                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    );
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == REQUEST_ENABLE_BT)
        {
        }

        if(requestCode == 10 && operation_Mode == 1)
        {
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    hc05 = btAdapter.getRemoteDevice(address);

                    int counter = 0;
                    do
                    {
                        try
                        {
                            btSocket = hc05.createRfcommSocketToServiceRecord(uuid);
                            btSocket.connect();
                            is_connected = true;
                            outputStream = btSocket.getOutputStream();
                            SendData('b');


                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                        counter++;
                    }while(!btSocket.isConnected() && counter <= 10);
                }
            }).start();

            modesSelector.setSelection(0);
            SendData((char) (3 + '0'));
        }

    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent)
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                if(mode == 2 )
                {
                    float x = sensorEvent.values[0];
                    float y = sensorEvent.values[1];
                    float z = sensorEvent.values[2];


                    if (x < 5 && y<1 && y>-1)
                    {
                        //forward
                        SendData('F');
                    }
                    else if (x > 8.5 && y<1 && y>-1)
                    {
                        //backward
                        SendData('B');
                    }
                    else if (x > 5 && x <8.5 && y<-1)
                    {
                        //left
                        SendData('L');
                    }
                    else if(x > 5 && x <8.5 && y>1)
                    {
                        //right
                        SendData('R');
                    }
                    else
                    {
                        //stop
                        SendData('S');
                    }
                }
            }
        }).start();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i)
    {

    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l)
    {
        mode = i;

        if(mode == 0)
        {
            SendData('b');
            SendData('S');
        }
        if(mode == 1)
        {
            if (is_connected)
            {
                Intent intent = new Intent(MainActivity.this, JoystickActivity.class);
                if(operation_Mode == 1)
                {
                    intent.putExtra("Address", address);
                    intent.putExtra("Mode", operation_Mode);
                }
                else
                {
                    intent.putExtra("Address", ipAddress);
                    intent.putExtra("Mode", operation_Mode);
                }
                startActivityForResult(intent,10);
            }
            else
            {
                Toast.makeText(MainActivity.this, "You are not Connected !!", Toast.LENGTH_SHORT).show();
            }

        }

        if (mode == 3)
        {
            SendData('o');

        }
        if (mode == 4)
        {
            SendData('l');
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView)
    {

    }

    public void SendData(char cmd)
    {
        if(operation_Mode == 1)
        {
            if(is_connected)
            {
                new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {


                        try
                        {
                            outputStream.write((int)cmd);
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }

        }
        else
        {
            if (is_connected)
            {
                new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {


                        if ((int)cmd >47 && (int)cmd <58)
                        {
                            Call<ResponseBody> speedCall = dataSender.setSpeed((int)cmd-'0');
                            speedCall.enqueue(new Callback<ResponseBody>()
                            {
                                @Override
                                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response)
                                {

                                }

                                @Override
                                public void onFailure(Call<ResponseBody> call, Throwable t)
                                {
                                }
                            });
                        }
                        else
                        {
                            switch (cmd)
                            {

                                case 'S':
                                    Call<ResponseBody> stopCall = dataSender.goStop();
                                    stopCall.enqueue(new Callback<ResponseBody>()
                                    {
                                        @Override
                                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response)
                                        {

                                        }

                                        @Override
                                        public void onFailure(Call<ResponseBody> call, Throwable t)
                                        {

                                        }
                                    });
                                    break;

                                case 'F':
                                    Call<ResponseBody> forwardCall = dataSender.goForward();
                                    forwardCall.enqueue(new Callback<ResponseBody>()
                                    {
                                        @Override
                                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response)
                                        {

                                        }

                                        @Override
                                        public void onFailure(Call<ResponseBody> call, Throwable t)
                                        {

                                        }
                                    });
                                    break;

                                case 'B':
                                    Call<ResponseBody> backwardCall = dataSender.goBackward();
                                    backwardCall.enqueue(new Callback<ResponseBody>()
                                    {
                                        @Override
                                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response)
                                        {

                                        }

                                        @Override
                                        public void onFailure(Call<ResponseBody> call, Throwable t)
                                        {

                                        }
                                    });
                                    break;

                                case 'L':
                                    Call<ResponseBody> leftCall = dataSender.goLeft();
                                    leftCall.enqueue(new Callback<ResponseBody>()
                                    {
                                        @Override
                                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response)
                                        {
                                        }

                                        @Override
                                        public void onFailure(Call<ResponseBody> call, Throwable t)
                                        {

                                        }
                                    });
                                    break;

                                case 'R':
                                    Call<ResponseBody> rightCall = dataSender.goRight();
                                    rightCall.enqueue(new Callback<ResponseBody>()
                                    {
                                        @Override
                                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response)
                                        {
                                        }

                                        @Override
                                        public void onFailure(Call<ResponseBody> call, Throwable t)
                                        {
                                        }
                                    });
                                    break;

                                case 'b':
                                    Call<ResponseBody> mode1Call = dataSender.setMode(0);
                                    mode1Call.enqueue(new Callback<ResponseBody>()
                                    {
                                        @Override
                                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response)
                                        {
                                        }

                                        @Override
                                        public void onFailure(Call<ResponseBody> call, Throwable t)
                                        {

                                        }
                                    });
                                    break;
                                case 'o':
                                    Call<ResponseBody> mode2Call = dataSender.setMode(1);
                                    mode2Call.enqueue(new Callback<ResponseBody>()
                                    {
                                        @Override
                                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response)
                                        {
                                        }

                                        @Override
                                        public void onFailure(Call<ResponseBody> call, Throwable t)
                                        {

                                        }
                                    });
                                    break;

                                case 'l':
                                    Call<ResponseBody> mode3Call = dataSender.setMode(2);
                                    mode3Call.enqueue(new Callback<ResponseBody>()
                                    {
                                        @Override
                                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response)
                                        {
                                        }

                                        @Override
                                        public void onFailure(Call<ResponseBody> call, Throwable t)
                                        {

                                        }
                                    });
                                    break;



                            }
                        }

                    }
                }).start();
            }
        }

    }

    public  void buildDialog()
    {
        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this)
                .setTitle("Enable WiFi")
                .setPositiveButton("OK", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {
                        if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.P)
                        {
                            wifiManager.setWifiEnabled(true);
                        }
                        else
                        {
                            Intent enablewifi = new Intent(Settings.ACTION_WIFI_SETTINGS);
                            startActivityForResult(enablewifi,1);
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

}