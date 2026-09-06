package data;

import model.UserRecord;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class DataYukle {

    // Label encoding kısmı Burada markaları KNN de işlem yapabileceğimiz sayılara dönüstürüyoruz

    private static HashMap<String, Double> markaSozlugu = new HashMap<>();
    private static double markaSayaci = 600.0; // Atanacak ilk sayı kodu

    public static ArrayList<UserRecord> veriYukle(String dosyaYolu) {

        ArrayList<UserRecord> veriListesi = new ArrayList<>(); // Nesneleri tutacağımız liste
        String satir;

        // Try catch .dosya okuma sırasında olusabiecek hataları yakalayabilmek icin
        try (BufferedReader okuyucu = new BufferedReader(new FileReader(dosyaYolu))) {

            // ilk satırı atlıyoruz çünkü fiyat marka gibi başliklar var
            okuyucu.readLine();

            while ((satir = okuyucu.readLine()) != null) {

                // bu regex ile dışarıdaki virgüllere göre bölme yapar tırnak içindekilere dokunmaz 
                String[] parcalar = satir.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);

                //eger satırda yeterli sütün yoksa atla
                if (parcalar.length < 18) {
                    continue;
                }

                try {
                    // satırlardan biri bile boşsa onu atla
                    if (parcalar[9].isEmpty() || parcalar[17].isEmpty() || parcalar[7].isEmpty() || 
                        parcalar[10].isEmpty() || parcalar[12].isEmpty()) {
                        continue; 
                    }

                   
                    
                    // trim ile boşlukları temizilyoruz
                    String clientCode = parcalar[9].replace("\"", "").trim();
                    
                    // Cinsiyeti encoding yapıyoruz.erkek ise 1 kadınsa 0 
                    String cinsiyetMetni = parcalar[17].replace("\"", "").trim();
                    int gender = cinsiyetMetni.equalsIgnoreCase("E") ? 1 : 0;

                    // fiyatı string türünden double 'a ceviriyoruz
                    double lineNetTotal = Double.parseDouble(parcalar[7].replace("\"", "").trim());

                    // Marka yı encoding ediyoruz.
                    String markaMetni = parcalar[10].replace("\"", "").trim();
                    double brandCode;
                    
                    try {
                        //Marka kodunu sayıya çevirme
                        brandCode = Double.parseDouble(markaMetni);
                    } catch (NumberFormatException ex) {
                        // sayıya ceviremediysek yeni bir sayı vererek ekle
                        if (!markaSozlugu.containsKey(markaMetni)) {
                            markaSozlugu.put(markaMetni, markaSayaci++); 
                        }
                        brandCode = markaSozlugu.get(markaMetni); 
                    }

                    // tahmin edilecek ana kategori 12.sütunda .burayı hedef alıyoruz 
                    String category = parcalar[12].replace("\"", "").trim();

                    // TEMİZLENMİŞ VERİLERLE NESNE OLUŞTUR
                    UserRecord kayit = new UserRecord(
                            clientCode,
                            gender,
                            lineNetTotal,
                            brandCode,
                            category
                    );

                    // Nesneyi ana listemize ekle.
                    veriListesi.add(kayit);

                } catch (NumberFormatException e) {
                    // bozuk veri tiplerini program çökmemesi için catch bloğu ile yakalarız
                }
            }

        } catch (IOException e) {
            System.out.println("Dosya okunurken hata oluştu: " + e.getMessage());
            e.printStackTrace();
        }

        return veriListesi; // Tüm verilerin nesneye dönüşmüş halini geri yolla
    }
}