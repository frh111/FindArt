package com.example.artshopv1;
// Clasa ImageWithPrice este utilizată pentru a reprezenta o imagine împreună cu prețul său.

public class ImageWithPrice {
    private String img;
    private String price;

    public ImageWithPrice() {
        // Constructorul implicit necesar pentru apelurile către DataSnapshot.getValue(ImageWithPrice.class)
    }
    public ImageWithPrice(String img, String price) {
        this.img = img;
        this.price = price;  //instrucțiunile pentru a inițializa variabilele de instanță ale clasei.
    }

    //Constructorul care primește URL-ul și prețul imaginii și le atribuie membrilor corespunzători.

    public String getImg() {
        return img;
    }
    //Metoda de acces pentru obținerea URL-ului imaginii.

    public void setImg(String img) {
        this.img = img;
    }
    // Metoda de modificare pentru setarea URL-ului imaginii.

    public String getPrice() {
        return price;
    }
    // Metoda de acces pentru obținerea prețului imaginii.

    public void setPrice(String price) {
        this.price = price;
    }
    // Metoda de modificare pentru setarea prețului imaginii.
}
