#!/usr/bin/env bash
# Uso (en el VPS Ubuntu, como root):
#   curl -fsSL https://raw.githubusercontent.com/Hawnett/BusTracka/deploy/vps/deploy/instalar.sh | REPO_URL=https://github.com/Hawnett/BusTracka.git BRANCH=deploy/vps bash
# o, si ya clonaste el repo:  cd BusTracka/deploy && bash instalar.sh
set -euo pipefail

REPO_URL="${REPO_URL:-https://github.com/Hawnett/BusTracka.git}"
BRANCH="${BRANCH:-deploy/vps}"
DESTINO="${DESTINO:-/opt/bustracka}"

# 1) Docker
if ! command -v docker >/dev/null 2>&1; then
  curl -fsSL https://get.docker.com | sh
fi

# 2) Codigo
if [ -d "$DESTINO/.git" ]; then
  git -C "$DESTINO" fetch origin "$BRANCH" && git -C "$DESTINO" checkout "$BRANCH" && git -C "$DESTINO" pull origin "$BRANCH"
elif [ -f "$(dirname "$0")/docker-compose.yml" ] && [ -d "$(dirname "$0")/../backend" ]; then
  DESTINO="$(cd "$(dirname "$0")/.." && pwd)"
else
  git clone --branch "$BRANCH" "$REPO_URL" "$DESTINO"
fi
cd "$DESTINO/deploy"

# 3) .env (secretos solo en el VPS)
if [ ! -f .env ]; then
  IP="$(curl -fsS https://api.ipify.org)"
  DOMINIO="${DOMINIO:-${IP}.sslip.io}"
  {
    echo "DOMINIO=$DOMINIO"
    echo "DB_PASSWORD=$(openssl rand -hex 16)"
    echo "JWT_SECRET=$(openssl rand -base64 48 | tr -d '\n')"
  } > .env
  chmod 600 .env
fi
set -a; . ./.env; set +a

# 4) Flujos de n8n con la URL publica en los botones de los correos
rm -rf n8n-import && cp -r ../backend/n8n n8n-import
sed -i "s#http://localhost:5173#https://${DOMINIO}#g" "n8n-import/Notificaciones de viaje.json"
sed -i "s#\"password\": \"bustracka_pass\"#\"password\": \"${DB_PASSWORD}\"#" n8n-import/credenciales.ejemplo.json

# 5) Levantar todo
docker compose up -d --build

# 6) Importar y activar flujos de n8n (una sola vez)
sleep 20
docker compose exec -T n8n n8n import:credentials --input=/import/credenciales.ejemplo.json || true
docker compose exec -T n8n n8n import:workflow --input="/import/Notificaciones de viaje.json" || true
docker compose exec -T n8n n8n import:workflow --input="/import/Correo de comprobante.json" || true
docker compose exec -T n8n n8n publish:workflow --id=Td5CDjuknCy3qTHi || docker compose exec -T n8n n8n update:workflow --id=Td5CDjuknCy3qTHi --active=true || true
docker compose exec -T n8n n8n publish:workflow --id=yaPhSo4h7jEUz83n || docker compose exec -T n8n n8n update:workflow --id=yaPhSo4h7jEUz83n --active=true || true
docker compose restart n8n

echo
echo "Listo:  https://${DOMINIO}"
echo "Correos de prueba (Mailpit):  https://${DOMINIO}/mailpit/"
echo "Usuarios: cliente@bustracka.pe / Cliente1234   |   admin@bustracka.pe / Admin1234"
