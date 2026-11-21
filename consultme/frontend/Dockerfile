# Node 24 (ista glavna verzija kao kod vas lokalno)
FROM node:24-alpine

WORKDIR /app

# Isključujemo pitanje o slanju statistike Angular timu
ENV NG_CLI_ANALYTICS=false

# Prvo samo package fajlovi -> Docker kešira instalirane pakete
COPY package.json package-lock.json ./
RUN npm ci

# Zatim ostatak koda
COPY . .

EXPOSE 4200

# --host 0.0.0.0  -> dev server prihvata konekcije izvan kontejnera
# --poll 2000     -> na Windows-u Docker ne vidi izmene fajlova sam
# --proxy-config  -> /api pozivi idu na backend:8080
CMD ["npx", "ng", "serve", "--host", "0.0.0.0", "--poll", "2000", "--proxy-config", "proxy.conf.json"]