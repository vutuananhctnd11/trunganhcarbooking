// Service Worker cho Web Push Notification - Trung Anh Car
self.addEventListener('push', function(event) {
    if (!event.data) return;

    try {
        const data = event.data.json();
        const options = {
            body: data.body || '',
            icon: '/favicon.ico',
            badge: '/favicon.ico',
            vibrate: [200, 100, 200],
            data: {
                url: data.url || '/'
            }
        };

        event.waitUntil(
            self.registration.showNotification(data.title || 'Trung Anh Car', options)
        );
    } catch (e) {
        // Fallback nếu payload là text thuần
        const text = event.data.text();
        event.waitUntil(
            self.registration.showNotification('Trung Anh Car', {
                body: text
            })
        );
    }
});

self.addEventListener('notificationclick', function(event) {
    event.notification.close();
    const targetUrl = event.notification.data && event.notification.data.url ? event.notification.data.url : '/';
    
    event.waitUntil(
        clients.matchAll({ type: 'window', includeUncontrolled: true }).then(function(clientList) {
            for (let i = 0; i < clientList.length; i++) {
                const client = clientList[i];
                if (client.url === targetUrl && 'focus' in client) {
                    return client.focus();
                }
            }
            if (clients.openWindow) {
                return clients.openWindow(targetUrl);
            }
        })
    );
});

