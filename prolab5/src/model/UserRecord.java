package model;

// her bir satırı temsil eden nesne classımız
public class UserRecord {
    
    
    // Güvenlik için değişkenleri private tanımlarız
    private String clientCode;
    private int gender;           // cinsiyet encoding
    private double lineNetTotal;  // Toplam harcama
    
    
    private double brandCode;     
    
    private String category;      

    
    // Constructer sayesinde yeni bi müşteri oluştugunda veri setinden içine verileri okuruz
    public UserRecord(String clientCode, int gender, double lineNetTotal, double brandCode, String category) {
        this.clientCode = clientCode;
        this.gender = gender;
        this.lineNetTotal = lineNetTotal;
        this.brandCode = brandCode;
        this.category = category;
    }

    
    //GETTER methodlar dışardan veri okumak için
    public String getClientCode() {
        return clientCode;
    }

    public int getGender() {
        return gender;
    }

    public double getLineNetTotal() {
        return lineNetTotal;
    }

    public double getBrandCode() {
        return brandCode;
    }

    public String getCategory() {
        return category;
    }

    //Setter methodlar .PreProcessor classında verileri değiştirebilmek için
    
    public void setLineNetTotal(double lineNetTotal) {
        this.lineNetTotal = lineNetTotal;
    }

    public void setBrandCode(double brandCode) {
        this.brandCode = brandCode;
    }

    
    // Verileri okunaklı bi hale getiriyoruz
    @Override
    public String toString() {
        return "UserRecord{" +
                "clientCode='" + clientCode + '\'' +
                ", gender=" + gender +
                ", lineNetTotal=" + String.format("%.4f", lineNetTotal) + // Sayıların virgülden sonraki kısmını kırparız
                ", brandCode=" + String.format("%.4f", brandCode) +
                ", category='" + category + '\'' +
                '}';
    }
}