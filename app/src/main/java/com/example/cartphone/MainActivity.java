package com.example.cartphone;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
public class MainActivity extends AppCompatActivity {


    Connection connect;
    String connectionResult = "";
    Button btn_scan;
    Button btnCart;
    Data data = new Data();


    TextView productName;
    TextView productPrice;
    TextView total;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_scan = findViewById(R.id.btn_scan);
        btn_scan.setOnClickListener(v->
                scanCode());


        btnCart = findViewById(R.id.btn_cart);
        btnCart.setOnClickListener(v -> openCart());

        productName = findViewById(R.id.productName);
        productPrice = findViewById(R.id.productPrice);
        total = findViewById(R.id.Total);
        total.setText(getTotal(""));

    }

    //camera scan function and added features
    private void scanCode()
    {
        ScanOptions options = new ScanOptions();
        options.setPrompt("Volume up to flash on");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.setCaptureActivity(CaptureAct.class);
        barLauncher.launch(options);
    }

    // this part is for Barcode Scanner Camera
    @SuppressLint("SetTextI18n")
    ActivityResultLauncher<ScanOptions> barLauncher = registerForActivityResult(new ScanContract(), result->
    {
        if(result.getContents() !=null)
        {
            data.setProductIDStr(result.getContents());

            try {
                ConnectionHelper connectionHelper = new ConnectionHelper();
                connect = connectionHelper.connectionClass();
                if(connect!=null){
                    String query = "SELECT * FROM Product WHERE Product_ID='" + data.getProductIDStr() + "'";
                    Statement st = connect.createStatement();
                    ResultSet rs = st.executeQuery(query);

                    if (rs.next()){
                        data.setProductName(rs.getString("Product_Name"));
                        float prdPriceFlt = Float.parseFloat(rs.getString("Product_Price"));
                        data.setProductPrice(prdPriceFlt);

                        quantityPrompt();

                        Toast.makeText(MainActivity.this,"Added to cart",Toast.LENGTH_SHORT).show();


                    }else{
                        productName.setText("Product doesn't exist in the database");
                    }
                }else{
                    connectionResult = "Check Connection";
                }
            }catch (Exception e){
                productName.setText("went to exception on camera");
            }

        }
    });

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

    //Dialog Box Prompting Quantity
    public void quantityPrompt(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Quantity");
        builder.setMessage("Input Quantity");
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);
        builder.setPositiveButton("Confirm", (dialog, which) -> {
            String qty = input.getText().toString().trim();

            data.setProductQuantity(qty);

            float qtyXPrice = Integer.parseInt(data.getProductQuantity())*data.getProductPrice();
            data.setProductSubTotal(String.valueOf(qtyXPrice));

            Toast.makeText(MainActivity.this, ""+qtyXPrice, Toast.LENGTH_SHORT).show();

            try {

                String query = "SELECT * FROM Cart WHERE Product_ID='" + data.getProductIDStr() + "'";
                Statement st = connect.createStatement();
                ResultSet rs = st.executeQuery(query);

                if (rs.next()){
                    //if it exist in the cart
                    int updQty = Integer.parseInt(data.getProductQuantity()) + rs.getInt("Quantity");
                    data.setProductQuantity(String.valueOf(updQty));

                    float qtyXPriceUpdate = Integer.parseInt(data.getProductQuantity())*data.getProductPrice();
                    data.setProductSubTotal(String.valueOf(qtyXPriceUpdate));

                    String updateQTY ="UPDATE Cart\n" +
                            "SET Quantity = "+data.getProductQuantity()+", Sub_Total = "+ data.getProductSubTotal()+" \n" +
                            "WHERE Product_ID = '"+data.getProductIDStr()+"'";
                    Statement updateQTYSt = connect.createStatement();
                    updateQTYSt.executeUpdate(updateQTY);

                    //query to update Overall total in the session
                    String sessionQuery = "UPDATE S\n" +
                            "SET S.Session_Total = (SELECT SUM(Cart.Sub_Total) FROM Cart)\n" +
                            "FROM SessionTbl S\n" +
                            "WHERE S.Session_ID = 'Session1'";
                    Statement sessionSt = connect.createStatement();
                    sessionSt.executeUpdate(sessionQuery);

                    Toast.makeText(MainActivity.this, "Added to Cart", Toast.LENGTH_SHORT).show();

                }else if (!rs.next()){
                    //if it doesn't exist in the cart

                    String addToCart = "INSERT INTO Cart(Session_ID,Product_ID,Product_Name, Quantity,Sub_Total) " +
                            "VALUES  ('Session1','"+data.getProductIDStr()+"','"+data.getProductName()+"','"+data.getProductQuantity()+"','"+data.getProductSubTotal()+"');";
                    Statement addToCartSt = connect.createStatement();
                    addToCartSt.executeUpdate(addToCart);

                    //query to update Overall total in the session
                    String sessionQuery = "UPDATE S\n" +
                            "SET S.Session_Total = (SELECT SUM(Cart.Sub_Total) FROM Cart)\n" +
                            "FROM SessionTbl S\n" +
                            "WHERE S.Session_ID = 'Session1'";
                    Statement sessionSt = connect.createStatement();
                    sessionSt.executeUpdate(sessionQuery);
                    Toast.makeText(MainActivity.this, "Added to Cart", Toast.LENGTH_SHORT).show();

                }else{
                    Toast.makeText(MainActivity.this, "error", Toast.LENGTH_SHORT).show();

                }

            }catch (Exception e){
                Toast.makeText(MainActivity.this, "i do not know", Toast.LENGTH_SHORT).show();

            }

        });
        builder.setNegativeButton("Cancel", (dialog, which) -> Toast.makeText(MainActivity.this, "Canceled", Toast.LENGTH_SHORT).show());
        builder.create().show();
    }

    //open cart
    public void openCart(){
        Intent intent = new Intent(this, CartList.class);
        startActivity(intent);
    }
}