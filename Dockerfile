# ============================================================
# SARA3 - DOCKER IMAGE COMPACTA PARA TESTS HEADLESS
# ============================================================

# STAGE 1: Builder
FROM eclipse-temurin:11-jdk-jammy AS builder
WORKDIR /app
COPY . .

# Instalar Google Chrome stable desde repositorio oficial
RUN apt-get update && apt-get install -y --no-install-recommends \
    wget gnupg git && \
    wget -q -O - https://dl-ssl.google.com/linux/linux_signing_key.pub | apt-key add - && \
    echo "deb [arch=amd64] http://dl.google.com/linux/chrome/deb/ stable main" > /etc/apt/sources.list.d/google-chrome.list && \
    apt-get update && apt-get install -y --no-install-recommends google-chrome-stable && \
    rm -rf /var/lib/apt/lists/* && \
    chmod +x gradlew run-tests-linux.sh && \
    ./gradlew --version && ./gradlew dependencies --write-locks 2>&1 || true

# ============================================================
# STAGE 2: Runtime - ejecutar tests con JDK (no JRE)
FROM eclipse-temurin:11-jdk-jammy
WORKDIR /app

# Instalar Google Chrome stable + dependencias X11
RUN apt-get update && apt-get install -y --no-install-recommends \
    wget gnupg xvfb x11-utils x11-xserver-utils dbus dbus-x11 && \
    wget -q -O - https://dl-ssl.google.com/linux/linux_signing_key.pub | apt-key add - && \
    echo "deb [arch=amd64] http://dl.google.com/linux/chrome/deb/ stable main" > /etc/apt/sources.list.d/google-chrome.list && \
    apt-get update && apt-get install -y --no-install-recommends google-chrome-stable && \
    rm -rf /var/lib/apt/lists/*

# Copiar TODO del builder
COPY --from=builder /app /app

# Copiar scripts de entrada y menú
COPY docker-entrypoint.sh /usr/local/bin/
COPY docker-menu.sh /app/
RUN chmod +x /usr/local/bin/docker-entrypoint.sh /app/docker-menu.sh

# Configurar permisos
RUN chmod +x gradlew run-tests-linux.sh && \
    mkdir -p logs target/reports

# Variables de entorno
ENV DISPLAY=:99 \
    QT_QPA_PLATFORM="offscreen" \
    JAVA_OPTS="-Xmx2048m -Xms512m" \
    CHROME_BIN="/usr/bin/google-chrome" \
    CHROME_DBUS_STUB_ONLY=1 \
    CHROME_HEADLESS=1 \
    DBUS_SYSTEM_BUS_ADDRESS="unix:path=/run/dbus/system_bus_socket"

ENTRYPOINT ["/usr/local/bin/docker-entrypoint.sh"]
CMD []
