# ER diagrams per task

One ER diagram for each task, showing only the entities that task works with.

| Task | Diagram | What it shows |
|------|---------|---------------|
| 1 · Product Catalogue | [task-1-product-catalogue.png](task-1-product-catalogue.png) | PRODUCT (Plant / Planter / Gardening Kit by category) |
| 2 · Clients | [task-2-clients.png](task-2-clients.png) | CLIENT (Household or Office) |
| 3 · Stock / Inventory | [task-3-stock.png](task-3-stock.png) | PRODUCT and its STOCK_ITEM record |
| 4 · Delivery Agents | [task-4-delivery-agents.png](task-4-delivery-agents.png) | DELIVERY_AGENT and vehicle details |
| 5 · Process Orders | [task-5-process-orders.png](task-5-process-orders.png) | CLIENT places ORDERS, which contain ORDER_ITEMs of PRODUCTs (stock reduced) |
| 6 · Assign Delivery | [task-6-assign-delivery.png](task-6-assign-delivery.png) | ORDERS linked to a CLIENT and an assigned DELIVERY_AGENT |
| 7 · Monthly Reports | [task-7-monthly-reports.png](task-7-monthly-reports.png) | ORDERS, ORDER_ITEM, PRODUCT and STOCK_ITEM read for revenue and low-stock |
| 8 · Client Dispatch Email | [task-8-client-dispatch-email.png](task-8-client-dispatch-email.png) | EMAIL sent to the CLIENT when their order is dispatched |
| 9 · Agent Assign Email | [task-9-agent-assign-email.png](task-9-agent-assign-email.png) | EMAIL sent to the DELIVERY_AGENT when a delivery is assigned |

