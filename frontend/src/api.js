const ORDER_API = process.env.REACT_APP_ORDER_API || "http://localhost:8081/api/orders";
const INVENTORY_API = process.env.REACT_APP_INVENTORY_API || "http://localhost:8082/api/inventory";

export async function fetchInventory() {
  const res = await fetch(INVENTORY_API);
  return res.json();
}

export async function placeOrder(order) {
  const res = await fetch(ORDER_API, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(order),
  });
  if (!res.ok) throw new Error("Failed to place order");
  return res.json();
}

export async function fetchOrders() {
  const res = await fetch(ORDER_API);
  return res.json();
}
