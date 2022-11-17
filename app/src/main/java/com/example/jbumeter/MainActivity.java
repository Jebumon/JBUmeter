package com.example.jbumeter;

import androidx.appcompat.app.AppCompatActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
//import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;


public class MainActivity extends AppCompatActivity {

    // Declaring variables
    Button dc_voltage_btn, ac_voltage_btn, resistance_btn, capacitance_btn, inductance_btn, continuity_btn, speak_result_btn, connect_bt_btn;
    //String input_data; //Input data from probe.
    //String control_in; //Input command from probe.
    String control_out; //Output command for control probe parameters.
    TextView result_view; // Display Measurements
    TextView result_view2;
    TextView result_view3;
    TextView result_view4;
    TextView label_name;  // Just app name
    private BluetoothDevice jbuDevice;
    private BluetoothAdapter jbuAdapter;
    private BluetoothSocket jbuSocket;
    private OutputStream outputStream;
    private InputStream inputStream;

    static final UUID mUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    ///////////////////////////////////////////////All my BT paired devices\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
    //[00:24:11:0E:A2:81, FE:F5:7F:69:FC:EB, 08:ED:B9:AB:D6:F2, 28:56:C1:46:56:10, 00:24:13:21:99:E5, 59:FE:01:D9:05:E1]
    //[00:24:11:0E:A2:81, FE:F5:7F:69:FC:EB, 08:ED:B9:AB:D6:F2, 28:56:C1:46:56:10, 00:24:13:21:99:E5, 59:FE:01:D9:05:E1, 98:D3:34:90:D4:C3]
    //HC-06 = [98:D3:34:90:D4:C3]
    private final String jbuBT_Address = "98:D3:34:90:D4:C3";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewByIdes();       //Method calling Assignment of all widgets.
        implementListeners();  //Method calling switch click listeners
    }


    private void findViewByIdes()
    {
        //Assignment of all widgets.
        dc_voltage_btn = findViewById(R.id.dc_voltage_btn);
        ac_voltage_btn = findViewById(R.id.ac_voltage_btn);
        resistance_btn = findViewById(R.id.resistance_btn);
        capacitance_btn = findViewById(R.id.capacitance_btn);
        inductance_btn = findViewById(R.id.inductance_btn);
        continuity_btn = findViewById(R.id.continuity_btn);
        speak_result_btn = findViewById(R.id.speak_result_btn);
        connect_bt_btn = findViewById(R.id.connect_bt_btn);

        result_view = findViewById(R.id.result_view);
        result_view2 = findViewById(R.id.result_view2);
        result_view3 = findViewById(R.id.result_view3);
        result_view4 = findViewById(R.id.result_view4);
        label_name = findViewById(R.id.label_name);
    }


    private void implementListeners(){      //Method for switch click listeners
        // DC Voltage Click Event
        dc_voltage_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "DC Voltage selected", Toast.LENGTH_SHORT).show();
                Log.v("dc_voltage_btn", "DC Voltage selected");
                result_view.setText("0.000V⎓");
                result_view2.setText("0.000V≂");
                result_view3.setText("0.000 Ω");
                result_view4.setText("0.000uF");
                control_out = "DCV";
                try {
                    outputStream.write(control_out.getBytes()); //Transmit control data to bluetooth meter
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        // AC Voltage Click Event
        ac_voltage_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "AC Voltage selected", Toast.LENGTH_SHORT).show();
                Log.v("ac_voltage_btn", "AC Voltage selected");
                result_view.setText("0.000V≂");
                result_view2.setText("0.000V⎓");
                result_view3.setText("0.000 Ω");
                result_view4.setText("0.000uF");
                control_out = "ACV";
                try {
                    outputStream.write(control_out.getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        resistance_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Resistance Selected", Toast.LENGTH_SHORT).show();
                Log.v("resistance_btn", "Resistance Selected");
                result_view.setText("Resistance");
                control_out = "RES";
                try {
                    outputStream.write(control_out.getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        capacitance_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Capacitance Selected", Toast.LENGTH_SHORT).show();
                Log.v("capacitance_btn", "Capacitance Selected");
                result_view.setText("Capacitance");
                control_out = "CAP";
                try {
                    outputStream.write(control_out.getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        inductance_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Inductance Selected", Toast.LENGTH_SHORT).show();
                Log.v("inductance_btn", "Inductance Selected");
                result_view.setText("Inductance");
                control_out = "IND";
                try {
                    outputStream.write(control_out.getBytes()); //Data send to bluetooth meter
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        continuity_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Continuity Selected", Toast.LENGTH_SHORT).show();
                result_view.setText("Continuity");
                control_out = "CON";
                try {
                    outputStream.write(control_out.getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        connect_bt_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (BTinit()) {
                    jbuBTconnect();
                }

            }
        });
    }


    public boolean BTinit()
    {
        boolean found_BT = false;
        jbuAdapter = BluetoothAdapter.getDefaultAdapter();

        if(jbuAdapter == null)
        {
            Toast.makeText(MainActivity.this, "Device not supports Bluetooth", Toast.LENGTH_SHORT).show();
        }
        if(!jbuAdapter.isEnabled())
        {
            Intent enableAdapter = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableAdapter,0);

            try
            {
                Thread.sleep(1000);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }

        Set<BluetoothDevice> bondedDevices = jbuAdapter.getBondedDevices();
        if(bondedDevices.isEmpty())
        {
            Toast.makeText(MainActivity.this, "First pair the device", Toast.LENGTH_SHORT).show();
        }
        else
        {
            for (BluetoothDevice iterator : bondedDevices)
            {
                String tempIterator = iterator.toString();
                if (tempIterator.equals(jbuBT_Address)) {
                    jbuDevice = iterator;
                    found_BT = true;
                    break;
                }
            }
        }
        return found_BT;
    }

    public boolean jbuBTconnect()
    {
        boolean connected = true;

        try
        {
            jbuSocket = jbuDevice.createRfcommSocketToServiceRecord(mUUID);
            jbuSocket.connect();
            Toast.makeText(getApplicationContext(), "Device successfully connected", Toast.LENGTH_LONG).show();
        } catch (IOException e)
        {
            e.printStackTrace();
            connected = false;
        }
        if(connected)
        {
            try
            {
                outputStream = jbuSocket.getOutputStream();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        return connected;

    }


}


