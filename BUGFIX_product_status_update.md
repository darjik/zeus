# Bug Fix: Product Status Update Not Persisting

## Issue
When selecting a product status from the dropdown in the order items table, the page would refresh but the status would revert to its original value instead of saving the new selection.

## Root Cause
The `updateOrderItemFromDTO` method in `OrderItemService` was not handling the `productStatus` field. When the controller received the status update request, it would:
1. Create a new DTO with only the productStatus field set
2. Call `updateOrderItem` which would call `updateOrderItemFromDTO`
3. The method would skip updating productStatus because it wasn't handled
4. The entity would be saved without any changes to the status field

## Fix Applied

### 1. Updated `OrderItemService.updateOrderItemFromDTO()` method
Added handling for the `productStatus` field:

```java
if (dto.getProductStatus() != null) {
    orderItem.setProductStatus(OrderItem.ProductStatus.valueOf(dto.getProductStatus()));
}
```

### 2. Improved `OrderItemService.convertToDTO()` method
Ensured that productStatus always has a default value (PENDING) instead of null:

```java
dto.setProductStatus(orderItem.getProductStatus() != null ? 
    orderItem.getProductStatus().name() : 
    OrderItem.ProductStatus.PENDING.name());
```

### 3. Simplified `OrderController.updateProductStatus()` method
Removed unnecessary database fetch since we only need to update one field:

```java
OrderItemDTO itemDTO = new OrderItemDTO();
itemDTO.setProductStatus(productStatus);
orderItemService.updateOrderItem(itemId, itemDTO);
```

## Files Modified
1. `OrderItemService.java` - Added productStatus handling in update method
2. `OrderItemService.java` - Improved default value handling in DTO conversion
3. `OrderController.java` - Simplified the update endpoint

## Testing
After applying this fix:
1. Navigate to an order with items
2. Click the product status dropdown for any item
3. Select a new status (e.g., "Procuring")
4. The page will refresh and the status should now be saved correctly
5. The dropdown should show the newly selected status

## Database
The `product_status` column was already added to the `order_items` table via migration script `V002__purchase_orders.sql`.

