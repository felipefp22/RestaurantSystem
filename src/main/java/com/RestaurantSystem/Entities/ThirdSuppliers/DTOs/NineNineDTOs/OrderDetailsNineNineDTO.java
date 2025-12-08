package com.RestaurantSystem.Entities.ThirdSuppliers.DTOs.NineNineDTOs;

import java.util.List;

public record OrderDetailsNineNineDTO(
        int errno,
        String errmsg,
        String requestId,
        long time,
        Data data
) {

    public record Data(
            long order_id,
            int status,
            int shop_accept_status,
            int before_status,
            int order_index,
            String remark,
            int city_id,
            String country,
            String timezone,
            int pay_type,
            int delivery_type,
            int delivery_eta,
            int expected_cook_eta,
            int expected_arrived_eta,
            long create_time,
            long pay_time,
            long complete_time,
            long cancel_time,
            long shop_confirm_time,
            Price price,
            Shop shop,
            ReceiveAddress receive_address,
            List<OrderItem> order_items,
            List<Promotion> promotions
    ) {}

    public record Price(
            double order_price,
            double real_price,
            double real_pay_price,
            double delivery_price,
            double shop_paid_money,
            double refund_price,
            String currency,
            double items_discount,
            double delivery_discount,
            OthersFees others_fees,
            double customer_need_paying_money
    ) {
        public record OthersFees(
                double small_order_price,
                double total_tip_money,
                double service_price,
                double coupon_discount
        ) {}
    }

    public record Shop(
            long shop_id,
            String app_shop_id,
            String shop_addr,
            String shop_name,
            List<String> shop_phone
    ) {}

    public record ReceiveAddress(
            long uid,
            String name,
            String first_name,
            String last_name,
            String calling_code,
            String phone,
            String city,
            String country_code,
            String poi_address,
            String house_number,
            String poi_lat,
            String poi_lng,
            String coordinate_type,
            String poi_display_name
    ) {}

    public record OrderItem(
            String app_item_id,
            String app_external_id,
            String name,
            double total_price,
            double sku_price,
            int amount,
            String remark,
            List<SubItem> sub_item_list,
            PromotionDetail promotion_detail,
            double real_price,
            int promo_type
    ) {

        public record SubItem(
                String app_item_id,
                String name,
                double total_price,
                double sku_price,
                int amount,
                String app_content_id,
                String content_app_external_id,
                List<String> sub_item_list
        ) {}
    }

    public record PromotionDetail(
            int promo_type,
            double save_price,
            double shop_subside_price
    ) {}

    public record Promotion(
            int promo_type,
            double save_price,
            double shop_subside_price
    ) {}
}
