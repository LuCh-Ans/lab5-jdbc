package lab5.model;

public class Product {
    private int id;
    private String name;
    private String brand;
    private String category;
    private double price;
    private boolean inStock;

    public Product() {}

    public Product(int id, String name, String brand, String category, double price, boolean inStock) {
        this.id = id;
        this.name = name;
        this.brand = brand;
        this.category = category;
        this.price = price;
        this.inStock = inStock;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public boolean isInStock() { return inStock; }
    public void setInStock(boolean inStock) { this.inStock = inStock; }
    @Override
    public String toString() {
        return "[" + id + "] " + name + " / " + brand;
    }
}
