package com.RestaurantSystem.Services.AuxsServices;

import com.RestaurantSystem.Entities.Company.Company;
import com.RestaurantSystem.Entities.ENUMs.PrintCategory;
import com.RestaurantSystem.Entities.Order.Order;
import com.RestaurantSystem.Entities.Order.OrdersItems;
import com.RestaurantSystem.Entities.Printer.DTOs.DeletePrintSyncsDTO;
import com.RestaurantSystem.Entities.Printer.DTOs.PrintPriorityAndCategoryNameDTO;
import com.RestaurantSystem.Entities.Printer.DTOs.PrintSyncOrderItemsDTO;
import com.RestaurantSystem.Entities.Printer.PrintSync;
import com.RestaurantSystem.Entities.Product.Product;
import com.RestaurantSystem.Entities.ProductCategory.ProductCategory;
import com.RestaurantSystem.Entities.User.AuthUserLogin;
import com.RestaurantSystem.Repositories.PrintSyncRepo;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PrintSyncService {
    private final String centerCommand = "{-Center-}";
    private final String leftCommand = "{-Left-}";
    private final String rightCommand = "{-Right-}";

    private final String italic = "{-Italic-}";
    private final String italicOff = "{-Normal-}";

    private final String underlining1 = "{-Underline-}";
    private final String underlining2 = "{-Underline2-}";
    private final String underliningOff = "{-UnderlineOff-}";

    private final String boldOn = "{-Bold-}"; // Bold ON
    private final String boldOff = "{-BoldOff-}"; // Bold OFF

    private final String cutCommand = "{-CutHere-}";
    private final String separatorLne = "\n--------------------------------\n";
    private final String separatorLneSmall = "\n-------------------\n";


    private final PrintSyncRepo printSyncRepo;
    private final VerificationsServices verificationsServices;

    public PrintSyncService(PrintSyncRepo printSyncRepo, VerificationsServices verificationsServices) {
        this.printSyncRepo = printSyncRepo;
        this.verificationsServices = verificationsServices;
    }

    // <>------------ Methods ------------<>
    public String createTableItemsPrint(Company company, Order order, PrintCategory printCategory, List<OrdersItems> orderItems, Boolean isCancelled) {
        if (order.getTableNumberOrDeliveryOrPickup().equals("delivery") || order.getTableNumberOrDeliveryOrPickup().equals("pickup"))
            return "";

        String header = getHeader(company);
        String date = getDate(order);
        String orderNum = getOrderNumber(order);
        String tableNum = "\nMesa: " + order.getTableNumberOrDeliveryOrPickup() + "\n\n";


        String itemsText = switch (printCategory) {
//            case FOODS -> getOrderItemsText(orderItems, false, isCancelled);
//            case DESSERTS -> getOrderItemsText(orderItems, false, isCancelled);
//            case DRINKS -> getOrderItemsText(orderItems.stream().filter(x -> x.getPrinCategoy), true, isCancelled);
//            case BEVERAGES -> getOrderItemsText(orderItems.stream().filter(x -> x.getPrinCategoy), true, isCancelled);
            default -> getOrderItemsText(company, orderItems, false, isCancelled);
        };

        String finalText = centerCommand + header + date + tableNum + leftCommand + (isCancelled ? getCancelledText() + "\n" : "") +
                itemsText + getFooter();

        return finalText;
    }

    public String createDeliveryPrint(Company company, Order order) {
        String dispatchOrOperation = "Dispacho \n\n";
        String header = getHeader(company);
        String date = getDate(order);
        String orderNum = getOrderNumber(order);
        String address = getAddress(order);

        String finalText = centerCommand + dispatchOrOperation + header + leftCommand + orderNum + date + separatorLne + address + separatorLne + getFooter();

        return separatorLne + separatorLne + getFooter() + cutCommand + finalText;
    }

    public void deletePrintSyncs(DeletePrintSyncsDTO dto, String requesterID) {
        AuthUserLogin requester = verificationsServices.retrieveRequester(requesterID);
        Company company = verificationsServices.retrieveCompany(dto.companyID());
        verificationsServices.justOwnerOrServer(company, requester);

        List<PrintSync> printSyncsToDelete = printSyncRepo.findAllById(dto.printSyncsToDeleteIDs());

        if (!printSyncsToDelete.isEmpty()) {
            List<PrintSync> filteredPrintSyncs = printSyncsToDelete.stream()
                    .filter(ps -> ps.getCompany().getId().equals(company.getId()))
                    .toList();
            printSyncRepo.deleteAll(filteredPrintSyncs);
        }
    }


    // <>------------ Helpers ------------<>
    private String getHeader(Company company) {
        return boldOn + company.getCompanyName() + boldOff + "\n";
    }

    private String getDate(Order order) {
        String day = String.valueOf(order.getOpenOrderDateUtc().getDayOfMonth());
        String month = String.valueOf(order.getOpenOrderDateUtc().getMonthValue());
        String year = String.valueOf(order.getOpenOrderDateUtc().getYear());
        String hour = String.valueOf(order.getOpenOrderDateUtc().getHour());
        String minute = String.valueOf(order.getOpenOrderDateUtc().getMinute());

        return day + "/" + month + "/" + year + " - " + hour + ":" + minute + "\n";
    }

    private String getCancelledText() {
        return separatorLne + "      ***! CANCELAMENTO !***" + separatorLne;
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

    private String getOrderItemsText(Company company, List<OrdersItems> orderItems, Boolean withPrice, boolean isCancelled) {
        Map<UUID, PrintPriorityAndCategoryNameDTO> priorityMap = getProductPrintSortMap(company);

        List<PrintSyncOrderItemsDTO> ordersToCreateText = new ArrayList<>();
        orderItems.forEach(x -> {
            PrintSyncOrderItemsDTO foundEqual = ordersToCreateText.stream().filter(y -> Objects.equals(y.getProductId(), x.getProductId()) && Objects.equals(y.getProductPrice(), x.getProductPrice()) &&
                    Objects.equals(y.getProductOptions(), x.getProductOptions()) && Objects.equals(y.getName(), x.getName()) && Objects.equals(y.getPrice(), x.getPrice()) &&
                    Objects.equals(y.getIsThirdSupplierPrice(), x.getIsThirdSupplierPrice()) && Objects.equals(y.getNotes(), x.getNotes())).findFirst().orElse(null);

            if (foundEqual != null) {
                foundEqual.setQuantity(foundEqual.getQuantity() + 1);
            } else {
                PrintSyncOrderItemsDTO dto = new PrintSyncOrderItemsDTO(x);
                dto.setPrintPriority(priorityMap.get(UUID.fromString(dto.getProductId().get(0))).printPriority());
                dto.setCategoryName(priorityMap.get(UUID.fromString(dto.getProductId().get(0))).categoryName());
                ordersToCreateText.add(dto);
            }
        });
//        ordersToCreateText.sort(Comparator.comparing(x -> priorityMap.get(UUID.fromString(x.getProductId().get(0))), Comparator.nullsLast(Integer::compareTo)));
        ordersToCreateText.sort(Comparator.comparing(PrintSyncOrderItemsDTO::getPrintPriority, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(dto -> dto.getNotes() == null || dto.getNotes().isBlank()));

        StringBuilder itemsText = new StringBuilder();
        Integer lastPrintPriority = null;
        for (PrintSyncOrderItemsDTO x : ordersToCreateText) {
            if (!Objects.equals(lastPrintPriority, x.getPrintPriority()) && !isCancelled) {
                itemsText.append(itemsText.isEmpty() ? "--------------------------------" : "\n\n" + "--------------------------------")
                        .append(separatorLne)
                        .append(boldOn + "      *  " + x.getCategoryName().toUpperCase() + "  *\n\n" + boldOff);
            }
            itemsText.append(!Objects.equals(lastPrintPriority, x.getPrintPriority()) ? "" : separatorLneSmall)
                    .append(x.getQuantity())
                    .append(" x ")
                    .append(boldOn + x.getName().toUpperCase().replaceAll("/", " / ") + boldOff)
                    .append((x.getNotes() != null && !x.getNotes().isBlank()) ? italic + "\n   - " + x.getNotes() + italicOff : "")
                    .append(isCancelled ? " (CANCELADO)" : "");

            if (!Objects.equals(lastPrintPriority, x.getPrintPriority())) lastPrintPriority = x.getPrintPriority();
        }

        return itemsText.toString();
    }

    private Map<UUID, PrintPriorityAndCategoryNameDTO> getProductPrintSortMap(Company company) {
        int ifPriorityNullNextFakeValue = 100000;
        Map<UUID, PrintPriorityAndCategoryNameDTO> map = new HashMap<>();

        List<ProductCategory> pCategories = new ArrayList<>(company.getProductsCategories());
        pCategories.sort(Comparator
                .comparing(ProductCategory::getPrintPriority, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(ProductCategory::getCategoryName, String.CASE_INSENSITIVE_ORDER));

        for (ProductCategory pc : pCategories) {
            Integer printPriority = pc.getPrintPriority();
            if (printPriority == null) {
                printPriority = ifPriorityNullNextFakeValue;
                ifPriorityNullNextFakeValue += 1;
            }
            for (Product product : pc.getProducts()) {
                map.put(product.getId(), new PrintPriorityAndCategoryNameDTO(printPriority, pc.getCategoryName()));
            }
        }

        return map;
    }

    private Map<UUID, Integer> getProductCategoriesNameByPrintSortMap(Company company) {
        int ifPriorityNullNextFakeValue = 100000;
        Map<UUID, Integer> map = new HashMap<>();

        List<ProductCategory> pCategories = new ArrayList<>(company.getProductsCategories());
        pCategories.sort(Comparator
                .comparing(ProductCategory::getPrintPriority, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(ProductCategory::getCategoryName, String.CASE_INSENSITIVE_ORDER));

        for (ProductCategory pc : pCategories) {
            Integer printPriority = pc.getPrintPriority();
            if (printPriority == null) {
                printPriority = ifPriorityNullNextFakeValue;
                ifPriorityNullNextFakeValue += 1;
            }
            for (Product product : pc.getProducts()) {
                map.put(product.getId(), printPriority);
            }
        }

        return map;
    }

    private String getFooter() {
        return "\n\n\n\n\n";
    }
}
