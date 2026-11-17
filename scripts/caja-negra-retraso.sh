#!/bin/bash
# Caja negra de PUT /api/admin/viajes/{id}/retraso (HU 5.2): 5 entradas = id del viaje,
# minutosRetraso, motivo, detalle y avisarPasajeros. Ejecutar con la BD recien cargada (schema.sql + seed.sql).
B=${B:-http://127.0.0.1:8080/api}
tok(){ curl -s -X POST $B/auth/login -H 'Content-Type: application/json' -d "{\"email\":\"$1\",\"contrasena\":\"$2\"}" | sed -E 's/.*"token":"([^"]+)".*/\1/'; }
A=$(tok admin@bustracka.pe Admin1234); C=$(tok cliente@bustracka.pe Cliente1234)
D10='Lluvia hoy'
D200='Retraso por cierre parcial de la via en el km 45 de la Panamericana Sur debido a obras de mantenimiento programadas por el MTC; el bus avanzara a velocidad reducida hasta pasar la zona de trabajo y lu'
D201='Retraso por cierre parcial de la via en el km 45 de la Panamericana Sur debido a obras de mantenimiento programadas por el MTC; el bus avanzara a velocidad reducida hasta pasar la zona de trabajo y lux'
body(){ printf '{"minutosRetraso":%s,"motivo":%s,"detalle":%s,"avisarPasajeros":%s}' "$1" "$2" "$3" "$4"; }
OK_D='"Congestion en la Panamericana Sur"'
run(){ if [ -n "$3" ]; then H="Authorization: Bearer $3"; else H="X-Sin-Token: 1"; fi
  r=$(curl -s -w "\n%{http_code}" -X PUT "$B/admin/viajes/$2/retraso" -H "$H" -H 'Content-Type: application/json' -d "$4")
  printf '%-5s HTTP %s  %s\n' "$1" "$(echo "$r" | tail -1)" "$(echo "$r" | head -n -1 | sed -E 's/.*"message":"([^"]*)".*/\1/; s/.*"minutosRetraso":([0-9-]+).*/guardado: minutosRetraso=\1/' | cut -c1-80)"; }
# Validos
run CP1  3 "$A" "$(body 30 '"TRAFICO"' "$OK_D" true)"
run CP2  3 "$A" "$(body 0 '"CLIMA"' "\"$D10\"" false)"
run CP3  3 "$A" "$(body 720 '"OTRO"' "\"$D200\"" true)"
# Id del viaje
run CP4  999 "$A" "$(body 30 '"TRAFICO"' "$OK_D" true)"
run CP5  10  "$A" "$(body 30 '"TRAFICO"' "$OK_D" true)"
run CP6  abc "$A" "$(body 30 '"TRAFICO"' "$OK_D" true)"
# minutosRetraso
run CP7  3 "$A" "$(body -1 '"TRAFICO"' "$OK_D" true)"
run CP8  3 "$A" "$(body 721 '"TRAFICO"' "$OK_D" true)"
run CP9  3 "$A" "$(body null '"TRAFICO"' "$OK_D" true)"
run CP10 3 "$A" "$(body 15.5 '"TRAFICO"' "$OK_D" true)"
run CP11 3 "$A" "$(body '"abc"' '"TRAFICO"' "$OK_D" true)"
# motivo
run CP12 3 "$A" "$(body 30 null "$OK_D" true)"
run CP13 3 "$A" "$(body 30 '"LLUVIA"' "$OK_D" true)"
# detalle
run CP14 3 "$A" "$(body 30 '"TRAFICO"' '"          "' true)"
run CP15 3 "$A" "$(body 30 '"TRAFICO"' '"Retraso x"' true)"
run CP16 3 "$A" "$(body 30 '"TRAFICO"' "\"$D201\"" true)"
# avisarPasajeros
run CP17 3 "$A" "$(body 30 '"TRAFICO"' "$OK_D" null)"
run CP18 3 "$A" "$(body 30 '"TRAFICO"' "$OK_D" '"si"')"
# Precondicion: solo el administrador
run CP19 3 "$C" "$(body 30 '"TRAFICO"' "$OK_D" true)"
run CP20 3 ""    "$(body 30 '"TRAFICO"' "$OK_D" true)"