package gr.spinellis.basic.invoice;

import java.util.List;
import gr.spinellis.basic.product.*;

/**
 * @composed 1 - * InvoiceItem
 * @assoc * - 1 Customer
 */
public class Invoice {
    public double total;
    public InvoiceItem[] items;
    public Customer customer;
    
    public void addItem(Product p, int quantity) {};
}
