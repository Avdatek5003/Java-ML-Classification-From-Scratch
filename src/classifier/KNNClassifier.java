package classifier;

import model.UserRecord;
import java.util.*;

public class KNNClassifier implements IClassifier {

    private List<UserRecord> egitimVerisi; // Geçmiş veriler
    private int k; // komşu sayısı

    public KNNClassifier(int k) {
        this.k = k;
    }

    @Override
    public void train(List<UserRecord> egitimVerisi) {
        // veriyi hafızaya alma
        this.egitimVerisi = egitimVerisi;
    }

    @Override
    public String predict(UserRecord musteri) {
        if (egitimVerisi == null || egitimVerisi.isEmpty()) {
            throw new IllegalStateException("Model eğitilmedi!");
        }

        // burada en yakın k kişiyi tutuyoruz
        PriorityQueue<Komsu> kuyruk = new PriorityQueue<>(
                Comparator.comparingDouble((Komsu n) -> n.mesafe).reversed()
        );

        // Mesafe hesabı
        for (UserRecord egitimMusterisi : egitimVerisi) {
            double mesafe = mesafeHesapla(musteri, egitimMusterisi);
            kuyruk.offer(new Komsu(mesafe, egitimMusterisi.getCategory()));

            // k(sepet) dolduysa en uzak kişi atılır
            if (kuyruk.size() > k) {
                kuyruk.poll(); 
            }
        }

        // kalan komsuların kategorisini saymak icin hashmap yapısı kuruyoruz
        Map<String, Integer> kategoriSayaci = new HashMap<>();
        
        for (Komsu komsu : kuyruk) {
            kategoriSayaci.put(komsu.kategori, kategoriSayaci.getOrDefault(komsu.kategori, 0) + 1);
        }

        String enIyiKategori = null;
        int maxOy = -1;

        // en yuksek oyu alan kategoriyi bulma
        for (Map.Entry<String, Integer> kayit : kategoriSayaci.entrySet()) {
            if (kayit.getValue() > maxOy) {
                maxOy = kayit.getValue();
                enIyiKategori = kayit.getKey();
            }
        }

        return enIyiKategori; // Kazananı döndür.
    }

    // ÖKlid ile mesafe hesabı yapıyoruz
    private double mesafeHesapla(UserRecord u1, UserRecord u2) {
        double farkCinsiyet = u1.getGender() - u2.getGender();
        double farkToplam = u1.getLineNetTotal() - u2.getLineNetTotal();
        double farkMarka = u1.getBrandCode() - u2.getBrandCode();

    
        return Math.sqrt(
                (farkCinsiyet * farkCinsiyet) +
                (farkToplam * farkToplam) +
                (farkMarka * farkMarka)
        );
    }

    // mesafe ve kategoriyi bi arada tutmamızı sağlayan class
    private static class Komsu {
        double mesafe;
        String kategori;

        public Komsu(double mesafe, String kategori) {
            this.mesafe = mesafe;
            this.kategori = kategori;
        }
    }
}