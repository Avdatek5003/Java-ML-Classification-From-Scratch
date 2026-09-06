package preprocess;

import model.UserRecord;
import java.util.ArrayList;

public class PreProcessor {
    
    // 
    // Min/Max sınırlarını hafızada tutuyoruz.
    public static double minHarcama = Double.MAX_VALUE;
    public static double maxHarcama = Double.MIN_VALUE;
    public static double minMarka = Double.MAX_VALUE;
    public static double maxMarka = Double.MIN_VALUE;

    
    // Eksik, boş olan satırları ayıklrız
    public static ArrayList<UserRecord> veriyiTemizle(ArrayList<UserRecord> veriListesi) {
        ArrayList<UserRecord> temizListe = new ArrayList<>();

        for (UserRecord u : veriListesi) {
            // Null veya boş metin kontrolü
            if (u.getClientCode() == null || u.getCategory() == null) continue;
            if (u.getClientCode().trim().isEmpty() || u.getCategory().trim().isEmpty()) continue;

            // Cinisyet kontrolü.0 ve 1 den başka deger kabul edilmez
            if (u.getGender() != 0 && u.getGender() != 1) continue;

            // Fiyat ve Marka kodu kontrolü
            if (u.getLineNetTotal() <= 0) continue;
            if (u.getBrandCode() < 0) continue; 

            // Temiz veriyi listeye ekleriz
            temizListe.add(u);
        }

        return temizListe;
    }

    //NORMALİZASYON kısmı
    public static void veriyiNormallestir(ArrayList<UserRecord> veriListesi) {

        if (veriListesi.isEmpty()) return;

        // Method çalıştığında eski min/max değerlerini sıfırlarız
        minHarcama = Double.MAX_VALUE;
        maxHarcama = Double.MIN_VALUE;
        minMarka = Double.MAX_VALUE;
        maxMarka = Double.MIN_VALUE;

        //  listeyi gezip en büyük ve en küçük değerleri tespit ederiz
        for (UserRecord u : veriListesi) {
            if (u.getLineNetTotal() < minHarcama) minHarcama = u.getLineNetTotal();
            if (u.getLineNetTotal() > maxHarcama) maxHarcama = u.getLineNetTotal();
            if (u.getBrandCode() < minMarka) minMarka = u.getBrandCode();
            if (u.getBrandCode() > maxMarka) maxMarka = u.getBrandCode();
        }

        //  Formül (x - min) / (max - min)
        for (UserRecord u : veriListesi) {
            
            // Fiyat için 0-1 dönüşümü yapar ve kaydederiz
            if (maxHarcama != minHarcama) {
                double normallestirilmisFiyat = (u.getLineNetTotal() - minHarcama) / (maxHarcama - minHarcama);
                u.setLineNetTotal(normallestirilmisFiyat);
            }

            // Marka Kodu için 0-1 dönüşümü yapar ve kaydederiz.
            if (maxMarka != minMarka) {
                double normallestirilmisMarka = (u.getBrandCode() - minMarka) / (maxMarka - minMarka);
                u.setBrandCode(normallestirilmisMarka); 
            }
        }
    }
}