package preprocess;

import model.UserRecord;

import java.util.ArrayList;
import java.util.List;

public class PreProcessor {

    private double minHarcama;
    private double maxHarcama;
    private boolean fitted = false;

    /*
     * Dataset seviyesinde temel veri temizleme.
     *
     * Bu aşama hedef veya test istatistiklerini kullanmadığı için
     * train/test split öncesinde uygulanabilir.
     */
    public static ArrayList<UserRecord> veriyiTemizle(
            ArrayList<UserRecord> veriListesi
    ) {

        ArrayList<UserRecord> temizListe = new ArrayList<>();

        for (UserRecord u : veriListesi) {

            if (u.getClientCode() == null
                    || u.getCategory() == null
                    || u.getBrand() == null) {
                continue;
            }

            if (u.getClientCode().trim().isEmpty()
                    || u.getCategory().trim().isEmpty()
                    || u.getBrand().trim().isEmpty()) {
                continue;
            }

            if (u.getGender() != 0 && u.getGender() != 1) {
                continue;
            }

            if (u.getLineNetTotal() <= 0) {
                continue;
            }

            temizListe.add(u);
        }

        return temizListe;
    }

    /*
     * Normalizasyon parametrelerini SADECE eğitim verisinden öğrenir.
     *
     * Böylece test setinin min/max bilgileri preprocessing aşamasına
     * sızmaz ve data leakage engellenir.
     */
    public void fit(List<UserRecord> egitimVerisi) {

        if (egitimVerisi == null || egitimVerisi.isEmpty()) {
            throw new IllegalArgumentException(
                    "PreProcessor boş eğitim verisiyle fit edilemez."
            );
        }

        minHarcama = Double.MAX_VALUE;
        maxHarcama = -Double.MAX_VALUE;

        for (UserRecord u : egitimVerisi) {

            if (u.getLineNetTotal() < minHarcama) {
                minHarcama = u.getLineNetTotal();
            }

            if (u.getLineNetTotal() > maxHarcama) {
                maxHarcama = u.getLineNetTotal();
            }
        }

        fitted = true;
    }

    /*
     * Daha önce eğitim setinden öğrenilmiş min/max değerlerini kullanarak
     * verilen listeyi dönüştürür.
     *
     * Orijinal nesneleri değiştirmek yerine yeni UserRecord nesneleri
     * oluşturulur. Böylece tekrar model çalıştırıldığında veri ikinci kez
     * normalize edilmez.
     */
    public ArrayList<UserRecord> transform(
            List<UserRecord> veriListesi
    ) {

        if (!fitted) {
            throw new IllegalStateException(
                    "PreProcessor önce eğitim verisiyle fit edilmelidir."
            );
        }

        ArrayList<UserRecord> sonuc = new ArrayList<>();

        for (UserRecord u : veriListesi) {

            double normalizeHarcama =
                    harcamayiNormallestir(u.getLineNetTotal());

            sonuc.add(
                    new UserRecord(
                            u.getClientCode(),
                            u.getGender(),
                            normalizeHarcama,
                            u.getBrand(),
                            u.getCategory()
                    )
            );
        }

        return sonuc;
    }

    /*
     * Manuel tahminde girilen ham harcama değerini de eğitim setinde
     * öğrenilen min/max değerleri ile normalize eder.
     */
    public double harcamayiNormallestir(double hamHarcama) {

        if (!fitted) {
            throw new IllegalStateException(
                    "PreProcessor henüz fit edilmedi."
            );
        }

        if (maxHarcama == minHarcama) {
            return 0.0;
        }

        return (hamHarcama - minHarcama)
                / (maxHarcama - minHarcama);
    }

    public double getMinHarcama() {
        return minHarcama;
    }

    public double getMaxHarcama() {
        return maxHarcama;
    }
}