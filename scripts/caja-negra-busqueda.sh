#!/bin/bash
# Caja negra de GET /api/viajes (HU 2.1 a 2.3): 5 entradas = origen, destino, fecha,
# pasajeros y franja (horario de salida). Ejecutar con la BD recien cargada (schema.sql + seed.sql).
B=${B:-http://127.0.0.1:8080/api}
C=$(curl -s -X POST $B/auth/login -H 'Content-Type: application/json' -d '{"email":"cliente@bustracka.pe","contrasena":"Cliente1234"}' | sed -E 's/.*"token":"([^"]+)".*/\1/')
MANANA=$(TZ=America/Lima date -d tomorrow +%F); AYER=$(TZ=America/Lima date -d yesterday +%F)
run(){ if [ "$3" = "sin-token" ]; then H="X-Sin-Token: 1"; else H="Authorization: Bearer $C"; fi
  r=$(curl -s -w "\n%{http_code}" "$B/viajes?$2" -H "$H"); code=$(echo "$r" | tail -1); cuerpo=$(echo "$r" | head -n -1)
  if [ "$code" = "200" ]; then res="$(echo "$cuerpo" | grep -o '"idViaje":[0-9]*' | wc -l) viaje(s): $(echo "$cuerpo" | grep -o '"idViaje":[0-9]*' | cut -d: -f2 | tr '\n' ' ')"
  else res=$(echo "$cuerpo" | sed -E 's/.*"message":"([^"]*)".*/\1/' | cut -c1-80); fi
  printf '%-5s HTTP %s  %s\n' "$1" "$code" "$res"; }
q(){ printf 'origen=%s&destino=%s&fecha=%s&pasajeros=%s&franja=%s' "$1" "$2" "$3" "$4" "$5"; }
echo "Busqueda Lima (1) -> Ica (2), fecha de manana = $MANANA"
# Validos
run CP1  "$(q 1 2 $MANANA 2 MANANA)"
run CP2  "$(q 1 2 $MANANA 1 NOCHE)"
run CP3  "$(q 1 2 $MANANA 6 MANANA)"
run CP4  "$(q 1 2 $MANANA 2 TARDE)"
run CP5  "origen=1&destino=2&fecha=$MANANA&pasajeros=2"
run CP6  "$(q 999 2 $MANANA 2 MANANA)"
# origen y destino
run CP7  "$(q abc 2 $MANANA 2 MANANA)"
run CP8  "$(q 1 1 $MANANA 2 MANANA)"
run CP9  "$(q 1 abc $MANANA 2 MANANA)"
# fecha
run CP10 "$(q 1 2 $AYER 2 MANANA)"
run CP11 "$(q 1 2 2026-13-45 2 MANANA)"
# pasajeros
run CP12 "$(q 1 2 $MANANA 0 MANANA)"
run CP13 "$(q 1 2 $MANANA 7 MANANA)"
run CP14 "$(q 1 2 $MANANA abc MANANA)"
run CP15 "$(q 1 2 $MANANA 2.5 MANANA)"
# franja
run CP16 "$(q 1 2 $MANANA 2 MADRUGADA)"
run CP17 "$(q 1 2 $MANANA 2 manana)"
# precondicion
run CP18 "$(q 1 2 $MANANA 2 MANANA)" sin-token
