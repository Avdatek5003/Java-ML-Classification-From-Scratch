package classifier;

import model.UserRecord;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class KNNClassifier implements IClassifier {

    private List<UserRecord> egitimVerisi;
    private int k;

    public KNNClassifier(int k) {

        if (k <= 0) {
            throw new IllegalArgumentException(
                    "K değeri 0'dan büyük olmalıdır."
            );
        }

        this.k = k;
    }

    @Override
    public void train(List<UserRecord> egitimVerisi) {

        if (egitimVerisi == null || egitimVerisi.isEmpty()) {
            throw new IllegalArgumentException(
                    "Eğitim verisi boş olamaz."
            );
        }

        this.egitimVerisi = egitimVerisi;
    }

    @Override
    public String predict(UserRecord musteri) {

        if (egitimVerisi == null || egitimVerisi.isEmpty()) {
            throw new IllegalStateException(
                    "Model eğitilmedi!"
            );
        }

        /*
         * PriorityQueue içerisinde en yakın K komşuyu tutuyoruz.
         *
         * Kuyruğun başında seçilen komşular içerisindeki en uzak
         * kayıt bulunur. K kapasitesi aşılırsa bu kayıt çıkarılır.
         */
        PriorityQueue<Komsu> kuyruk =
                new PriorityQueue<>(
                        Comparator
                                .comparingDouble(
                                        (Komsu n) -> n.mesafe
                                )
                                .reversed()
                );

        for (UserRecord egitimMusterisi : egitimVerisi) {

            double mesafe =
                    mesafeHesapla(
                            musteri,
                            egitimMusterisi
                    );

            kuyruk.offer(
                    new Komsu(
                            mesafe,
                            egitimMusterisi.getCategory()
                    )
            );

            if (kuyruk.size() > k) {
                kuyruk.poll();
            }
        }

        Map<String, Integer> kategoriSayaci =
                new HashMap<>();

        Map<String, Double> kategoriMesafeToplami =
                new HashMap<>();

        for (Komsu komsu : kuyruk) {

            kategoriSayaci.put(
                    komsu.kategori,
                    kategoriSayaci.getOrDefault(
                            komsu.kategori,
                            0
                    ) + 1
            );

            kategoriMesafeToplami.put(
                    komsu.kategori,
                    kategoriMesafeToplami.getOrDefault(
                            komsu.kategori,
                            0.0
                    ) + komsu.mesafe
            );
        }

        String enIyiKategori = null;
        int enYuksekOy = -1;
        double enDusukMesafe = Double.MAX_VALUE;

        /*
         * Önce çoğunluk oyuna bakılır.
         *
         * Eşitlik varsa toplam komşu mesafesi daha düşük olan
         * kategori tercih edilir.
         */
        for (Map.Entry<String, Integer> kayit
                : kategoriSayaci.entrySet()) {

            String kategori = kayit.getKey();
            int oy = kayit.getValue();

            double toplamMesafe =
                    kategoriMesafeToplami.get(kategori);

            if (oy > enYuksekOy
                    || (oy == enYuksekOy
                    && toplamMesafe < enDusukMesafe)
                    || (oy == enYuksekOy
                    && toplamMesafe == enDusukMesafe
                    && (enIyiKategori == null
                    || kategori.compareTo(enIyiKategori) < 0))) {

                enYuksekOy = oy;
                enDusukMesafe = toplamMesafe;
                enIyiKategori = kategori;
            }
        }

        return enIyiKategori;
    }

    /*
     * Karma özellikler için basit bir distance yaklaşımı:
     *
     * Gender:
     *     aynıysa 0, farklıysa 1
     *
     * Spending:
     *     training-set min/max ile 0-1 normalize edilmiş
     *     sayısal mesafe
     *
     * Brand:
     *     aynı marka = 0
     *     farklı marka = 1
     *
     * Böylece marka isimlerine yapay sayısal uzaklık verilmez.
     */
    private double mesafeHesapla(
            UserRecord u1,
            UserRecord u2
    ) {

        double farkCinsiyet =
                u1.getGender() == u2.getGender()
                        ? 0.0
                        : 1.0;

        double farkHarcama =
                u1.getLineNetTotal()
                        - u2.getLineNetTotal();

        double markaUyusmazligi =
                u1.getBrand()
                        .equalsIgnoreCase(u2.getBrand())
                        ? 0.0
                        : 1.0;

        return Math.sqrt(
                farkCinsiyet * farkCinsiyet
                        +
                farkHarcama * farkHarcama
                        +
                markaUyusmazligi * markaUyusmazligi
        );
    }

    private static class Komsu {

        double mesafe;
        String kategori;

        public Komsu(
                double mesafe,
                String kategori
        ) {
            this.mesafe = mesafe;
            this.kategori = kategori;
        }
    }
}