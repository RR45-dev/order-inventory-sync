import React, { useEffect, useState } from "react";
import { fetchInventory, placeOrder, fetchOrders } from "./api";

export default function App() {
  const [inventory, setInventory] = useState([]);
  const [orders, setOrders] = useState([]);
  const [sku, setSku] = useState("");
  const [quantity, setQuantity] = useState(1);
  const [customerId, setCustomerId] = useState("cust-demo");
  const [message, setMessage] = useState("");

  const load = () => {
    fetchInventory().then(setInventory).catch(() => {});
    fetchOrders().then(setOrders).catch(() => {});
  };

  useEffect(() => {
    load();
    const interval = setInterval(load, 4000); // poll so order/inventory status updates as Kafka events flow
    return () => clearInterval(interval);
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      await placeOrder({
        customerId,
        channel: "WEB",
        items: [{ sku, quantity: Number(quantity) }],
      });
      setMessage("Order submitted — check status below in a few seconds.");
      load();
    } catch (err) {
      setMessage("Failed to submit order: " + err.message);
    }
  };

  return (
    <div style={{ fontFamily: "sans-serif", maxWidth: 720, margin: "40px auto" }}>
      <h1>StockSync</h1>
      <p>Place an order and watch inventory + order status sync across services via Kafka.</p>

      <h2>Current Inventory</h2>
      <table border="1" cellPadding="6" style={{ borderCollapse: "collapse", width: "100%" }}>
        <thead><tr><th>SKU</th><th>Description</th><th>Qty on hand</th></tr></thead>
        <tbody>
          {inventory.map((item) => (
            <tr key={item.sku}>
              <td>{item.sku}</td><td>{item.description}</td><td>{item.quantityOnHand}</td>
            </tr>
          ))}
        </tbody>
      </table>

      <h2>Place Order</h2>
      <form onSubmit={handleSubmit}>
        <input placeholder="Customer ID" value={customerId} onChange={(e) => setCustomerId(e.target.value)} />
        <input placeholder="SKU" value={sku} onChange={(e) => setSku(e.target.value)} required />
        <input type="number" min="1" value={quantity} onChange={(e) => setQuantity(e.target.value)} />
        <button type="submit">Place Order</button>
      </form>
      {message && <p>{message}</p>}

      <h2>Recent Orders</h2>
      <table border="1" cellPadding="6" style={{ borderCollapse: "collapse", width: "100%" }}>
        <thead><tr><th>ID</th><th>Customer</th><th>Status</th></tr></thead>
        <tbody>
          {orders.map((o) => (
            <tr key={o.id}><td>{o.id}</td><td>{o.customerId}</td><td>{o.status}</td></tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
