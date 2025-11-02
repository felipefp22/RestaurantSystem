package com.RestaurantSystem.Entities.CompaniesCompound.DTOs;

import java.util.UUID;

public record CreateOrUpdateCompoundDTO(
        UUID compoundID,
        String compoundName,
        String compoundDescription
) {
}
