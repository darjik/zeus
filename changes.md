# Changes Tracker

## Order Processing Expansion TODOs

Derived from the latest IMS requirements and the development guidance in `.github/copilot-instructions.md`, the following items document the next set of work for the order lifecycle.

1. **Vendor purchase orders** ✅ DONE
   - After the existing four default tasks are complete, surface the ability to select the items required for an order and generate one or more purchase orders addressed to the appropriate vendors.
   - Track the items, their quantities, target vendor, expected delivery, and connect each PO back to the originating order.
   - **Implementation**: Created `PurchaseOrder` and `PurchaseOrderItem` entities with supporting repositories, services, and controller endpoints. Added UI in order view page with modal for creating POs by selecting order items and vendor. Database tables created via migration script.

2. **Product-level procurement status** ✅ DONE
   - Introduce a `Procuring` status (if not already present) so each product tied to an order can be individually tracked before assembly begins.
   - Maintain the ability to update each product separately so its progress is visible within the broader order.
   - **Implementation**: Added `ProductStatus` enum to `OrderItem` entity with statuses: PENDING, PROCURING, IN_ASSEMBLY, IN_POWDER_COATING, READY, PACKED. Added dropdown in order items table to update individual product status. Database column added via migration script.

2a. **Order-specific product part customization** ✅ DONE
   - Products added to an order should copy their default part list, but allow per-order customization.
   - Users can add or remove parts for specific order items without affecting the master product part list.
   - This enables flexibility for custom orders where part requirements may vary from the standard product configuration.
   - **Implementation**: Created `OrderItemPart` entity to track parts specific to each order item. Parts are automatically copied from `ProductPart` when order item is created. Added `OrderItemPartService` for managing parts and `OrderItemPartController` with full CRUD endpoints. Created UI at `/orders/{orderId}/items/{itemId}/parts` with ability to add/remove parts and adjust quantities. Database table created via migration script. Added "Manage Parts" button in order items table.

3. **Assembly / trial / disassembly cycle**
   - Once all materials are received, move the order status to `In Assembly` and prompt technicians to log trial results.
   - Provide a follow-up task to disassemble the machine/product before powder coating or corrective work begins.

4. **Powder coating loop + reassembly trials**
   - After disassembly, set the product status to `In Powder Coating` and record incoming/outgoing dates.
   - When the coating step is finished, transition into assembly again and capture a second trial run.

5. **Inspection workflows with feedback resolution**
   - After trials, initiate the first internal inspection where each inspection event captures the date, time, and iteration count along with any feedback.
   - Continue the cycle until there are no outstanding action items; every feedback item must be resolved before moving on.
   - Once internal inspection passes, open the customer inspection flow with identical tracking plus the attending personnel for each inspection.

6. **Packing, transportation, and documentation**
   - After every product reaches `Packing`, allow the Production Manager or Workshop Supervisor to arrange transport (route, courier, dates).
   - Provide fields for transporter name, contact person, and phone number along with actions to create invoice, packing list, label, and upload the LR copy.
   - Switch the global order status to `In Transit` while documents are being used.

7. **Installation and completion**
   - When the machines reach the customer site, set the order status to `Installing` and log multiple notes for the planning team.
   - Upon finishing installation, generate a completion letter and advance the order status to `Complete`.

8. **Order documents authoring and printing**
   - Offer editable templates for invoices, purchase orders, and completion letters tied to an order so users can update the content before finalizing.
   - Enable PDF generation, storage, and print dispatch using attached printers, keeping each generated file archived with its parent order record.
