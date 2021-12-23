package com.example.wirelessrobot;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

import io.github.controlwear.virtual.joystick.android.JoystickView;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class JoystickActivity extends AppCompatActivity
{

    private SeekBar seekBar;
    public TextView speedText;
    private JoystickView joystickView;
    private CheckBox autospeedcheck;
    private ImageView backbtn;

    private boolean is_autoSpeed = false;

    private Retrofit.Builder retroBuilder;
    private Retrofit retrofit;
    private SendData dataSender;
    private int operation_Mode;

    private UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
    private BluetoothDevice hc05 = null;
    private BluetoothSocket btSocket = null;
    private boolean is_connected = false;

    private OutputStream outputStream =null;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.joystick);

        seekBar = (SeekBar) findViewById(R.id.speedbar);
        speedText = (TextView) findViewById(R.id.speed_text);
        joystickView = (JoystickView) findViewById(R.id.joyStick);
        autospeedcheck = (CheckBox) findViewById(R.id.speed_check);
        backbtn = (ImageView) findViewById(R.id.back_btn);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        if(bundle!=null)
        {
            String ipAddress =(String) bundle.get("Address");
            operation_Mode = (int) bundle.get("Mode");

            if (operation_Mode == 1)
            {
                new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        hc05 = btAdapter.getRemoteDevice(ipAddress);

                        int counter = 0;
                        do
                        {
                            try
                            {
                                btSocket = hc05.createRfcommSocketToServiceRecord(uuid);
                                btSocket.connect();
                                is_connected = true;
                                outputStream = btSocket.getOutputStream();
                            }
                            catch (IOException e)
                            {
                                e.printStackTrace();
                            }
                            counter++;
                        }while(!btSocket.isConnected() && counter <= 10);
                    }
                }).start();
            }
            else
            {
                retroBuilder  = new Retrofit.Builder()
                        .baseUrl("http://"+ipAddress+"/")
                        .addConverterFactory(GsonConverterFactory.create());

                retrofit = retroBuilder.build();
                dataSender = retrofit.create(SendData.class);
                is_connected  = true;
            }



        }
        else
        {
            finish();
        }


        joystickView.setOnMoveListener(new JoystickView.OnMoveListener()
        {
            @Override
            public void onMove(int angle, int strength)
            {
                new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {

                        if(angle == 0 && strength ==0)
                        {

                            if (is_autoSpeed)
                            {
                                speedText.setText("Speed : 0/9");
                            }




                            SendData('S');
                        }
                        else
                        {
                            char d = direction(angle,strength);
                            int s = (int) map(strength,0,100,0,9);



                            if(d != '0')
                            {
                                if(!is_autoSpeed)
                                {
                                    SendData(d);
                                }
                                else
                                {
                                    SendData(d);
                                    SendData((char)(s +'0'));
                                    speedText.setText("Speed : "+s+"/9");
                                }
                            }
                            else
                            {


                                SendData('S');

                                if (is_autoSpeed)
                                {
                                    speedText.setText("Speed : 0/9");
                                }
                            }

                        }
                    }
                }).start();



            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b)
            {
                if(!is_autoSpeed)
                {
                    speedText.setText("Speed : "+i+"/9");
                    SendData((char)(i+'0'));
                }
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

        autospeedcheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b)
            {
                is_autoSpeed = b;
            }
        });

        backbtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                finish();
            }
        });



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
                                    Call<ResponseBody> mode1Call = dataSender.setMode(1);
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
                                    Call<ResponseBody> mode2Call = dataSender.setMode(2);
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
                                    Call<ResponseBody> mode3Call = dataSender.setMode(3);
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


    public long map(long x, long in_min, long in_max, long out_min, long out_max)
    {
        return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
    }


    public char direction(int angle , int strength)
    {

        if (strength != 0)
        {
            if (angle > 70 && angle < 110)
            {
                return 'F';
            }
            if (angle > 160 && angle < 200)
            {
                return 'L';
            }
            if (angle > 250 && angle < 290)
            {
                return 'B';
            }
            if (angle > 340 && angle < 360 || angle >= 0 && angle <= 20)
            {
                return 'R';
            }
        }
        else
        {
            return 'S';
        }
        return '0';
    }

}
