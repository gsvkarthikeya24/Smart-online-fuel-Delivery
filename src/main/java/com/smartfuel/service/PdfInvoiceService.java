package com.smartfuel.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.smartfuel.entity.Order;
import com.smartfuel.entity.Payment;
import com.smartfuel.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
public class PdfInvoiceService {

    @Autowired
    private PaymentRepository paymentRepository;

    public ByteArrayInputStream generateInvoicePdf(Order order) {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Set up fonts
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
            Font bodyFontBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);

            // Title
            Paragraph title = new Paragraph("FUEL DELIVERY INVOICE", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // Company Info
            Paragraph companyInfo = new Paragraph("Smart Online Fuel Delivery System\nSupport: support@smartfuel.com\nDate: " + 
                    order.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), bodyFont);
            companyInfo.setSpacingAfter(20);
            document.add(companyInfo);

            // Table of Details
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(20f);

            // Add Header
            PdfPCell cell1 = new PdfPCell(new Phrase("Order Details", headerFont));
            cell1.setColspan(2);
            cell1.setBackgroundColor(java.awt.Color.LIGHT_GRAY);
            cell1.setPadding(8);
            table.addCell(cell1);

            // Fetch transaction ID
            String txnId = paymentRepository.findByOrder(order)
                    .map(Payment::getTransactionId)
                    .orElse("PENDING");

            table.addCell(new Phrase("Order ID", bodyFontBold));
            table.addCell(new Phrase(order.getId().toString(), bodyFont));

            table.addCell(new Phrase("Customer Name", bodyFontBold));
            table.addCell(new Phrase(order.getCustomer().getFullName(), bodyFont));

            table.addCell(new Phrase("Customer Email", bodyFontBold));
            table.addCell(new Phrase(order.getCustomer().getEmail(), bodyFont));

            table.addCell(new Phrase("Fuel Provider", bodyFontBold));
            table.addCell(new Phrase(order.getProvider().getFullName(), bodyFont));

            table.addCell(new Phrase("Fuel Type", bodyFontBold));
            table.addCell(new Phrase(order.getFuelType().getName(), bodyFont));

            table.addCell(new Phrase("Quantity Ordered", bodyFontBold));
            table.addCell(new Phrase(String.format("%.2f Liters", order.getQuantityLiters()), bodyFont));

            table.addCell(new Phrase("Price Per Liter", bodyFontBold));
            table.addCell(new Phrase(String.format("Rs. %.2f", order.getFuelType().getBasePricePerLiter()), bodyFont));

            table.addCell(new Phrase("Emergency Surcharge", bodyFontBold));
            table.addCell(new Phrase(order.isEmergency() ? "15%" : "No Surcharge", bodyFont));

            table.addCell(new Phrase("Total Price", bodyFontBold));
            table.addCell(new Phrase(String.format("Rs. %.2f", order.getTotalPrice()), bodyFontBold));

            table.addCell(new Phrase("Payment Method", bodyFontBold));
            table.addCell(new Phrase(order.getPaymentMethod(), bodyFont));

            table.addCell(new Phrase("Payment Status", bodyFontBold));
            table.addCell(new Phrase(order.getPaymentStatus(), bodyFont));

            table.addCell(new Phrase("Transaction ID", bodyFontBold));
            table.addCell(new Phrase(txnId, bodyFont));

            table.addCell(new Phrase("Delivery Address", bodyFontBold));
            table.addCell(new Phrase(order.getDeliveryAddress(), bodyFont));

            table.addCell(new Phrase("Delivery Status", bodyFontBold));
            table.addCell(new Phrase(order.getStatus(), bodyFontBold));

            document.add(table);

            // Footer note
            Paragraph footer = new Paragraph("\nThank you for choosing Smart Online Fuel Delivery! Please rate your service in the app.", bodyFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return new ByteArrayInputStream(out.toByteArray());
    }
}
