package gui;

import classifier.KararAgaci; 
import classifier.IClassifier;
import classifier.KNNClassifier;
import data.DataYukle;
import model.UserRecord;
import preprocess.PreProcessor;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Arayuz elemanlarını barındıran ana pencere sınıfımız
public class MainGui extends JFrame {

    // Veri seti değişkenleri
    private ArrayList<UserRecord> tumTemizVeri; //  temizlenmiş veri

    // Arayüz bileşenleri
    private JLabel etiketDosyaYolu;
    private JButton butonDosyaYukle;
    private JRadioButton radyoKNN;
    private JRadioButton radyoKA; // Karar ağacı
    private JLabel etiketParametre;
    private JTextField metinParametre;
    private JSlider sliderVeriOrani; // Test veri oranını belirleyen çubuk
    private JButton butonCalistir;
    private JTable tabloSonuclar;
    private DefaultTableModel tabloModeli;
    private CubukGrafikPaneli grafikPaneli;

    // Manuel tahmin ve hata matrisi bileşenleri
    private IClassifier mevcutEgitilmisModel; // O an hafızada olan model
    private JComboBox<String> kutuCinsiyet;
    private JTextField metinManuelHarcama, metinManuelMarka;
    private JLabel etiketManuelSonuc;
    private JButton butonManuelTahmin, butonMatrisGoster;
    
    // Algoritmaların nerede hata yaptığını tutan tablo 
    private Map<String, Map<String, Integer>> hataMatrisi = new HashMap<>();

    // Geçmiş sonuçları grafikte çizmek için liste
    private List<Double> dogrulukGecmisi = new ArrayList<>();
    private List<String> modelAdiGecmisi = new ArrayList<>();

    public MainGui() {
        // 1. Pencere ayarları
        setTitle("Kocaeli Satış Verisi - ML Sınıflandırma Analizi");
        setSize(1400, 750); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // 2. Dosya yükleme paneli
        JPanel dosyaPaneli = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        dosyaPaneli.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY)); 

        butonDosyaYukle = new JButton("Veri Seti Yükle (CSV)");
        etiketDosyaYolu = new JLabel("Yüklü Dosya: MarketSalesKocaeli.csv (Varsayılan)");
        etiketDosyaYolu.setFont(new Font("Arial", Font.ITALIC, 12));
        etiketDosyaYolu.setForeground(Color.DARK_GRAY);

        dosyaPaneli.add(butonDosyaYukle);
        dosyaPaneli.add(etiketDosyaYolu);

        // Butona tıklandığımızda dosya secme paneli acılır
        butonDosyaYukle.addActionListener(e -> dosyaSec());

        // Ayarlar Paneli
        JPanel ustPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        ustPanel.setBorder(BorderFactory.createTitledBorder("Model ve Parametre Seçimi"));

        JPanel satir1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        radyoKNN = new JRadioButton("K-Nearest Neighbors (KNN)", true);
        radyoKA = new JRadioButton("Decision Tree (Karar Ağacı)");
        ButtonGroup grup = new ButtonGroup();
        grup.add(radyoKNN);
        grup.add(radyoKA);

        etiketParametre = new JLabel("K Değeri (Örn: 5):");
        metinParametre = new JTextField("5", 5);

        // Seçilen algoritmaya göre etiket metnini güncelleriz
        radyoKNN.addActionListener(e -> {
            etiketParametre.setText("K Değeri (Örn: 5):");
            metinParametre.setText("5");
        });
        radyoKA.addActionListener(e -> {
            etiketParametre.setText("Maks. Derinlik (Örn: 7):");
            metinParametre.setText("7");
        });

        satir1.add(radyoKNN);
        satir1.add(radyoKA);
        satir1.add(etiketParametre);
        satir1.add(metinParametre);

        JPanel satir2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        JLabel etiketSlider = new JLabel("Eğitim Verisi Oranı (%): ");
        
        sliderVeriOrani = new JSlider(JSlider.HORIZONTAL, 50, 90, 80);
        sliderVeriOrani.setMajorTickSpacing(10);
        sliderVeriOrani.setMinorTickSpacing(5);
        sliderVeriOrani.setPaintTicks(true);
        sliderVeriOrani.setPaintLabels(true);

        butonCalistir = new JButton("Modeli Çalıştır ve Test Et");
        butonCalistir.setBackground(new Color(0, 153, 76));
        butonCalistir.setForeground(Color.BLACK); 
        butonCalistir.setFont(new Font("Arial", Font.BOLD, 12));

        satir2.add(etiketSlider);
        satir2.add(sliderVeriOrani);
        satir2.add(Box.createHorizontalStrut(30)); 
        satir2.add(butonCalistir);

        ustPanel.add(satir1);
        ustPanel.add(satir2);

        // Dosya ve Ayarlar panellerini birleştiriyoruz
        JPanel baslikPaneli = new JPanel(new BorderLayout());
        baslikPaneli.add(dosyaPaneli, BorderLayout.NORTH);
        baslikPaneli.add(ustPanel, BorderLayout.CENTER);

        add(baslikPaneli, BorderLayout.NORTH);

        // Sonuc Tablosu
        String[] kolonlar = {"Model", "Parametre", "Train %", "Eğitim Süresi", "Tahmin Süresi", "Doğruluk (%)"};
        tabloModeli = new DefaultTableModel(kolonlar, 0);
        tabloSonuclar = new JTable(tabloModeli);
        JScrollPane tabloKaydirma = new JScrollPane(tabloSonuclar);
        tabloKaydirma.setBorder(BorderFactory.createTitledBorder("Geçmiş Test Sonuçları (Karşılaştırma Tablosu)"));
        tabloKaydirma.setPreferredSize(new Dimension(850, 150));

        add(tabloKaydirma, BorderLayout.CENTER);

        // Custom BarPlot Grafiği
        grafikPaneli = new CubukGrafikPaneli();
        grafikPaneli.setPreferredSize(new Dimension(850, 250));
        grafikPaneli.setBorder(BorderFactory.createTitledBorder("Doğruluk Oranı (Accuracy) Bar Plot Grafiği"));
        add(grafikPaneli, BorderLayout.SOUTH);

        // 6. Canlı tahmin ve hata matrisi
        manuelPaneliKur();

        // 7. Çalıstır butonu
        butonCalistir.addActionListener(e -> testiCalistir());

        // Başlangıçta varsayılan dosyayı yükle
        sessizVeriYukle("MarketSalesKocaeli.csv");
    }

    // Dosya Seçme Kısmı
    // CSV formatında dosya seçmemizi sağlar
    private void dosyaSec() {
        JFileChooser dosyaSecici = new JFileChooser();
        dosyaSecici.setDialogTitle("CSV Veri Setini Seçin");
        
        int secim = dosyaSecici.showOpenDialog(this);
        
        if (secim == JFileChooser.APPROVE_OPTION) {
            String secilenDosyaYolu = dosyaSecici.getSelectedFile().getAbsolutePath();
            etiketDosyaYolu.setText("Yüklü Dosya: " + dosyaSecici.getSelectedFile().getName());
            uyariliVeriYukle(secilenDosyaYolu); 
            
            // Yeni dosya yüklendiğinde eskileri temizleriz
            tabloModeli.setRowCount(0);
            dogrulukGecmisi.clear();
            modelAdiGecmisi.clear();
            grafikPaneli.grafigiGuncelle(dogrulukGecmisi, modelAdiGecmisi);
            
            butonManuelTahmin.setEnabled(false);
            butonMatrisGoster.setEnabled(false);
        }
    }

   
    // Manuel dosya seçiminde başarılı veya başarısız denemeleri gösteririz
    private void uyariliVeriYukle(String dosyaYolu) {
        try {
            ArrayList<UserRecord> hamVeri = DataYukle.veriYukle(dosyaYolu);
            tumTemizVeri = PreProcessor.veriyiTemizle(hamVeri);
            PreProcessor.veriyiNormallestir(tumTemizVeri);

            if(tumTemizVeri.isEmpty()) {
                throw new Exception("Veri seti boş veya format hatalı!");
            }
            
            JOptionPane.showMessageDialog(this, "Veri seti başarıyla yüklendi!\nSatır Sayısı: " + tumTemizVeri.size(), "Başarılı", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Veri yüklenirken hata oluştu! CSV formatını kontrol edin.", "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Başlangıçta veri yükleme methodu
    private void sessizVeriYukle(String dosyaYolu) {
        try {
            ArrayList<UserRecord> hamVeri = DataYukle.veriYukle(dosyaYolu);
            tumTemizVeri = PreProcessor.veriyiTemizle(hamVeri);
            PreProcessor.veriyiNormallestir(tumTemizVeri);
        } catch (Exception ex) {
            System.out.println("Varsayılan dosya bulunamadı, manuel yükleme bekleniyor.");
        }
    }

    
    // Arayüzün sağ tarafındaki canlı tahmin araçlarını oluşturuyoruz
    private void manuelPaneliKur() {
        JPanel manuelPanel = new JPanel(new GridLayout(8, 1, 5, 5));
        manuelPanel.setBorder(BorderFactory.createTitledBorder("Canlı Tahmin Testi"));
        manuelPanel.setPreferredSize(new Dimension(350, 400));

        manuelPanel.add(new JLabel("Cinsiyet:"));
        kutuCinsiyet = new JComboBox<>(new String[]{"Kadın", "Erkek"});
        manuelPanel.add(kutuCinsiyet);

        manuelPanel.add(new JLabel("Harcama Tutarı (Normalize/Ham):"));
        metinManuelHarcama = new JTextField();
        manuelPanel.add(metinManuelHarcama);

        manuelPanel.add(new JLabel("Marka Kodu (Normalize/Ham):"));
        metinManuelMarka = new JTextField();
        manuelPanel.add(metinManuelMarka);

        butonManuelTahmin = new JButton("Tahmin Et");
        butonManuelTahmin.setEnabled(false); 
        manuelPanel.add(butonManuelTahmin);

        etiketManuelSonuc = new JLabel("Sonuç: ---", SwingConstants.CENTER);
        etiketManuelSonuc.setFont(new Font("Arial", Font.BOLD, 14));
        etiketManuelSonuc.setForeground(new Color(0, 102, 204));
        manuelPanel.add(etiketManuelSonuc);

        manuelPanel.add(new JLabel("")); 

        butonMatrisGoster = new JButton("Hata Matrisini Gör");
        butonMatrisGoster.setEnabled(false); 
        manuelPanel.add(butonMatrisGoster);

        add(manuelPanel, BorderLayout.EAST);

        butonManuelTahmin.addActionListener(e -> manuelTahminYap());
        butonMatrisGoster.addActionListener(e -> hataMatrisiniGoster());
    }

    // Testi calıstırma ve kaydetme methodu
    private void testiCalistir() {
        if (tumTemizVeri == null || tumTemizVeri.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Lütfen önce bir veri seti yükleyin!", "Veri Yok", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int parametre = Integer.parseInt(metinParametre.getText().trim());
            int egitimYuzdesi = sliderVeriOrani.getValue(); 

            // Verileri rastgele karıştırır ve 2ye böleriz.eğitim test oranı için
            Collections.shuffle(tumTemizVeri); 
            int bolmeIndeksi = (int) (tumTemizVeri.size() * (egitimYuzdesi / 100.0));
            
            List<UserRecord> egitimVerisi = tumTemizVeri.subList(0, bolmeIndeksi);
            List<UserRecord> testVerisi = tumTemizVeri.subList(bolmeIndeksi, tumTemizVeri.size());

            IClassifier model;
            String modelAdi;

            // Seçime göre algoritmayı oluştururuz
            if (radyoKNN.isSelected()) {
                model = new KNNClassifier(parametre);
                modelAdi = "KNN (K=" + parametre + ")";
            } else {
                model = new KararAgaci(parametre); 
                modelAdi = "DT (Depth=" + parametre + ")";
            }

            // Eğitim Süreci
            long t1 = System.currentTimeMillis();
            model.train(egitimVerisi);
            long egitimSuresi = System.currentTimeMillis() - t1;

            // Test Süreci
            long t2 = System.currentTimeMillis();
            int dogruTahmin = 0;
            hataMatrisi.clear(); 

            for (UserRecord kullanici : testVerisi) {
                String gercek = kullanici.getCategory();
                String tahminEdilen = model.predict(kullanici);

                if (tahminEdilen != null && tahminEdilen.equals(gercek)) {
                    dogruTahmin++;
                }

                // Hata matrisi tablosu için oyları sayarız
                hataMatrisi.putIfAbsent(gercek, new HashMap<>());
                Map<String, Integer> satir = hataMatrisi.get(gercek);
                satir.put(tahminEdilen, satir.getOrDefault(tahminEdilen, 0) + 1);
            }
            long tahminSuresi = System.currentTimeMillis() - t2;

            // Başarı hesaplama ve Tabloya yazdırma kısımlarını oluşturuyoruz
            double dogruluk = ((double) dogruTahmin / testVerisi.size()) * 100;
            String dogrulukMetni = String.format("%.2f", dogruluk);

            Object[] tabloSatiri = {
                modelAdi, 
                parametre, 
                "%" + egitimYuzdesi + " (" + egitimVerisi.size() + ")", 
                egitimSuresi + " ms", 
                tahminSuresi + " ms", 
                dogrulukMetni
            };
            tabloModeli.addRow(tabloSatiri);

            // Grafiği Güncelle
            dogrulukGecmisi.add(dogruluk);
            modelAdiGecmisi.add(modelAdi + " (%" + egitimYuzdesi + ")");
            grafikPaneli.grafigiGuncelle(dogrulukGecmisi, modelAdiGecmisi);

            // Arayüzü dışarıdan tahmin yapmak için aktifleştir
            mevcutEgitilmisModel = model;
            butonManuelTahmin.setEnabled(true);
            butonMatrisGoster.setEnabled(true);

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Lütfen parametre için geçerli bir tam sayı giriniz!", "Giriş Hatası", JOptionPane.WARNING_MESSAGE);
        }
    }

    // Manuel Tahmin kısmı(Dısarıdan veri girip canlı tahmin yaparız)
    private void manuelTahminYap() {
        try {
            int cinsiyet = kutuCinsiyet.getSelectedIndex(); 
            double hamHarcama = Double.parseDouble(metinManuelHarcama.getText());
            double hamMarka = Double.parseDouble(metinManuelMarka.getText());

            // Elle girilen harcamayı normalize ederiz
            double harcama = hamHarcama;
            if (PreProcessor.maxHarcama != PreProcessor.minHarcama) {
                harcama = (hamHarcama - PreProcessor.minHarcama) / (PreProcessor.maxHarcama - PreProcessor.minHarcama);
            }

            // Elle girilen markayı algoritmaya uygun hale getiririz
            double marka = hamMarka;
            if (PreProcessor.maxMarka != PreProcessor.minMarka) {
                marka = (hamMarka - PreProcessor.minMarka) / (PreProcessor.maxMarka - PreProcessor.minMarka);
            }

            
            UserRecord manuelMusteri = new UserRecord("MANUAL", cinsiyet, harcama, marka, "");
            
            String sonuc = mevcutEgitilmisModel.predict(manuelMusteri);
            etiketManuelSonuc.setText("Sonuç: " + sonuc);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Geçerli sayısal değerler giriniz!", "Hata", JOptionPane.WARNING_MESSAGE);
        }
    }

    // Hata matrisi gösterimi
    // Algoritmanın hangi kategoriyi neyle karıştırdığını tablo olarak sunarız
    private void hataMatrisiniGoster() {
        if (hataMatrisi.isEmpty()) return;

        List<String> kategoriler = new ArrayList<>(hataMatrisi.keySet());
        Collections.sort(kategoriler);

        String[] kolonIsimleri = new String[kategoriler.size() + 1];
        kolonIsimleri[0] = "Gerçek \\ Tahmin";
        for (int i = 0; i < kategoriler.size(); i++) kolonIsimleri[i + 1] = kategoriler.get(i);

        Object[][] veri = new Object[kategoriler.size()][kategoriler.size() + 1];

        for (int i = 0; i < kategoriler.size(); i++) {
            String gercek = kategoriler.get(i);
            veri[i][0] = gercek; 
            for (int j = 0; j < kategoriler.size(); j++) {
                String tahminEdilen = kategoriler.get(j);
                veri[i][j + 1] = hataMatrisi.get(gercek).getOrDefault(tahminEdilen, 0);
            }
        }

        JTable matrisTablosu = new JTable(veri, kolonIsimleri);
        matrisTablosu.setRowHeight(30); 
        matrisTablosu.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12)); 
        matrisTablosu.setFont(new Font("Arial", Font.PLAIN, 12)); 
        matrisTablosu.setEnabled(false); 
        
        matrisTablosu.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); 
        for (int i = 0; i < matrisTablosu.getColumnCount(); i++) {
            matrisTablosu.getColumnModel().getColumn(i).setPreferredWidth(120); 
        }

        JScrollPane kaydirmaPaneli = new JScrollPane(matrisTablosu);
        kaydirmaPaneli.setPreferredSize(new Dimension(1000, 600));

        JOptionPane.showMessageDialog(this, kaydirmaPaneli, "Hata Matrisi (Confusion Matrix)", JOptionPane.PLAIN_MESSAGE);
    }

    // Custom Barplot Grafiği Çizimi
    class CubukGrafikPaneli extends JPanel {
        private List<Double> dogruluklar = new ArrayList<>();
        private List<String> etiketler = new ArrayList<>();

        public void grafigiGuncelle(List<Double> dogruluklar, List<String> etiketler) {
            this.dogruluklar = dogruluklar;
            this.etiketler = etiketler;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (dogruluklar.isEmpty()) return;

            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); 
            
            int genislik = getWidth();
            int yukseklik = getHeight();
            int bosluk = 45; 

            // Eksen çizgileri
            g2d.drawLine(bosluk, yukseklik - bosluk, genislik - bosluk, yukseklik - bosluk); 
            g2d.drawLine(bosluk, yukseklik - bosluk, bosluk, bosluk); 

            int cubukGenisligi = 60;
            int aralik = 40;
            int baslangicX = bosluk + 20;

            for (int i = 0; i < dogruluklar.size(); i++) {
                double oran = dogruluklar.get(i);
                int cubukYuksekligi = (int) ((oran / 100.0) * (yukseklik - 2 * bosluk)); 
                int x = baslangicX + i * (cubukGenisligi + aralik);
                int y = yukseklik - bosluk - cubukYuksekligi;

                // Algoritmaya göre renk ayrımı
                if (etiketler.get(i).contains("KNN")) {
                    g2d.setColor(new Color(51, 153, 255)); 
                } else {
                    g2d.setColor(new Color(255, 128, 0)); 
                }

                g2d.fillRect(x, y, cubukGenisligi, cubukYuksekligi);
                g2d.setColor(Color.DARK_GRAY);
                g2d.drawRect(x, y, cubukGenisligi, cubukYuksekligi);

                // Barın üzerine yüzde değerinin yazılması
                g2d.setFont(new Font("Arial", Font.BOLD, 12));
                g2d.drawString(String.format("%.1f%%", oran), x + 10, y - 10);

                // Barın altına modelin adını yazılması
                g2d.setFont(new Font("Arial", Font.PLAIN, 10));
                g2d.drawString(etiketler.get(i), x - 10, yukseklik - bosluk + 20);
            }
        }
    }

    public static void main(String[] args) {
        try {
            // Windows Mac entegresi
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(() -> {
            MainGui app = new MainGui();
            app.setVisible(true);
        });
    }
}