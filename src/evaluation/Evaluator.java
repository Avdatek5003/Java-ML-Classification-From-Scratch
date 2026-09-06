package evaluation;

import classifier.IClassifier;
import model.UserRecord;
import java.util.List;

public class Evaluator {

    // Modelin ne kadar basarılı tahmin yaotığını hesaplarız 
    public static double dogrulukHesapla(IClassifier model, List<UserRecord> testVerisi) {

        int dogruSayisi = 0; // Doğru tahmin sayacı

        // Test listesindeki her bir üşteri için tahminde bulunma
        for (UserRecord musteri : testVerisi) {

            // 1. Modelin Tahmini 
            String tahminEdilen = model.predict(musteri);
            
            // 2. Gerçek Sonuç
            String gercek = musteri.getCategory();

            // Tahmin ile gerçeği kıyaslama
            if (tahminEdilen.equals(gercek)) {
                dogruSayisi++;
            }
        }

        // Doğruluk Oranı hesaplarız
        return (double) dogruSayisi / testVerisi.size();
    }
}