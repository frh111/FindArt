package com.example.artshopv1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
//import com.example.artshopv1.PaymentService.PaymentService;
//import com.example.artshopv1.PaymentService.PaymentResult;

import com.stripe.android.PaymentConfiguration;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    Button buttonLogout;
    FirebaseAuth auth;
    FirebaseUser user;
    TextView loggedUser;
    PaymentSheet paymentSheet;
    String paymentIntentClientSecret;
    PaymentSheet.CustomerConfiguration configuration;

    private GridView gridView;

    private Button cartButton;

    private List<String> imageUrls = new ArrayList<>(); // list = colectie ordonata care permite redimensionarea dinamica

    private boolean[] selectedImages;   //selectedImages indică ce imagini au fost selectate de utilizator în GridView.

    private int cartCount = 0; // cartCount păstrează numărul de articole din coșul de cumpărături. Inițial, acesta este 0, deoarece coșul este gol la început.

    double totalPrice = 0.0;

    private List<String> cartItems = new ArrayList<>();

    //artItems păstrează URL-urile imaginilor care au fost adăugate în coșul de cumpărături.
    // Fiecare element din această listă este un string care reprezintă URL-ul unei imagini.

    private Map<String, String> imagePrices = new HashMap<>();

    //magePrices păstrează prețurile imaginilor, unde fiecare cheie este URL-ul unei imagini și valoarea asociată este prețul respectivei imagini.


    // Inițializarea elementelor UI și a referințelor Firebase
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        paymentSheet = new PaymentSheet(this, this::onPaymentSheetResult);
        buttonLogout = findViewById(R.id.buttonLogout);
        gridView = findViewById(R.id.gridView);
        cartButton = findViewById(R.id.cartButton);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        loggedUser = findViewById(R.id.loggedUser);

        // Verificarea dacă utilizatorul este autentificat
        if (user == null){
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            loggedUser.setText(user.getEmail());
        }


        // Se creează un nou obiect Intent pentru a porni activitatea MainActivity.
        // Obțineți contextul aplicației cu getApplicationContext() și specificați clasa activității pe care doriți să o porniți (MainActivity).
        //Se pornește activitatea MainActivity, iar apoi activitatea curentă este închisă cu ajutorul metodei finish().


        // Butonul de deconectare din pagina cu GridImages

        buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        });


        // Referința la baza de date Firebase pentru imaginile de afișat

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("images");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    ImageWithPrice image = snapshot.getValue(ImageWithPrice.class);
                    if (image != null) {
                        String imageUrl = snapshot.child("img").getValue(String.class);
                        String price = image.getPrice();
                        Log.d("Image URL", imageUrl);
                        Log.d("Image Price", price);
                        imageUrls.add(imageUrl);
                        imagePrices.put(imageUrl, price);
                    }
                }
                // Setarea adaptorului pentru grid view după ce URL-urile imaginilor au fost obținute

                ImageAdapter adapter = new ImageAdapter(MainActivity.this, imageUrls, imagePrices);
                gridView.setAdapter(adapter);

                selectedImages = new boolean[imageUrls.size()];
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Failed to load images", Toast.LENGTH_SHORT).show();
            }
        });

        // Set click listener for grid items
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                LinearLayout linearLayout = (LinearLayout) view;
                ImageView imageView = linearLayout.findViewById(R.id.imageView);
                if (selectedImages[position]) {
                    imageView.setBackgroundColor(0); // Deselect the image
                    selectedImages[position] = false;
                } else {
                    imageView.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_light)); // Select the image
                    selectedImages[position] = true;
                }
            }
        });


        cartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCart();
            }
        });
    }

    private void onPaymentSheetResult(PaymentSheetResult paymentSheetResult) {
        if(paymentSheetResult instanceof  PaymentSheetResult.Canceled){
            Toast.makeText(this, "Canceled", Toast.LENGTH_SHORT).show();
        }
        if(paymentSheetResult instanceof  PaymentSheetResult.Failed){
            Toast.makeText(this,((PaymentSheetResult.Failed) paymentSheetResult).getError().getMessage(), Toast.LENGTH_SHORT).show();
        }
        if(paymentSheetResult instanceof  PaymentSheetResult.Completed){
            Toast.makeText(this, "Payment Success", Toast.LENGTH_SHORT).show();
        }
    }

    private void showCart() {
        totalPrice = 0.0;
        // Create a custom layout for the dialog
        View cartView = getLayoutInflater().inflate(R.layout.custom_cart_dialog, null);


        // Initialize dialog builder with custom layout
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(cartView);

        // Get references to views in the custom layout
//        TextView cartItemsTextView = cartView.findViewById(R.id.cart_items_text);
        TextView totalPriceTextView = cartView.findViewById(R.id.total_price_text);
        Button payButton = cartView.findViewById(R.id.pay_button);
        Button cancelButton = cartView.findViewById(R.id.cancel_button);

        // Convert selected images to cart items
        cartItems.clear();
         // Initialize total price
        for (int i = 0; i < selectedImages.length; i++) {
            //Se parcurg toate imaginile pentru a verifica care dintre ele sunt selectate și pentru a actualiza coșul de cumpărături.
            if (selectedImages[i]) {
                cartItems.add(imageUrls.get(i));

                String price = imagePrices.get(imageUrls.get(i));
                //Obține prețul imaginii selectate din map-ul imagePrices folosind URL-ul imaginii.
                if (price != null) {
                    totalPrice += Double.parseDouble(price);
                }
            }
        }


        totalPriceTextView.setText(String.format("Total Price: $%.2f", totalPrice)); // Set total price

        // Create and show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();

        // Set onClickListener for the pay button
        payButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cartItems.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Cart is empty", Toast.LENGTH_SHORT).show();
                } else {
                    String totalAmount = Double.toString(totalPrice);
                    stripeApi(totalAmount);
                }
            }
        });
        // Set onClickListener for the cancel button
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Dismiss the dialog
                dialog.dismiss();
            }
        });
    }
    public void stripeApi(String totalAmount) {  //Metoda inițiază o cerere de plată către server folosind suma totală specificată.
        RequestQueue queue = Volley.newRequestQueue(this);

        //Creează o nouă coadă de cereri Volley.
        //RequestQueue gestionează trimiterea cererilor HTTP și răspunsurile primite.

        String url = "http://192.168.100.15/findartapp/stripe/index.php";
        //Specifică URL-ul serverului care va gestiona cererea de plată.

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {

            //StringRequest este folosit pentru a trimite date către server și pentru a primi un răspuns sub formă de text.

                    // Definește un listener pentru răspunsurile de succes. În acest caz, răspunsul este convertit într-un obiect JSON.
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            configuration = new PaymentSheet.CustomerConfiguration(
                                    jsonObject.getString("customer"),
                                    jsonObject.getString("ephemeralKey")
                            );
                            paymentIntentClientSecret = jsonObject.getString("paymentIntent");
                            PaymentConfiguration.init(getApplicationContext(), jsonObject.getString("publishableKey"));

                            if (paymentIntentClientSecret != null) {
                                paymentSheet.presentWithPaymentIntent(paymentIntentClientSecret,
                                        new PaymentSheet.Configuration("Tatiana", configuration));
                            } else {
                                Toast.makeText(MainActivity.this, "Failed to retrieve payment details", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(MainActivity.this, "Failed to parse response", Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Toast.makeText(MainActivity.this, "Failed to connect to server", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override

            // Override pentru metoda getParams pentru a specifica parametrii POST trimiși la server (authkey și totalAmount).
            protected Map<String, String> getParams() {
                Map<String, String> paramV = new HashMap<>();
                paramV.put("authkey", "tatianaKey");
                paramV.put("totalAmount", totalAmount);
                return paramV;
            }
        };
        queue.add(stringRequest);
}
}