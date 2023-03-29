package com.example.cartphone;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Checkout extends AppCompatActivity {

    TextView txt;
    Button btn;
    TextView txt2;
    ImageView imageView;
    ImageView backToCartBtn;
    TextView total;
    Connection connect;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);
        txt = (TextView) findViewById(R.id.textView);
        txt2 = (TextView) findViewById(R.id.textView2);
        btn = (Button) findViewById(R.id.button);
        imageView = (ImageView) findViewById(R.id.imageView);
        backToCartBtn = (ImageView) findViewById(R.id.backIcon);
        backToCartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backToCart();
            }
        });

        btn.setOnClickListener(v -> {
            getStringDT();
            barcode();
        });

        total = findViewById(R.id.Total);
        total.setText(getTotal(""));

    }
    public void getStringDT(){
        DateTimeFormatter dtf;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocalDate now = LocalDate.now();
            dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            String format = dtf.format(now);
            txt.setText(format);
        }
    }
    public void backToCart(){
        Intent intent = new Intent(this, CartList.class);
        startActivity(intent);
    }

    public String getTotal(String total){
        try {
            ConnectionHelper connectionHelper = new ConnectionHelper();
            connect = connectionHelper.connectionClass();
            if(connect!=null){
                String query = "SELECT * FROM SessionTbl WHERE Session_ID='Session1' AND Session_Active='Active'";
                Statement st = connect.createStatement();
                ResultSet rs = st.executeQuery(query);

                if (rs.next()){
                    total = "PHP: " + rs.getString("Session_Total");
                }
            }
        }catch (Exception ignored){
        }
        return total;
    }

    public void barcode(){
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try{
            BitMatrix bitMatrix = multiFormatWriter.encode("Session1", BarcodeFormat.CODE_128
                    ,imageView.getWidth(),imageView.getHeight());
            Bitmap bitmap = Bitmap.createBitmap(imageView.getWidth(),imageView.getHeight(),
                    Bitmap.Config.RGB_565);
            for (int i = 0; i < imageView.getWidth(); i++){
                for (int j = 0; j < imageView.getHeight();j++){
                    bitmap.setPixel(i,j,bitMatrix.get(i,j)? Color.BLACK:Color.WHITE);
                }
            }
            imageView.setImageBitmap(bitmap);
            txt2.setText("Session1");
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}