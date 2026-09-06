package evaluation;

import classifier.IClassifier;
import model.UserRecord;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Evaluator {

    public static double dogrulukHesapla(
            IClassifier model,
            List<UserRecord> testVerisi
    ) {

        if (testVerisi == null || testVerisi.isEmpty()) {
            return 0.0;
        }

        int dogruSayisi = 0;

        for (UserRecord musteri : testVerisi) {

            String tahminEdilen = model.predict(musteri);
            String gercek = musteri.getCategory();

            if (tahminEdilen != null
                    && tahminEdilen.equals(gercek)) {

                dogruSayisi++;
            }
        }

        return (double) dogruSayisi / testVerisi.size();
    }

    /*
     * Confusion matrix içerisindeki hem gerçek hem tahmin edilen
     * bütün sınıfları bulur.
     */
    private static Set<String> tumKategorileriBul(
            Map<String, Map<String, Integer>> matris
    ) {

        Set<String> kategoriler = new HashSet<>();

        kategoriler.addAll(matris.keySet());

        for (Map<String, Integer> tahminler : matris.values()) {
            kategoriler.addAll(tahminler.keySet());
        }

        kategoriler.remove(null);

        return kategoriler;
    }

    public static double macroPrecision(
            Map<String, Map<String, Integer>> matris
    ) {

        Set<String> kategoriler =
                tumKategorileriBul(matris);

        if (kategoriler.isEmpty()) {
            return 0.0;
        }

        double toplamPrecision = 0.0;

        for (String kategori : kategoriler) {

            int tp = getCount(
                    matris,
                    kategori,
                    kategori
            );

            int fp = 0;

            for (String gercek : kategoriler) {

                if (!gercek.equals(kategori)) {

                    fp += getCount(
                            matris,
                            gercek,
                            kategori
                    );
                }
            }

            double precision =
                    (tp + fp == 0)
                            ? 0.0
                            : (double) tp / (tp + fp);

            toplamPrecision += precision;
        }

        return toplamPrecision / kategoriler.size();
    }

    public static double macroRecall(
            Map<String, Map<String, Integer>> matris
    ) {

        Set<String> kategoriler =
                tumKategorileriBul(matris);

        if (kategoriler.isEmpty()) {
            return 0.0;
        }

        double toplamRecall = 0.0;

        for (String kategori : kategoriler) {

            int tp = getCount(
                    matris,
                    kategori,
                    kategori
            );

            int fn = 0;

            for (String tahmin : kategoriler) {

                if (!tahmin.equals(kategori)) {

                    fn += getCount(
                            matris,
                            kategori,
                            tahmin
                    );
                }
            }

            double recall =
                    (tp + fn == 0)
                            ? 0.0
                            : (double) tp / (tp + fn);

            toplamRecall += recall;
        }

        return toplamRecall / kategoriler.size();
    }

    public static double macroF1(
            Map<String, Map<String, Integer>> matris
    ) {

        Set<String> kategoriler =
                tumKategorileriBul(matris);

        if (kategoriler.isEmpty()) {
            return 0.0;
        }

        double toplamF1 = 0.0;

        for (String kategori : kategoriler) {

            int tp = getCount(
                    matris,
                    kategori,
                    kategori
            );

            int fp = 0;
            int fn = 0;

            for (String digerKategori : kategoriler) {

                if (!digerKategori.equals(kategori)) {

                    fp += getCount(
                            matris,
                            digerKategori,
                            kategori
                    );

                    fn += getCount(
                            matris,
                            kategori,
                            digerKategori
                    );
                }
            }

            double precision =
                    (tp + fp == 0)
                            ? 0.0
                            : (double) tp / (tp + fp);

            double recall =
                    (tp + fn == 0)
                            ? 0.0
                            : (double) tp / (tp + fn);

            double f1 =
                    (precision + recall == 0.0)
                            ? 0.0
                            : 2.0 * precision * recall
                            / (precision + recall);

            toplamF1 += f1;
        }

        return toplamF1 / kategoriler.size();
    }

    private static int getCount(
            Map<String, Map<String, Integer>> matris,
            String gercek,
            String tahmin
    ) {

        Map<String, Integer> satir =
                matris.get(gercek);

        if (satir == null) {
            return 0;
        }

        return satir.getOrDefault(
                tahmin,
                0
        );
    }
}