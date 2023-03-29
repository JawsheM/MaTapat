package com.example.cartphone;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;



public class CartList extends AppCompatActivity {

    private ArrayList<ClassListItems> itemsArrayList; //List Items Array
    private MyAppAdapter myAppAdapter;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private boolean success = false;
    private ConnectionHelper connectionClass;
    Connection connect;


    private Button checkoutBtn;
    private ImageView backBtn;
    private Button delete;
    TextView total;

    Data data = new Data();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart_list);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerViewID);
        recyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);

        connectionClass = new ConnectionHelper();
        itemsArrayList = new ArrayList<ClassListItems>();

        total = (TextView) findViewById(R.id.SubTotalOverAll);
        total.setText(getTotal(""));

        backBtn = (ImageView) findViewById(R.id.backIcon);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backToMain();
            }
        });

        delete = (Button) findViewById(R.id.deleteBtn);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteItem();
            }
        });

        checkoutBtn = (Button) findViewById(R.id.checkoutBtn);
        checkoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCheckout();
            }
        });


        SyncData orderData = new SyncData();
        orderData.execute("");

    }

    public void openCheckout(){
        Intent intent = new Intent(this, Checkout.class);
        startActivity(intent);
    }

    public void backToMain(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }


    //camera scan function and added features
    private void deleteItem()
    {
        ScanOptions options = new ScanOptions();
        options.setPrompt("Volume up to flash on");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.setCaptureActivity(CaptureAct.class);
        barLaucher.launch(options);
    }

    // this part is for Barcode Scanner Camera
    ActivityResultLauncher<ScanOptions> barLaucher = registerForActivityResult(new ScanContract(), result->
    {
        if(result.getContents() !=null)
        {
            ConnectionHelper connectionHelper = new ConnectionHelper();
            connect = connectionHelper.connectionClass();
            try {
                if(connect!=null){
                    String query = "SELECT * FROM Cart WHERE Product_ID='"+result.getContents()+"'";
                    Statement st = connect.createStatement();
                    ResultSet rs = st.executeQuery(query);

                    if (rs.next()){
                        Delete(result.getContents());
                    }else{
                        // nothing really happens probobly a toast
                        Toast.makeText(CartList.this,"No Matching ProductId in Cart",Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(CartList.this,"Check Connection to database",Toast.LENGTH_SHORT).show();
                }
            }catch (Exception e){
                //Exception
                Toast.makeText(CartList.this,"Exception",Toast.LENGTH_SHORT).show();
            }
        }
    });

    public void Delete(String productID){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete");
        builder.setMessage("Do you want to delete");
        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                try {
                    //if product is in the cart
                    String deleteItem = "DELETE FROM Cart WHERE Product_ID="+ productID+";";

                    Statement deleteItemSt = connect.createStatement();
                    deleteItemSt.executeUpdate(deleteItem);

                    String sessionQuery = "UPDATE S\n" +
                            "SET S.Session_Total = (SELECT SUM(Cart.Sub_Total) FROM Cart)\n" +
                            "FROM SessionTbl S\n" +
                            "WHERE S.Session_ID = 'Session1'";
                    Statement sessionSt = connect.createStatement();
                    sessionSt.executeUpdate(sessionQuery);

                    Toast.makeText(CartList.this,"Deleted From Cart",Toast.LENGTH_SHORT).show();

                }catch (Exception e){
                    Toast.makeText(CartList.this, "i do not know (Cart List)", Toast.LENGTH_SHORT).show();

                }

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
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
                }else{
                }
            }else{
            }
        }catch (Exception e){
        }
        return total;
    }


    public class SyncData extends AsyncTask<String, String, String> {
        String msg = "";
        ProgressDialog progress;


        @Override
        protected void onPreExecute(){
            progress = ProgressDialog.show(CartList.this, "Synchronising",
                    "Cart View Loading! Please Wait...",true);

        }

        @Override
        protected String doInBackground(String... strings) {
            try {

                Connection connect = connectionClass.connectionClass();

                if (connect == null) {
                    success = false;
                } else {
                    String query = "SELECT * FROM Cart";
                    Statement st = connect.createStatement();
                    ResultSet rs = st.executeQuery(query);

                    while (rs.next()) {
                        try {
                            itemsArrayList.add(new ClassListItems(rs.getString("Product_Name")
                                    ,rs.getInt("Quantity"), rs.getFloat("Sub_Total")));
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                    msg = "Success";
                    success = true;
                }

            } catch (Exception e) {

            }
            return msg;
        }

        @Override
        protected void onPostExecute(String msg){
            progress.dismiss();
            Toast.makeText(CartList.this, msg + "", Toast.LENGTH_SHORT).show();
            if(success == false){

            }else{
                try {
                    myAppAdapter = new MyAppAdapter(itemsArrayList, CartList.this);
                    recyclerView.setAdapter(myAppAdapter);
                }catch (Exception ex){

                }
            }
        }

    }

    public class MyAppAdapter extends RecyclerView.Adapter<MyAppAdapter.ViewHolder>{
        private List<ClassListItems> values;
        public Context context;

        public class ViewHolder extends RecyclerView.ViewHolder{
            public TextView productNameView;
            public TextView quantityView;
            public TextView subTotalView;
            public View layout;

            public ViewHolder(View v){
                super(v);
                layout = v;
                productNameView = (TextView) v.findViewById(R.id.productName);
                quantityView = (TextView) v.findViewById(R.id.quantity);
                subTotalView = (TextView) v.findViewById(R.id.subTotal);
            }
        }

        public MyAppAdapter (ArrayList<ClassListItems> myDataSet, Context context){
            values = myDataSet;
            this.context = context;
        }

        // Create new views (invoked by the layout manager) and inflates
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            // create new view
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View v = inflater.inflate(R.layout.list_content, parent, false);
            ViewHolder vh = new ViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            final ClassListItems classListItems = values.get(position);
            holder.productNameView.setText(classListItems.getProductName());
            holder.quantityView.setText(String.valueOf(classListItems.getquantity()));
            holder.subTotalView.setText(String.valueOf(classListItems.getsubTotal()));

        }
        @Override
        public int getItemCount() {
            return values.size();
        }

    }


}