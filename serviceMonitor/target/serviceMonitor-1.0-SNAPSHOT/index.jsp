<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Service Monitor</title>
    <link rel="stylesheet" href="css/style.css" />
    
    <!-- Chart.js Library -->
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
  </head>
  <body>
    <header class="header">
      <h1>Service Monitor</h1>
    </header>

    <main class="container">
      <!-- 1. Service Status Grid -->
      <section class="services-grid">
        <div class="service-card">
          <h3>Savannah (ZFS)</h3>
          <div id="status-savannah" class="status-box"></div>
        </div>
        <div class="service-card">
          <h3>Utah (Jellyfin)</h3>
          <div id="status-jellyfin" class="status-box"></div>
        </div>
        <div class="service-card">
          <h3>Utah (AdGuard)</h3>
          <div id="status-adguard" class="status-box"></div>
        </div>
      </section>

      <!-- 2. Graphs Section -->
      <section class="graphs-grid">
        <div class="graph-placeholder">
            <canvas id="uptimeChart"></canvas>
        </div>
      </section>

      <!-- 3. Settings Form -->
      <section class="settings-panel">
          <h3>Dashboard Settings</h3>
          <form action="api/settings" method="POST">
              <label class="switch">
                <input type="checkbox" name="notifications" id="notif-toggle">
                <span class="slider"></span>
              </label>
              <span style="font-size: 1.1rem; vertical-align: middle;">Enable Pop-up Notifications</span>
              <br>
              <button type="submit" class="btn-save">Save Settings</button>
          </form>
      </section>
    </main>

    <!-- Notification Toast Element -->
    <div id="notification-toast">Alert: Service Status Changed!</div>

    <script>
      let notificationsEnabled = false;
      let previousStatus = {}; 
      let myChart = null;

      // --- 1. Load Settings on Page Load ---
      document.addEventListener("DOMContentLoaded", function() {
          // Add timestamp to prevent caching
          fetch("${pageContext.request.contextPath}/api/settings?t=" + new Date().getTime())
            .then(res => res.json())
            .then(data => {
                console.log("Settings loaded:", data);
                notificationsEnabled = data.notifications;
                
                // Set the toggle switch state
                const checkbox = document.getElementById("notif-toggle");
                if (checkbox) {
                    checkbox.checked = notificationsEnabled;
                }

                // TRIGGER DEMO NOTIFICATION
                if(notificationsEnabled) {
                    showNotification("System: Notifications are enabled and working!");
                }
            })
            .catch(err => console.error("Error loading settings:", err));
          
          // Start the polling immediately
          updateAllStatuses();
      });

      // --- 2. Main Status Checker ---
      async function checkServiceStatus(serviceUrl, elementId, serviceName) {
        const statusElement = document.getElementById(elementId);
        
        try {
          const response = await fetch("${pageContext.request.contextPath}" + serviceUrl);
          if (!response.ok) throw new Error("Network error");
          const data = await response.json();

          // Update Status Box Color
          const isOnline = (data.status === "online");
          statusElement.className = "status-box " + (isOnline ? "status-online" : "status-offline");
          
          // Trigger Notification if status CHANGED
          if (previousStatus[serviceName] && previousStatus[serviceName] !== data.status) {
              if (notificationsEnabled) {
                  showNotification(serviceName + " is now " + data.status.toUpperCase());
              }
          }
          previousStatus[serviceName] = data.status;

        } catch (error) {
          console.error(error);
          statusElement.className = "status-box status-offline";
        }
      }

      function showNotification(msg) {
          const x = document.getElementById("notification-toast");
          x.innerText = msg;
          x.className = "show";
          // After 3 seconds, remove the show class
          setTimeout(function(){ x.className = x.className.replace("show", ""); }, 3000);
      }

      function updateAllStatuses() {
        checkServiceStatus("/status/savannah", "status-savannah", "Savannah");
        checkServiceStatus("/status/jellyfin", "status-jellyfin", "Jellyfin");
        checkServiceStatus("/status/adguard", "status-adguard", "AdGuard");
        updateChart(); 
      }
      
      // Poll every 5 seconds
      setInterval(updateAllStatuses, 5000);

      // --- 3. Graph Logic ---
      async function updateChart() {
          try {
              const response = await fetch("${pageContext.request.contextPath}/api/history");
              const data = await response.json();
              
              if (!data || data.length === 0) {
                  console.log("No data for chart yet...");
                  return;
              }

              // Parse data
              const labels = data.map(d => d.time.split(" ")[1] || d.time).reverse(); 
              const values = data.map(d => d.status === 'online' ? 1 : 0).reverse();

              const ctx = document.getElementById('uptimeChart').getContext('2d');

              if (myChart) {
                  myChart.data.labels = labels;
                  myChart.data.datasets[0].data = values;
                  myChart.update();
              } else {
                  myChart = new Chart(ctx, {
                      type: 'line',
                      data: {
                          labels: labels,
                          datasets: [{
                              label: 'AdGuard Uptime History',
                              data: values,
                              borderColor: '#859900', 
                              backgroundColor: 'rgba(133, 153, 0, 0.2)',
                              borderWidth: 2,
                              stepped: true, 
                              fill: true,
                              pointRadius: 0 // Hide dots for cleaner look
                          }]
                      },
                      options: {
                          responsive: true,
                          maintainAspectRatio: false,
                          scales: {
                              y: {
                                  min: 0,
                                  max: 1.1, // Slight padding at top
                                  ticks: {
                                      stepSize: 1,
                                      callback: function(value) {
                                          if(value === 1) return 'Online';
                                          if(value === 0) return 'Offline';
                                          return '';
                                      }
                                  }
                              }
                          },
                          animation: false 
                      }
                  });
              }
          } catch (e) {
              console.error("Graph update error:", e);
          }
      }
    </script>
  </body>
</html>