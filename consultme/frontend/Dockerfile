# Node 24 (ista glavna verzija kao kod vas lokalno)
FROM node:24-alpine

WORKDIR /app

# Isključujemo pitanje o slanju statistike Angular timu (inače bi kontejner čekao odgovor)
ENV NG_CLI_ANALYTICS=false

# Prvo samo package fajlovi -> Docker kešira instalirane pakete,
# pa se npm ci ponovo pokreće samo kad se promeni package.json / package-lock.json
COPY package.json package-lock.json ./
RUN npm ci

# Zatim ostatak koda
COPY . .

EXPOSE 4200

# --host 0.0.0.0  -> dev server prihvata konekcije izvan kontejnera
# --poll 2000     -> na Windows-u Docker ne vidi izmene fajlova sam, pa proverava na 2 sekunde
CMD ["npx", "ng", "serve", "--host", "0.0.0.0", "--poll", "2000"]
