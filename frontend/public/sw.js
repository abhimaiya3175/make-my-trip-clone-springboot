self.addEventListener("push", (event) => {
  if (!event.data) {
    return;
  }

  let payload = {
    title: "Flight Status Update",
    body: "Your tracked flight status changed.",
    flightId: "",
  };

  try {
    payload = event.data.json();
  } catch (error) {
    payload.body = event.data.text();
  }

  const notificationOptions = {
    body: payload.body,
    icon: "/favicon.ico",
    badge: "/favicon.ico",
    data: {
      flightId: payload.flightId || "",
    },
  };

  event.waitUntil(
    Promise.all([
      self.registration.showNotification(payload.title, notificationOptions),
      clients.matchAll({ type: "window", includeUncontrolled: true }).then((clientList) => {
        clientList.forEach((client) => {
          client.postMessage({
            type: "FLIGHT_STATUS_PUSH",
            payload,
          });
        });
      }),
    ])
  );
});

self.addEventListener("notificationclick", (event) => {
  event.notification.close();
  const flightId = event.notification.data && event.notification.data.flightId;
  const targetUrl = flightId ? `/flight-status?flightId=${encodeURIComponent(flightId)}` : "/flight-status";

  event.waitUntil(
    clients.matchAll({ type: "window", includeUncontrolled: true }).then((clientList) => {
      for (const client of clientList) {
        if ("focus" in client) {
          client.navigate(targetUrl);
          return client.focus();
        }
      }
      if (clients.openWindow) {
        return clients.openWindow(targetUrl);
      }
      return null;
    })
  );
});
