package com.RestaurantSystem.Services.AuxsServices;

import com.RestaurantSystem.Entities.Company.Company;
import com.RestaurantSystem.Entities.Order.Order;
import com.RestaurantSystem.Repositories.PrintSyncRepo;
import org.springframework.stereotype.Service;

@Service
public class PrintSyncService {
    private final String centerCommand = "{-Center-}";
    private final String leftCommand = "{-Left-}";
    private final String rightCommand = "{-Right-}";
    private final String cutCommand = "{-CutHere-}";

    private final PrintSyncRepo printSyncRepo;

    public PrintSyncService(PrintSyncRepo printSyncRepo) {
        this.printSyncRepo = printSyncRepo;
    }

    // <>------------ Methods ------------<>

    public String createDeliveryPrint(Company company, Order order) {
        String header = getHeader(company);
        String date = getDate(order);
        String orderNum = getOrderNumber(order);
        String address = getAddress(order);

        String finalText = centerCommand + header + leftCommand + orderNum + date +  getSeparatorLine() + address + getSeparatorLine() + getFooter();

        return getSeparatorLine() + getSeparatorLine() + getFooter() + cutCommand + finalText;
    }



    // <>------------ Helpers ------------<>
    private String getSeparatorLine(){
        return "\n--------------------------------\n";
    }

    private String getHeader(Company company) {
        return "Dispacho \n\n" + company.getCompanyName() + "\n";
    }



    private String getDate(Order order) {
        String day = String.valueOf(order.getOpenOrderDateUtc().getDayOfMonth());
        String month = String.valueOf(order.getOpenOrderDateUtc().getMonthValue());
        String year = String.valueOf(order.getOpenOrderDateUtc().getYear());
        String hour = String.valueOf(order.getOpenOrderDateUtc().getHour());
        String minute = String.valueOf(order.getOpenOrderDateUtc().getMinute());

        return day + "/" + month + "/" + year + " - " + hour + ":" + minute + "\n";
    }

    private String getOrderNumber(Order order) {
        return "\n* Pedido Numero " + order.getOrderNumberOnShift() + " * \n";
    }

    private String getAddress(Order order) {
        String fullName = order.getCustomer().getCustomerName().trim();
        String[] parts = fullName.split("\\s+");

        String displayName = parts[0];
        if (parts.length > 1) {
            displayName += " " + parts[1];
        }

        return "Entrega Para \n" +
                displayName + "\n" +
                order.getCustomer().getComplement() + "\n" + "Telefone: " + order.getCustomer().getPhone();
    }



    private String getFooter() {
        return "\n\n\n\n\n";
    }
}
