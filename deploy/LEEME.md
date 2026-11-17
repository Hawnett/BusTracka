# Despliegue en VPS (Ubuntu 24.04, 4 GB RAM)

1. En el VPS, como root:
   ```bash
   curl -fsSL https://raw.githubusercontent.com/Hawnett/BusTracka/deploy/vps/deploy/instalar.sh \
     | REPO_URL=https://github.com/Hawnett/BusTracka.git BRANCH=deploy/vps bash
   ```
   (Repo privado: clonar antes con un token de solo lectura y correr `bash deploy/instalar.sh`.)
2. Abrir `https://<IP>.sslip.io` (lo imprime el script). Correos de prueba: `/mailpit/`.
3. Actualizar tras un cambio: `cd /opt/bustracka && git pull && bash deploy/instalar.sh`.
4. Ver logs: `cd /opt/bustracka/deploy && docker compose logs -f backend`.
5. Reiniciar de cero (borra datos): `docker compose down -v && bash instalar.sh`.

Los secretos (`DB_PASSWORD`, `JWT_SECRET`) se generan en `deploy/.env` dentro del VPS y no se suben a git.
