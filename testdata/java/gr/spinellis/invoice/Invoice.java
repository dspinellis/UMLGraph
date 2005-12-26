package gr.spinellis.invoice;

import java.util.List;
import gr.spinellis.product.*;

/**
 * @composed 1 - * gr.spinellis.invoice.InvoiceItem
 * @assoc * - 1 gr.spinellis.invoice.Customer
 */
public class Invoice {
    public double total;
    public InvoiceItem[] items;
    public Customer customer;
    
    public void addItem(Product p, int quantity) {};
}
