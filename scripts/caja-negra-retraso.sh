#!/bin/bash
# Casos de caja negra de PUT /api/admin/viajes/{id}/retraso
B=${B:-http://127.0.0.1:8080/api}
tok(){ curl -s -X POST $B/auth/login -H 'Content-Type: application/json' -d "{\"email\":\"$1\",\"contrasena\":\"$2\"}" | sed -E 's/.*"token":"([^"]+)".*/\1/'; }
A=$(tok admin@bustracka.pe Admin1234); C=$(tok cliente@bustracka.pe Cliente1234)
run(){ if [ -n "$3" ]; then H="Authorization: Bearer $3"; else H="X-Sin-Token: 1"; fi
  r=$(curl -s -w "\n%{http_code}" -X PUT "$B/admin/viajes/$2/retraso" -H "$H" -H 'Content-Type: application/json' -d "$4")
  echo "$1 | id=$2 | $4 | HTTP $(echo "$r" | tail -1) | $(echo "$r" | head -n -1 | sed -E 's/.*"message":"([^"]*)".*/\1/; s/.*"minutosRetraso":([0-9-]+).*/viaje actualizado, minutosRetraso=\1/' | cut -c1-90)"; }
run CP1 3 "$A" '{"minutosRetraso":30}'
run CP2 3 "$A" '{"minutosRetraso":0}'
run CP3 3 "$A" '{"minutosRetraso":720}'
run CP4 3 "$A" '{"minutosRetraso":-1}'
run CP5 3 "$A" '{"minutosRetraso":721}'
run CP6 3 "$A" '{}'
run CP7 3 "$A" '{"minutosRetraso":"abc"}'
run CP8 3 "$A" '{"minutosRetraso":15.5}'
run CP9 999 "$A" '{"minutosRetraso":30}'
run CP10 10 "$A" '{"minutosRetraso":30}'
run CP11 abc "$A" '{"minutosRetraso":30}'
run CP12 3 "$C" '{"minutosRetraso":30}'
run CP13 3 "" '{"minutosRetraso":30}'
run CP14 3 "xxx.yyy.zzz" '{"minutosRetraso":30}'