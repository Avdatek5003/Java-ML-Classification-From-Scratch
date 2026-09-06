package data;

import model.UserRecord;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class DataYukle {

    public static ArrayList<UserRecord> veriYukle(String dosyaYolu) {

        ArrayList<UserRecord> veriListesi = new ArrayList<>();
        String satir;

        try (BufferedReader okuyucu = new BufferedReader(new FileReader(dosyaYolu))) {

            // CSV header satırını atla.
            okuyucu.readLine();

            while ((satir = okuyucu.readLine()) != null) {

                // Tırnak içerisindeki virgülleri bölmeden gerçek CSV kolonlarını ayır.
                String[] parcalar =
                        satir.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);

                if (parcalar.length < 18) {
                    continue;
                }

                try {

                    // Model için gerekli alanlardan biri eksikse kaydı atla.
                    if (parcalar[9].trim().isEmpty()
                            || parcalar[17].trim().isEmpty()
                            || parcalar[7].trim().isEmpty()
                            || parcalar[10].trim().isEmpty()
                            || parcalar[12].trim().isEmpty()) {

                        continue;
                    }

                    String clientCode =
                            parcalar[9].replace("\"", "").trim();

                    // Erkek = 1, Kadın = 0
                    String cinsiyetMetni =
                            parcalar[17].replace("\"", "").trim();

                    int gender =
                            cinsiyetMetni.equalsIgnoreCase("E") ? 1 : 0;

                    double lineNetTotal =
                            Double.parseDouble(
                                    parcalar[7]
                                            .replace("\"", "")
                                            .trim()
                            );

                    /*
                     * Brand kategorik bir özelliktir.
                     *
                     * Önceki sürümde markalar 600, 601, 602... gibi
                     * sayılara dönüştürülüyordu. Bu durum KNN için
                     * gerçekte var olmayan sayısal uzaklıklar oluşturuyordu.
                     *
                     * Bu nedenle marka artık String olarak korunur.
                     */
                    String brand =
                            parcalar[10].replace("\"", "").trim();

                    // Tahmin edilecek hedef sınıf.
                    String category =
                            parcalar[12].replace("\"", "").trim();

                    UserRecord kayit =
                            new UserRecord(
                                    clientCode,
                                    gender,
                                    lineNetTotal,
                                    brand,
                                    category
                            );

                    veriListesi.add(kayit);

                } catch (NumberFormatException e) {
                    // Sayısal formatı bozuk kayıtları atla.
                }
            }

        } catch (IOException e) {
            System.out.println(
                    "Dosya okunurken hata oluştu: " + e.getMessage()
            );

            e.printStackTrace();
        }

        return veriListesi;
    }
}