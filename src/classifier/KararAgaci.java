package classifier;

import model.UserRecord;
import java.util.*;

public class KararAgaci implements IClassifier {

    // Ağacın sonsuza kadar büyümesini engelleyen üst sınır
    private int maksimumDerinlik; 
    // Kök dügüm
    private Dugum kok;

    public KararAgaci(int maksimumDerinlik) {
        this.maksimumDerinlik = maksimumDerinlik;
    }

    @Override
    public void train(List<UserRecord> egitimVerisi) {
        // Algoritmanın ögrenme aşaması
        this.kok = agacInsaEt(egitimVerisi, 0);
    }

    @Override
    public String predict(UserRecord musteri) {
        if (kok == null) throw new IllegalStateException("Model henüz eğitilmedi!");
        // yeni musteri geldiginde sorulan sorulara gore aşagı dogru ilerleriz
        return ozyinelemeliTahminEt(musteri, kok);
    }

    // Ağac inşa etmeye baslıyoruz
    private Dugum agacInsaEt(List<UserRecord> veriListesi, int mevcutDerinlik) {
       
        if (veriListesi.isEmpty()) return null;//  Veri kalmadıysa null dön
        if (mevcutDerinlik >= maksimumDerinlik) { // maxdepth ulaştıgımzda dallanmayı durdurur ve en cok tekrar edeni sonuc yaparız
            return new Dugum(enCokTekrarEdenKategoriyiAl(veriListesi)); 
        }

        // daldaki tum müsteriler zaten aynı kategoriyse sonucu döndürüruz
        String ilkKategori = veriListesi.get(0).getCategory();
        boolean hepsiAyniMi = veriListesi.stream().allMatch(u -> u.getCategory().equals(ilkKategori));
        if (hepsiAyniMi) {
            return new Dugum(ilkKategori);
        }

        //En iyi bolunmeyi bulma
        EnIyiBolunme enIyiBolunme = enIyiBolunmeyiBul(veriListesi);

        // iyi bi bolunme yoksa dallanmayıp yaprak dügüm yaparız
        if (enIyiBolunme == null || enIyiBolunme.solGrup.isEmpty() || enIyiBolunme.sagGrup.isEmpty()) {
            return new Dugum(enCokTekrarEdenKategoriyiAl(veriListesi));
        }

        // sol ve sag agacı kendi içinde aynı işlemlere uygular ve recursive bi yapı kurarız
        Dugum solCocuk = agacInsaEt(enIyiBolunme.solGrup, mevcutDerinlik + 1);
        Dugum sagCocuk = agacInsaEt(enIyiBolunme.sagGrup, mevcutDerinlik + 1);

        // Karar dugumu olusturup döndürme kısmı(hangi ozellik hangi eşik degere göre)
        return new Dugum(enIyiBolunme.ozellikIndeksi, enIyiBolunme.esikDegeri, solCocuk, sagCocuk);
    }

    //Tahmin kısmı
    private String ozyinelemeliTahminEt(UserRecord musteri, Dugum dugum) {
        // yaprak dügüme ulastıysak kategoriyi tahmin ederiz
        if (dugum.yaprakMi) return dugum.kategori;

        //Dügümdeki kurala gore sol veya sag daldan devam ederiz
        double deger = ozellikDegeriniAl(musteri, dugum.ozellikIndeksi);
        if (deger <= dugum.esikDegeri) {
            return ozyinelemeliTahminEt(musteri, dugum.solDal);
        } else {
            return ozyinelemeliTahminEt(musteri, dugum.sagDal);
        }
    }

    // Gini ile en iyi soryu bulma
    private EnIyiBolunme enIyiBolunmeyiBul(List<UserRecord> veriListesi) {
        double enIyiGini = Double.MAX_VALUE; // Gini ye max deger veriyoruz ki en kücük olanı bulalım
        EnIyiBolunme enIyiBolunmeSecimi = null;

        // 3 Özellik var 3kez kontrol edeceğiz
        for (int ozellikIndeksi = 0; ozellikIndeksi < 3; ozellikIndeksi++) {
            
            // Özellik değerlerini listeye alırız
            Set<Double> esikDegerleri = new HashSet<>();
            for (UserRecord u : veriListesi) {
                esikDegerleri.add(ozellikDegeriniAl(u, ozellikIndeksi));
            }

            // Her threshold degerine gore veriyi 2ye böleriz
            for (double esikDeger : esikDegerleri) {
                List<UserRecord> solGrup = new ArrayList<>();
                List<UserRecord> sagGrup = new ArrayList<>();

                for (UserRecord u : veriListesi) {
                    if (ozellikDegeriniAl(u, ozellikIndeksi) <= esikDeger) solGrup.add(u);
                    else sagGrup.add(u);
                }

                if (solGrup.isEmpty() || sagGrup.isEmpty()) continue;

                // Bölmenin safligini ölçme
                double solGini = giniHesapla(solGrup);
                double sagGini = giniHesapla(sagGrup);
                
                //Ortlama Gini degerini bulma
                double agirlikliGini = (solGrup.size() * solGini + sagGrup.size() * sagGini) / veriListesi.size();

                // en kucuk gini değeri buysa kaydet
                if (agirlikliGini < enIyiGini) {
                    enIyiGini = agirlikliGini;
                    enIyiBolunmeSecimi = new EnIyiBolunme(ozellikIndeksi, esikDeger, solGrup, sagGrup);
                }
            }
        }
        return enIyiBolunmeSecimi;
    }

    // Gini safsızlık hesabı
    private double giniHesapla(List<UserRecord> veriListesi) {
        Map<String, Integer> sayaclar = new HashMap<>();
        
        for (UserRecord u : veriListesi) {
            sayaclar.put(u.getCategory(), sayaclar.getOrDefault(u.getCategory(), 0) + 1);
        }

        double safsizlik = 1.0;
        for (int sayi : sayaclar.values()) {
            double olasilik = (double) sayi / veriListesi.size(); 
            safsizlik -= (olasilik * olasilik);
        }
        return safsizlik;
    }

    // yaprakta birden fazla kategori kaldıysa en cok tekrar edeni seçeriz
    private String enCokTekrarEdenKategoriyiAl(List<UserRecord> veriListesi) {
        Map<String, Integer> sayaclar = new HashMap<>();
        for (UserRecord u : veriListesi) {
            sayaclar.put(u.getCategory(), sayaclar.getOrDefault(u.getCategory(), 0) + 1);
        }
        // Map içindeki en yuksek degere sahip kategoriyi döndürürüz
        return Collections.max(sayaclar.entrySet(), Map.Entry.comparingByValue()).getKey();
    }

    // index e göre hangi kategoriye bakacagımızı seceriz
    private double ozellikDegeriniAl(UserRecord u, int ozellikIndeksi) {
        switch (ozellikIndeksi) {
            case 0: return u.getGender();
            case 1: return u.getLineNetTotal();
            case 2: return u.getBrandCode();
            default: throw new IllegalArgumentException("Geçersiz özellik indeksi");
        }
    }

    
   
    private static class Dugum {
        boolean yaprakMi;//Dugum sonuc mu yaprak mı
        String kategori; // tahmin sonucu
        int ozellikIndeksi; // hangi ozellik
        double esikDegeri; // sorudaki eşik deger
        Dugum solDal; // sol alt dal
        Dugum sagDal; // sag  alt dal

        // yaprak dügümü icin yapıcı method olusturyoruz
        public Dugum(String kategori) {
            this.yaprakMi = true;
            this.kategori = kategori;
        }

        // Karar Düğümü için Constructor
        public Dugum(int ozellikIndeksi, double esikDegeri, Dugum solDal, Dugum sagDal) {
            this.yaprakMi = false;
            this.ozellikIndeksi = ozellikIndeksi;
            this.esikDegeri = esikDegeri;
            this.solDal = solDal;
            this.sagDal = sagDal;
        }
    }

    //  verileri geçici olarak tuttuğumuz taşıyıcı sınıf
    private static class EnIyiBolunme {
        int ozellikIndeksi;
        double esikDegeri;
        List<UserRecord> solGrup;
        List<UserRecord> sagGrup;

        public EnIyiBolunme(int ozellikIndeksi, double esikDegeri, List<UserRecord> solGrup, List<UserRecord> sagGrup) {
            this.ozellikIndeksi = ozellikIndeksi;
            this.esikDegeri = esikDegeri;
            this.solGrup = solGrup;
            this.sagGrup = sagGrup;
        }
    }
}