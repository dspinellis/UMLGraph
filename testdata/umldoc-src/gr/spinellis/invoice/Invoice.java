package gr.spinellis.invoice;

import gr.spinellis.product.Product;

import java.util.Date;

public class Invoice {
    public double total;
    public InvoiceItem[] items;
    public Customer customer;
    public Date invoiceDate;
    
    public void addItem(Product p, int quantity) {};
}
