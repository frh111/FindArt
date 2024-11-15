package com.example.artshopv1;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// utilizată pentru afișarea imaginilor într-un GridView.
public class ImageAdapter extends BaseAdapter {

    private Context context;
    private List<String> imageUrls;
    private Map<String, String> imagePrices;

    // Constructorul clasei ImageAdapter.
    public ImageAdapter(Context context, List<String> imageUrls, Map<String, String> imagePrices) {
        this.context = context;
        this.imageUrls = imageUrls;
        this.imagePrices = imagePrices;
    }

    // Returnează numărul total de elemente din lista de imagini.
    @Override
    public int getCount() {
        return imageUrls.size();
    }

    // Returnează obiectul de la poziția specificată în lista de imagini.
    @Override
    public Object getItem(int position) {
        return imageUrls.get(position);
    }

    // Returnează id-ul elementului de la poziția specificată.
    @Override
    public long getItemId(int position) {
        return position;
    }

    // Metoda getView este responsabilă pentru crearea și returnarea unei View pentru fiecare element din GridView.
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        TextView textViewPrice;
        ViewHolder viewHolder;

            // Verificăm dacă vederea trebuie creată sau reutilizată
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.grid_item, parent, false);

            // Găsim și stocăm referințele la elementele de vedere în ViewHolder
            imageView = convertView.findViewById(R.id.imageView);
            textViewPrice = convertView.findViewById(R.id.textViewPrice);

            // Creăm un nou ViewHolder și stocăm referințele
            viewHolder = new ViewHolder(imageView, textViewPrice);

            // Setăm ViewHolder-ul ca tag pentru convertView
            convertView.setTag(viewHolder);
        } else {
            // Reutilizăm ViewHolder-ul existent
            viewHolder = (ViewHolder) convertView.getTag();

            // Obținem referințele la elementele de vedere din ViewHolder
            imageView = viewHolder.imageView;
            textViewPrice = viewHolder.textViewPrice;
        }

        // Obținem URL-ul imaginii și prețul pentru poziția curentă
        String imageUrl = imageUrls.get(position);
        String price = imagePrices.get(imageUrl);

        // Utilizează biblioteca Picasso pentru a încărca imaginea de la URL-ul specificat în ImageView.
        Picasso.get()
                .load(imageUrl)
                .error(R.drawable.logo) // Specifică o imagine placeholder în caz de eroare.
                .into(imageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        // Se afișează un mesaj de succes în cazul în care încărcarea imaginii reușește.
                        Log.d("Picasso", "Image loaded successfully");
                    }

                    @Override
                    public void onError(Exception e) {
                        // Se afișează un mesaj de eroare în cazul în care apare o eroare în timpul încărcării imaginii.
                        Log.e("Picasso", "Failed to load image: " + e.getMessage());
                    }
                });

        // Setează prețul în TextView
        textViewPrice.setText(price);

        return convertView;
    }

    // ViewHolder pattern pentru a stoca referințele către ImageView și TextView pentru fiecare element din GridView.
    static class ViewHolder {
        ImageView imageView;
        TextView textViewPrice;

        ViewHolder(ImageView imageView, TextView textViewPrice) {
            this.imageView = imageView;
            this.textViewPrice = textViewPrice;
        }
    }
}
