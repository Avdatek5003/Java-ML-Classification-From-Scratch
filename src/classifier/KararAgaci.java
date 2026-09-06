package classifier;

import model.UserRecord;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class KararAgaci implements IClassifier {

    private int maksimumDerinlik;
    private Dugum kok;

    public KararAgaci(int maksimumDerinlik) {

        if (maksimumDerinlik <= 0) {
            throw new IllegalArgumentException(
                    "Maksimum derinlik 0'dan büyük olmalıdır."
            );
        }

        this.maksimumDerinlik = maksimumDerinlik;
    }

    @Override
    public void train(List<UserRecord> egitimVerisi) {

        if (egitimVerisi == null || egitimVerisi.isEmpty()) {
            throw new IllegalArgumentException(
                    "Eğitim verisi boş olamaz."
            );
        }

        this.kok = agacInsaEt(
                egitimVerisi,
                0
        );
    }

    @Override
    public String predict(UserRecord musteri) {

        if (kok == null) {
            throw new IllegalStateException(
                    "Model henüz eğitilmedi!"
            );
        }

        return ozyinelemeliTahminEt(
                musteri,
                kok
        );
    }

    private Dugum agacInsaEt(
            List<UserRecord> veriListesi,
            int mevcutDerinlik
    ) {

        if (veriListesi.isEmpty()) {
            return null;
        }

        if (mevcutDerinlik >= maksimumDerinlik) {

            return new Dugum(
                    enCokTekrarEdenKategoriyiAl(
                            veriListesi
                    )
            );
        }

        String ilkKategori =
                veriListesi.get(0).getCategory();

        boolean hepsiAyniMi =
                veriListesi.stream()
                        .allMatch(
                                u -> u.getCategory()
                                        .equals(ilkKategori)
                        );

        if (hepsiAyniMi) {
            return new Dugum(ilkKategori);
        }

        EnIyiBolunme enIyiBolunme =
                enIyiBolunmeyiBul(veriListesi);

        if (enIyiBolunme == null
                || enIyiBolunme.solGrup.isEmpty()
                || enIyiBolunme.sagGrup.isEmpty()) {

            return new Dugum(
                    enCokTekrarEdenKategoriyiAl(
                            veriListesi
                    )
            );
        }

        Dugum solCocuk =
                agacInsaEt(
                        enIyiBolunme.solGrup,
                        mevcutDerinlik + 1
                );

        Dugum sagCocuk =
                agacInsaEt(
                        enIyiBolunme.sagGrup,
                        mevcutDerinlik + 1
                );

        if (enIyiBolunme.kategorikMi) {

            return new Dugum(
                    enIyiBolunme.ozellikIndeksi,
                    enIyiBolunme.kategoriDegeri,
                    solCocuk,
                    sagCocuk
            );
        }

        return new Dugum(
                enIyiBolunme.ozellikIndeksi,
                enIyiBolunme.esikDegeri,
                solCocuk,
                sagCocuk
        );
    }

    private String ozyinelemeliTahminEt(
            UserRecord musteri,
            Dugum dugum
    ) {

        if (dugum.yaprakMi) {
            return dugum.kategori;
        }

        if (dugum.kategorikMi) {

            boolean ayniKategori =
                    musteri.getBrand()
                            .equalsIgnoreCase(
                                    dugum.kategoriDegeri
                            );

            if (ayniKategori) {
                return ozyinelemeliTahminEt(
                        musteri,
                        dugum.solDal
                );
            }

            return ozyinelemeliTahminEt(
                    musteri,
                    dugum.sagDal
            );
        }

        double deger =
                sayisalOzellikDegeriniAl(
                        musteri,
                        dugum.ozellikIndeksi
                );

        if (deger <= dugum.esikDegeri) {

            return ozyinelemeliTahminEt(
                    musteri,
                    dugum.solDal
            );
        }

        return ozyinelemeliTahminEt(
                musteri,
                dugum.sagDal
        );
    }

    private EnIyiBolunme enIyiBolunmeyiBul(
            List<UserRecord> veriListesi
    ) {

        double enIyiGini =
                Double.MAX_VALUE;

        EnIyiBolunme enIyiBolunmeSecimi =
                null;

        /*
         * Numeric özellikler:
         *
         * 0 -> Gender
         * 1 -> Normalized spending
         */
        for (int ozellikIndeksi = 0;
             ozellikIndeksi < 2;
             ozellikIndeksi++) {

            List<Double> esikDegerleri =
                    sayisalEsikleriOlustur(
                            veriListesi,
                            ozellikIndeksi
                    );

            for (double esikDeger
                    : esikDegerleri) {

                List<UserRecord> solGrup =
                        new ArrayList<>();

                List<UserRecord> sagGrup =
                        new ArrayList<>();

                for (UserRecord u : veriListesi) {

                    if (sayisalOzellikDegeriniAl(
                            u,
                            ozellikIndeksi
                    ) <= esikDeger) {

                        solGrup.add(u);

                    } else {

                        sagGrup.add(u);
                    }
                }

                if (solGrup.isEmpty()
                        || sagGrup.isEmpty()) {
                    continue;
                }

                double agirlikliGini =
                        agirlikliGiniHesapla(
                                solGrup,
                                sagGrup,
                                veriListesi.size()
                        );

                if (agirlikliGini < enIyiGini) {

                    enIyiGini =
                            agirlikliGini;

                    enIyiBolunmeSecimi =
                            EnIyiBolunme
                                    .sayisalBolunme(
                                            ozellikIndeksi,
                                            esikDeger,
                                            solGrup,
                                            sagGrup
                                    );
                }
            }
        }

        /*
         * Brand kategorik bir özelliktir.
         *
         * Her marka için:
         *
         *     brand == X
         *
         * ve
         *
         *     brand != X
         *
         * şeklinde aday bölünmeler değerlendirilir.
         */
        Set<String> markalar = new TreeSet<>();

        for (UserRecord u : veriListesi) {
            markalar.add(u.getBrand());
        }

        for (String marka : markalar) {

            List<UserRecord> solGrup =
                    new ArrayList<>();

            List<UserRecord> sagGrup =
                    new ArrayList<>();

            for (UserRecord u : veriListesi) {

                if (u.getBrand()
                        .equalsIgnoreCase(marka)) {

                    solGrup.add(u);

                } else {

                    sagGrup.add(u);
                }
            }

            if (solGrup.isEmpty()
                    || sagGrup.isEmpty()) {
                continue;
            }

            double agirlikliGini =
                    agirlikliGiniHesapla(
                            solGrup,
                            sagGrup,
                            veriListesi.size()
                    );

            if (agirlikliGini < enIyiGini) {

                enIyiGini =
                        agirlikliGini;

                enIyiBolunmeSecimi =
                        EnIyiBolunme
                                .kategorikBolunme(
                                        2,
                                        marka,
                                        solGrup,
                                        sagGrup
                                );
            }
        }

        return enIyiBolunmeSecimi;
    }

    /*
     * Sürekli numeric özelliklerde iki komşu benzersiz değer
     * arasındaki orta noktayı threshold olarak kullanıyoruz.
     */
    private List<Double> sayisalEsikleriOlustur(
            List<UserRecord> veriListesi,
            int ozellikIndeksi
    ) {

        TreeSet<Double> benzersizDegerler =
                new TreeSet<>();

        for (UserRecord u : veriListesi) {

            benzersizDegerler.add(
                    sayisalOzellikDegeriniAl(
                            u,
                            ozellikIndeksi
                    )
            );
        }

        List<Double> degerler =
                new ArrayList<>(
                        benzersizDegerler
                );

        List<Double> esikler =
                new ArrayList<>();

        for (int i = 0;
             i < degerler.size() - 1;
             i++) {

            double esik =
                    (
                            degerler.get(i)
                            +
                            degerler.get(i + 1)
                    ) / 2.0;

            esikler.add(esik);
        }

        return esikler;
    }

    private double agirlikliGiniHesapla(
            List<UserRecord> solGrup,
            List<UserRecord> sagGrup,
            int toplamBoyut
    ) {

        double solGini =
                giniHesapla(solGrup);

        double sagGini =
                giniHesapla(sagGrup);

        return (
                solGrup.size() * solGini
                +
                sagGrup.size() * sagGini
        ) / toplamBoyut;
    }

    private double giniHesapla(
            List<UserRecord> veriListesi
    ) {

        Map<String, Integer> sayaclar =
                new HashMap<>();

        for (UserRecord u : veriListesi) {

            sayaclar.put(
                    u.getCategory(),
                    sayaclar.getOrDefault(
                            u.getCategory(),
                            0
                    ) + 1
            );
        }

        double safsizlik =
                1.0;

        for (int sayi : sayaclar.values()) {

            double olasilik =
                    (double) sayi
                    / veriListesi.size();

            safsizlik -=
                    olasilik * olasilik;
        }

        return safsizlik;
    }

    private String enCokTekrarEdenKategoriyiAl(
            List<UserRecord> veriListesi
    ) {

        Map<String, Integer> sayaclar =
                new HashMap<>();

        for (UserRecord u : veriListesi) {

            sayaclar.put(
                    u.getCategory(),
                    sayaclar.getOrDefault(
                            u.getCategory(),
                            0
                    ) + 1
            );
        }

        return Collections.max(
                sayaclar.entrySet(),
                Map.Entry.comparingByValue()
        ).getKey();
    }

    private double sayisalOzellikDegeriniAl(
            UserRecord u,
            int ozellikIndeksi
    ) {

        switch (ozellikIndeksi) {

            case 0:
                return u.getGender();

            case 1:
                return u.getLineNetTotal();

            default:
                throw new IllegalArgumentException(
                        "Geçersiz sayısal özellik indeksi"
                );
        }
    }

    private static class Dugum {

        boolean yaprakMi;
        String kategori;

        int ozellikIndeksi;

        double esikDegeri;

        boolean kategorikMi;
        String kategoriDegeri;

        Dugum solDal;
        Dugum sagDal;

        // Leaf node
        public Dugum(String kategori) {

            this.yaprakMi = true;
            this.kategori = kategori;
        }

        // Numeric split node
        public Dugum(
                int ozellikIndeksi,
                double esikDegeri,
                Dugum solDal,
                Dugum sagDal
        ) {

            this.yaprakMi = false;
            this.kategorikMi = false;

            this.ozellikIndeksi =
                    ozellikIndeksi;

            this.esikDegeri =
                    esikDegeri;

            this.solDal =
                    solDal;

            this.sagDal =
                    sagDal;
        }

        // Categorical split node
        public Dugum(
                int ozellikIndeksi,
                String kategoriDegeri,
                Dugum solDal,
                Dugum sagDal
        ) {

            this.yaprakMi = false;
            this.kategorikMi = true;

            this.ozellikIndeksi =
                    ozellikIndeksi;

            this.kategoriDegeri =
                    kategoriDegeri;

            this.solDal =
                    solDal;

            this.sagDal =
                    sagDal;
        }
    }

    private static class EnIyiBolunme {

        int ozellikIndeksi;

        double esikDegeri;

        boolean kategorikMi;
        String kategoriDegeri;

        List<UserRecord> solGrup;
        List<UserRecord> sagGrup;

        private EnIyiBolunme() {
        }

        static EnIyiBolunme sayisalBolunme(
                int ozellikIndeksi,
                double esikDegeri,
                List<UserRecord> solGrup,
                List<UserRecord> sagGrup
        ) {

            EnIyiBolunme sonuc =
                    new EnIyiBolunme();

            sonuc.ozellikIndeksi =
                    ozellikIndeksi;

            sonuc.esikDegeri =
                    esikDegeri;

            sonuc.kategorikMi =
                    false;

            sonuc.solGrup =
                    solGrup;

            sonuc.sagGrup =
                    sagGrup;

            return sonuc;
        }

        static EnIyiBolunme kategorikBolunme(
                int ozellikIndeksi,
                String kategoriDegeri,
                List<UserRecord> solGrup,
                List<UserRecord> sagGrup
        ) {

            EnIyiBolunme sonuc =
                    new EnIyiBolunme();

            sonuc.ozellikIndeksi =
                    ozellikIndeksi;

            sonuc.kategorikMi =
                    true;

            sonuc.kategoriDegeri =
                    kategoriDegeri;

            sonuc.solGrup =
                    solGrup;

            sonuc.sagGrup =
                    sagGrup;

            return sonuc;
        }
    }
}