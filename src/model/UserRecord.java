package model;

// Veri setindeki tek bir müşteri/satış kaydını temsil eder.
public class UserRecord {

    private String clientCode;
    private int gender;
    private double lineNetTotal;
    private String brand;
    private String category;

    public UserRecord(
            String clientCode,
            int gender,
            double lineNetTotal,
            String brand,
            String category
    ) {
        this.clientCode = clientCode;
        this.gender = gender;
        this.lineNetTotal = lineNetTotal;
        this.brand = brand;
        this.category = category;
    }

    public String getClientCode() {
        return clientCode;
    }

    public int getGender() {
        return gender;
    }

    public double getLineNetTotal() {
        return lineNetTotal;
    }

    public String getBrand() {
        return brand;
    }

    public String getCategory() {
        return category;
    }

    public void setLineNetTotal(double lineNetTotal) {
        this.lineNetTotal = lineNetTotal;
    }

    @Override
    public String toString() {
        return "UserRecord{" +
                "clientCode='" + clientCode + '\'' +
                ", gender=" + gender +
                ", lineNetTotal=" + String.format("%.4f", lineNetTotal) +
                ", brand='" + brand + '\'' +
                ", category='" + category + '\'' +
                '}';
    }
}