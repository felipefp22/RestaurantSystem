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
    public String createDispatchItemsPrint(Company company, Order order, PrintCategory printCategory, List<OrdersItems> orderItems, Boolean isWithPrice) {
        String header = getHeader(company);
        String date = getDate(order);
        String orderNum = "";
        String tableOrDeliveryOrPickupNum = order.getTableNumberOrDeliveryOrPickup().equals("delivery") ? "* DELIVERY *\n\n" : order.getTableNumberOrDeliveryOrPickup().equals("pickup") ? "* RETIRADA *\n\n" : "";
        String thirdSp = "";
        String customerData = getAddress(order);

        if (order.getTableNumberOrDeliveryOrPickup().equals("delivery") || order.getTableNumberOrDeliveryOrPickup().equals("pickup")) {
            orderNum = getOrderNumber(order);
            if (order.getIsThirdSpOrder() != null) thirdSp = separatorLne + order.getIsThirdSpOrder() + separatorLne;
        } else {
            tableOrDeliveryOrPickupNum = "\nMesa: " + order.getTableNumberOrDeliveryOrPickup() + "\n\n";
        }

        List<PrintSyncOrderItemsDTO> itemsToCreateText = switch (printCategory) {
//            case FOODS -> getPrintSyncOrderItemsDTO(orderItems, false, isCancelled);
//            case DESSERTS -> getPrintSyncOrderItemsDTO(orderItems, false, isCancelled);
//            case DRINKS -> getPrintSyncOrderItemsDTO(orderItems.stream().filter(x -> x.getPrinCategoy), true, isCancelled);
//            case BEVERAGES -> getPrintSyncOrderItemsDTO(orderItems.stream().filter(x -> x.getPrinCategoy), true, isCancelled);
            default -> getPrintSyncOrderItemsDTO(company, orderItems);
        };

        String itemsText = createPreparationText(itemsToCreateText, isWithPrice, false, true);

        String finalText = centerCommand + header + date + orderNum + tableOrDeliveryOrPickupNum + thirdSp + customerData + leftCommand + itemsText + getFooter();

        return finalText;
    }

    public String createPreparationItemsPrint(Company company, Order order, PrintCategory printCategory, List<OrdersItems> orderItems, Boolean isCancelled) {
        String header = getHeader(company);
        String date = getDate(order);
        String orderNum = getOrderNumber(order);
        String tableNum = "\nMesa: " + order.getTableNumberOrDeliveryOrPickup() + "\n\n";

        List<PrintSyncOrderItemsDTO> itemsToCreateText = switch (printCategory) {
//            case FOODS -> getPrintSyncOrderItemsDTO(orderItems, false, isCancelled);
//            case DESSERTS -> getPrintSyncOrderItemsDTO(orderItems, false, isCancelled);
//            case DRINKS -> getPrintSyncOrderItemsDTO(orderItems.stream().filter(x -> x.getPrinCategoy), true, isCancelled);
//            case BEVERAGES -> getPrintSyncOrderItemsDTO(orderItems.stream().filter(x -> x.getPrinCategoy), true, isCancelled);
            default -> getPrintSyncOrderItemsDTO(company, orderItems);
        };

        String itemsText = createPreparationText(itemsToCreateText, false, isCancelled, false);

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

    public String getCutCommand() {
        return cutCommand;
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
        try {
            String systemCustomer = (order.getCustomer() != null && order.getCustomer().getCustomerName() != null) ?
                    order.getCustomer().getCustomerName() : ((order.getPickupName() != null) ? order.getPickupName() : "");

            if (order.getTableNumberOrDeliveryOrPickup().equals("delivery")) {

                if (order.getIsThirdSpOrder() != null) {
                    return separatorLne + order.getPickupName() + "\n" +
                            order.getThirdSpAddress() + ", " + order.getThirdSpAddressNumber() + "\n" +
                            (order.getThirdSpComplementAddress() != null ? "Compl:" + order.getThirdSpComplementAddress() + "\n" : "") +
                            ((order.getThirdSpAddressReference() != null) ? "Ref: " + order.getThirdSpAddressReference() + "\n" : "") +
                            ((order.getThirdSpPhone() != null) ? "Tel: " + order.getThirdSpPhone() + "\n" : "") +
                            ((order.getThirdSpPhoneLocalizer() != null) ? "Localizador: " + order.getThirdSpPhoneLocalizer() : "")
                            + separatorLne;
                } else {
                    return "";
                }

            } else if (order.getTableNumberOrDeliveryOrPickup().equals("pickup")) {
                if (order.getIsThirdSpOrder() != null) {
                    return "";
                } else {
                    return systemCustomer;
                }
            } else {
                return systemCustomer;
            }
        } catch (Exception e) {
            return separatorLne + "Erro ao pegar dados do usuario, conferir no " + order.getIsThirdSpOrder() +
                    ((order.getThirdSpOrderNumber() != null) ? ", pedido numero " + order.getOrderNumberOnShift() : "") +
                    "\n\nERRO descrição: " + e.getMessage() + separatorLne;
        }
    }

    private String getFooter() {
        return "\n\n\n\n\n";
    }


    private String createPreparationText(List<PrintSyncOrderItemsDTO> ordersToCreateText, Boolean withPrice, boolean isCancelled, Boolean saveSpace) {
        StringBuilder itemsText = new StringBuilder();
        Integer lastPrintPriority = null;
        for (PrintSyncOrderItemsDTO x : ordersToCreateText) {
            Boolean isHalfHalf = x.getProductId().size() == 2;
            Boolean isOneThird = x.getProductId().size() == 3;
            Boolean isOneQuarter = x.getProductId().size() == 4;
            if (!Objects.equals(lastPrintPriority, x.getPrintPriority()) && !isCancelled) {
                itemsText.append(itemsText.isEmpty() ? "--------------------------------" : (!saveSpace ? "\n\n--------------------------------" : "\n--------------------------------"))
                        .append(!saveSpace ? separatorLne : "\n")
                        .append(boldOn + "      *  " + x.getCategoryName().toUpperCase() + (!saveSpace ? "  *\n\n" : "  *\n") + boldOff);
            }
            itemsText.append((!Objects.equals(lastPrintPriority, x.getPrintPriority()) || saveSpace) ? "" : separatorLneSmall)
                    .append((Objects.equals(lastPrintPriority, x.getPrintPriority()) && saveSpace) ? "\n" : "")
                    .append(boldOn + x.getQuantity() + boldOff + " x ")
                    .append(saveSpace ? "" : isHalfHalf ? "[ 1/2 - Meia ]\n" : (isOneThird ? "[ 1/3 - Terco ]\n" : (isOneQuarter ? "[ 1/4 - Quarto ]\n" : "")))
                    .append(boldOn + x.getName().toUpperCase().replaceAll("/", " / ") + boldOff)
                    .append(withPrice ? (" - R$ " + String.format("%.2f", (x.getPrice() * x.getQuantity()))) : "")
                    .append(x.getProductOptions() != null && !x.getProductOptions().isEmpty() ? x.getProductOptions().stream().map(option -> "\n - " + option.split("\\|")[1]).reduce("", String::concat) : "")
                    .append((x.getNotes() != null && !x.getNotes().isBlank()) ? italic + "\n  -- " + x.getNotes() + italicOff : "")
                    .append(isCancelled ? " (CANCELADO)" : "");

            if (!Objects.equals(lastPrintPriority, x.getPrintPriority())) lastPrintPriority = x.getPrintPriority();
        }

        return itemsText.toString();
    }

    private List<PrintSyncOrderItemsDTO> getPrintSyncOrderItemsDTO(Company company, List<OrdersItems> orderItems) {
        Map<UUID, PrintPriorityAndCategoryNameDTO> priorityMap = getProductSortPrintPriorityMap(company);
        Map<String, PrintSyncOrderItemsDTO> grouped = new LinkedHashMap<>();

        for (OrdersItems x : orderItems) {
            String key =
                    x.getProductId() + "|" +
                            x.getProductPrice() + "|" +
                            x.getProductOptions() + "|" +
                            x.getName() + "|" +
                            x.getPrice() + "|" +
                            x.getIsThirdSupplierPrice() + "|" +
                            x.getNotes();
            PrintSyncOrderItemsDTO dto = grouped.get(key);

            if (dto != null) {
                dto.setQuantity(dto.getQuantity() + 1);
            } else {
                dto = new PrintSyncOrderItemsDTO(x);
                dto.setPrintPriority(priorityMap.get(UUID.fromString(dto.getProductId().get(0))).printPriority());
                dto.setCategoryName(priorityMap.get(UUID.fromString(dto.getProductId().get(0))).categoryName());
                grouped.put(key, dto);
            }
        }

        List<PrintSyncOrderItemsDTO> ordersToCreateText = new ArrayList<>(grouped.values());
        ordersToCreateText.sort(Comparator.comparing(PrintSyncOrderItemsDTO::getPrintPriority, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(dto -> dto.getNotes() == null || dto.getNotes().isBlank()));

        return ordersToCreateText;
    }

    private Map<UUID, PrintPriorityAndCategoryNameDTO> getProductSortPrintPriorityMap(Company company) {
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
}