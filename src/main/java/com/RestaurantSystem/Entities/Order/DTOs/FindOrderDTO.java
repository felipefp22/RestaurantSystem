package com.RestaurantSystem.Entities.Order.DTOs;

import java.util.UUID;

public record FindOrderDTO(
        UUID companyID,
        UUID orderID,
        Double money,
        Double pix,
        Double debit,
        Double credit,
        Double valeRefeicao,
        Double othersPaymentModes
) {
}
