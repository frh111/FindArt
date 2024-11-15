package com.example.artshopv1;
public class PaymentService {

    // simulează o tranzacție de plată

    public static PaymentResult processPayment(double amount, String currency) {

        // Simulează un rezultat de plată de succes
        return new PaymentResult(true, null);
    }
}

// nu este publică, deci poate fi accesată doar din același pachet
class PaymentResult {
    private boolean success;  //Un boolean care indică dacă plata a fost efectuată cu succes.
    private String errorMessage; //  Un string care stochează mesajul de eroare, dacă există.

 // Constructorul clasei PaymentResult
    public PaymentResult(boolean success, String errorMessage) {
        this.success = success;
        this.errorMessage = errorMessage;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
