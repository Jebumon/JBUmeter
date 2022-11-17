package com.example.jbumeter;


import static com.example.jbumeter.R.color.colorAccent;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Color;
import android.icu.number.Precision;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;




/////////////////////////////Main Activity{///////////////////////////
public class MainActivity extends AppCompatActivity {
    LineGraphSeries<DataPoint> series;

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putBoolean("Connected", connected);
        outState.putBoolean("Found BT", found_BT);
        outState.putInt("Command", command);
        outState.putString("inputDATA", inputDATA);
        outState.putString("displayValue", displayValue);



    }

    ////////////////////////Variable initialization{//////////////////////
    static final UUID mUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    ///////////////////////////////////////////////All my BT paired devices\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
    //[00:24:11:0E:A2:81, FE:F5:7F:69:FC:EB, 08:ED:B9:AB:D6:F2, 28:56:C1:46:56:10, 00:24:13:21:99:E5, 59:FE:01:D9:05:E1]
    //[00:24:11:0E:A2:81, FE:F5:7F:69:FC:EB, 08:ED:B9:AB:D6:F2, 28:56:C1:46:56:10, 00:24:13:21:99:E5, 59:FE:01:D9:05:E1, 98:D3:34:90:D4:C3]
    //HC-06 = [98:D3:34:90:D4:C3]
    private final String jbuBT_Address = "98:D3:34:90:D4:C3";
    BluetoothAdapter jbuAdapter;
    private BluetoothDevice jbuHC06;
    private BluetoothSocket jbuBTSocket = null;
    private InputStream inputStream = null;
    private OutputStream outputStream;
    public  boolean connected = false;
    public boolean found_BT = false;
    int  command;
    public String inputDATA,meterValue,displayValue, speakValue = "";
    Button connect_btn, voltage_BTN, speak_BTN;
    TextView result_View, statusBT, result_type, maxValue;
    Thread runningThread, connectingThread, speakingThread;
    boolean isRunning = true;
    boolean isSpeaking= false;
    TextToSpeech speakResult;
    GraphView graph;
    double x,y = 0.0;

    /////////////////////////////////////////////////////////////////Variable Initialization}







    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);////////Calling main screen activity

        findViewByIdes();
        implementButtonListeners();
        initGraph();
        initSpeak();

        if(savedInstanceState != null)
        {
            connected = savedInstanceState.getBoolean("Connected");
            found_BT  = savedInstanceState.getBoolean("Found BT");
            inputDATA = savedInstanceState.getString("inputDATA");
            displayValue=savedInstanceState.getString("displayValue");

        }

    }


///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void initGraph()
    {
        //double y,x;
        //x = -5.0;
        x = 0.0;
        graph = (GraphView) findViewById(R.id.graph);
        graph.setBackgroundResource(R.drawable.graph_background);
        series = new LineGraphSeries<DataPoint>();
        series.setColor(Color.GREEN);
        Viewport viewport = graph.getViewport();
        viewport.setYAxisBoundsManual(true);
        viewport.setScalable(true);
        viewport.setScrollable(true);
        viewport.setMinY(-3.0);
        viewport.setMaxY(3.0);
        viewport.setMaxX(100);
        viewport.setMinX(0);
        viewport.scrollToEnd();
        /*for(int i = 0; i<500; i++)
        {
            x = x + 0.1;
            y = Math.sin(x);
            series.appendData(new DataPoint(x,y),true, 500);
        }*/
        graph.addSeries(series);
    }
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void initSpeak()
    {
        speakResult = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status == TextToSpeech.SUCCESS){
                    int langStatus = speakResult.setLanguage(Locale.ENGLISH);

                    if(langStatus == TextToSpeech.LANG_MISSING_DATA || langStatus == TextToSpeech.LANG_NOT_SUPPORTED)
                    {
                        Toast.makeText(getApplicationContext(),"Language not supported", Toast.LENGTH_SHORT).show();
                        Log.e("SpeakValue", "Language not supported" );
                    }else
                        {
                            speak_BTN.setEnabled(true);
                        }
                }else
                    {
                        Log.e("SpeakValue", "Initialization Failed");
                    }
            }
        });

    }



    private void findViewByIdes() {
        //////////////////////Variable assignment{///////////////////
        connect_btn = (Button) findViewById(R.id.connect_bt_btn);
        voltage_BTN = (Button) findViewById(R.id.dc_voltage_btn);
        speak_BTN = (Button) findViewById(R.id.speak_result_btn);
        result_View = (TextView) findViewById(R.id.result_view);
        maxValue    = (TextView)findViewById(R.id.result_view3);
        statusBT = (TextView) findViewById(R.id.result_view4);
        result_type = (TextView) findViewById(R.id.result_type);
        /////////////////////////////////////////////////////////////Variable Assignment}

    }

     private void implementButtonListeners(){

        //////////////////////Button listeners//////////////////////
        connect_btn.setOnClickListener(new View.OnClickListener() ////////<Connect Button>
        {
            @Override
            public void onClick(View v) {

                System.out.println("Fount BT =========================== " + found_BT);
                System.out.println("Connected BT =========================== " + connected);
                result_View.setText("JBUmeter");
                result_type.setText("");

                connectingThread = new Thread(new Runnable() {
                    @Override
                    public void run() {

                        if(!found_BT)
                        {
                            BTinit();                       //Boolean method to check Bluetooth is available or not
                            command = 82;
                        }
                        try {
                            Thread.sleep(1);
                            ConnectBTN();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        System.out.println("-------------------------connected-------------------");
                    }
                }); connectingThread.start();

            }
        });////////////////////connect Button}


        voltage_BTN.setOnClickListener(new View.OnClickListener() { ///////<Voltage Button>
            @Override
            public void onClick(View v) {


                if(isRunning)
                {
                    isRunning = false;
                    voltage_BTN.setBackgroundResource(R.drawable.button_border);
                }else
                    {
                        isRunning = true;
                        voltage_BTN.setBackgroundResource(R.drawable.button_pressed);
                    }
                if(connected) {

                    command = 44;
                    try {
                        outputStream = jbuBTSocket.getOutputStream(); //Already assigned while connecting.
                        outputStream.write(44);
                        //Toast.makeText(getApplicationContext(),"Sending command",Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        Toast.makeText(getApplicationContext(), "Device not found", Toast.LENGTH_SHORT).show();
                        connect_btn.setText("Connect Device");
                        found_BT = false;
                        connected = false;
                        statusBT.setText("DEVICE NOT REACHABLE");
                        e.printStackTrace();
                    }

                }
                else
                    {
                        statusBT.setText("DEVICE NOT REACHABLE");
                    }

                //Thread runningThread;
                //boolean isRunning;

                runningThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while (isRunning && connected)
                        {
                            x = x + 1.0;

                            try {
                                outputStream = jbuBTSocket.getOutputStream(); //Already assigned while connecting.
                                outputStream.write(44);
                                DATAreceive();
                                speakValue = meterValue + " Volt dc" ;
                                Thread.sleep(1);
                                System.out.println("---------------------loop-------------------");
                            }catch (Exception e)
                            {

                            }

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    series.appendData(new DataPoint(x,y),true, 5000);
                                }
                            });


                        }

                    }
                }); runningThread.start();

            }
        });////////////////////voltage Button}


         speak_BTN.setOnClickListener(new View.OnClickListener() {//////////////////Speak button
             @Override
             public void onClick(View v) {

                 if(!isSpeaking)
                 {
                     isSpeaking = true;
                     speak_BTN.setBackgroundResource(R.drawable.button_pressed);
                 }else
                 {
                     isSpeaking = false;
                     speak_BTN.setBackgroundResource(R.drawable.button_border);
                     speakResult.stop();
                 }
                 float pitch = 1;
                 float speed = 1;
                 speakResult.setPitch(pitch);
                 speakResult.setSpeechRate(speed);
                 if(!connected){
                     speakValue = "It's more than a multimeter, Designed and developed by Jebumon K Thomas. Please connect the device";
                 }
                 speakingThread = new Thread(new Runnable() {
                     @Override
                     public void run() {

                      do {
                          try {
                              System.out.println("=============================Speaking========================");

                              speakResult.speak(speakValue, TextToSpeech.QUEUE_FLUSH, null);

                              Thread.sleep(3000);
                          } catch (InterruptedException e) {
                              e.printStackTrace();
                          }
                      }while ( isSpeaking && connected);

                     }
                 }); speakingThread.start();
             }
         });


    }////////////////////////////////////////////////////////Implement listeners}


    public void ConnectBTN()
    {
        if(!connected)
        {
            System.out.println("----------------------------ConnectBTN-------------");
            statusBT.setText("Connecting...");
            BTconnect();    //Method to connect BT
        }
        else
        {
            try
            {
                //System.out.println("Try to close ===========================================");
                //System.out.println("BT Connected Status = " + connected + jbuBTSocket.isConnected());
                jbuBTSocket.close();                            //Bluetooth disconnecting
                connect_btn.setText("Connect Device");
                connect_btn.setBackgroundResource(R.drawable.button_border);
                statusBT.setText("Device Disconnected");
                result_type.setText("");
                connected = false;
                System.out.println("BT Connected Status = " + connected + jbuBTSocket.isConnected());
                System.out.println("Try to close ===========================================");

            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

    }


    public void DATAreceive(){
        final InputStream inputStream;
        System.out.println(" Thread called 888888888888888888888888888888888888");
        InputStream tempIn = null;

        try {
            tempIn = jbuBTSocket.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        inputStream = tempIn;

        System.out.println(" Out put = ////////////////////////");
        BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
        String x = "";
        String total = "";
        try {
            x = r.readLine();
            System.out.println(" Out put =1 =" + x);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println(" Out put =" + x);
        int length = x.length();
        System.out.println(" String length =" + length);

////////////////////////////Splitting input data///////////////////////////
        String[] splitData = x.split("-");
        String dataCount = splitData[0];
        String rawData = splitData[1];
        String dataType = splitData[2];
        String valueData = splitData[3];

        displayValue = x;
        DecimalFormat df = new DecimalFormat("0.000");
        double tempMeterValue = ((Double.parseDouble(rawData) - 512) *5) / 1024 ; // 512 is the middle value of 10bit adc(1024/2)

        meterValue = df.format(tempMeterValue) + " ";
        //meterValue = valueData;
        System.out.println("Voltage =" + meterValue);
        result_View.setText(meterValue);
        //y = Double.parseDouble(meterValue);     //For Graph
        y= tempMeterValue;
        result_type.setText(dataType);
        statusBT.setText(displayValue);

    }

    /////////////////////////Bluetooth Initialization{////////////////////
    public boolean BTinit()
    {
        found_BT = false;   //initializing flag for bluetooth found or not
        jbuAdapter = BluetoothAdapter.getDefaultAdapter();
        System.out.println("***************************************************");
        System.out.println("jbuAdapter " +jbuAdapter);
        System.out.println("***************************************************");

        if(jbuAdapter == null)
        {
            Toast.makeText(getApplicationContext(),"Device doesn't support bluetooth", Toast.LENGTH_SHORT).show();
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
        Set<BluetoothDevice> pairedDevices = jbuAdapter.getBondedDevices();

        if(pairedDevices.isEmpty())
        {
            Toast.makeText(getApplicationContext(),"Please pair the device first", Toast.LENGTH_SHORT).show();
        }
        else
            {
                for (BluetoothDevice iterator : pairedDevices)
                {
                    /*System.out.println("***************************************************");
                    System.out.println("BT : " + iterator);
                    System.out.println("***************************************************");*/
                    String tempIterator = iterator.toString(); //iterator Object to string for compare
                    System.out.println(tempIterator);
                    //System.out.println(jbuBT_Address);

                    if(tempIterator.equals(jbuBT_Address)) // checking device is found or not
                    {
                        System.out.println("*************************Found jbuBT***************************");
                        jbuHC06 = iterator; //Module address fetching for connecting
                        found_BT = true;
                        break;
                    }
                }
            }

        return found_BT;
    }/////////////////////////////////////////////////////////Bluetooth Initialization}

    //////////////////////////////Bluetooth Connect{//////////////////////////////////
    public boolean BTconnect()
    {
        System.out.println("-------------------BTconnect----------------------");
        int counter = 0;
        do {
            try {
                //Toast.makeText(getApplicationContext(),"Connecting...",Toast.LENGTH_SHORT).show();
                jbuBTSocket = jbuHC06.createRfcommSocketToServiceRecord(mUUID);
                jbuBTSocket.connect();
                statusBT.setText("Device Connected !");
                connected = true;
            } catch (IOException e)
            {
                e.printStackTrace();
                connected = false;
                statusBT.setText("Retry...");
            }
            counter++;
        }while (!jbuBTSocket.isConnected() && counter <3);

        if(connected)
        {
            try
            {
                inputStream  = jbuBTSocket.getInputStream();
                outputStream = jbuBTSocket.getOutputStream();
               outputStream.write(82);
               System.out.println("JBUmeter is connected successfully");
               //statusBT.setText("JBUmeter is connected");
               connect_btn.setText("Disconnect");
               connect_btn.setBackgroundResource(R.drawable.button_pressed);
               System.out.println(outputStream);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        return connected;
    } //////////////////////////////Bluetooth Connect}//////////////////////////////////








       /* jbuAdapter = BluetoothAdapter.getDefaultAdapter();
        System.out.println(jbuAdapter.getBondedDevices());

        jbuHC06 = jbuAdapter.getRemoteDevice(jbuBT_Address);
        System.out.println(jbuHC06.getName());


        int counter = 0;
        do{
            try {
                jbuBTSocket = jbuHC06.createInsecureRfcommSocketToServiceRecord(mUUID);
                System.out.println(jbuBTSocket);
                jbuBTSocket.connect();
                System.out.println(jbuBTSocket.isConnected());

            }catch (IOException e){

                e.printStackTrace();
            }
            counter++;

        }while (!jbuBTSocket.isConnected() && counter < 3);

        try {
            outputStream = jbuBTSocket.getOutputStream();
            outputStream.write(48);
        } catch (IOException e) {
            e.printStackTrace();
        }



        try {
            inputStream = jbuBTSocket.getInputStream();
            inputStream.skip(inputStream.available());

            for(int i = 0; i< 26; i++) {

                byte b = (byte) inputStream.read();
                System.out.println((char)b);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            jbuBTSocket.close();
            System.out.println(jbuBTSocket.isConnected());

        } catch (IOException e) {
            e.printStackTrace();
        }*/


    }/////////////////////////////Main activity}

