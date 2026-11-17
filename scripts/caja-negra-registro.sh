#!/bin/bash
# Caja negra de POST /api/auth/register (HU 1.1): 5 entradas = nombreUsuario, email,
# contrasena, confirmarContrasena y celular. Ejecutar con la BD recien cargada (schema.sql + seed.sql).
B=${B:-http://127.0.0.1:8080/api}
N100=$(printf 'a%.0s' $(seq 1 100)); N101=${N100}b
body(){ printf '{"nombreUsuario":%s,"email":%s,"contrasena":%s,"confirmarContrasena":%s,"celular":%s}' "$1" "$2" "$3" "$4" "$5"; }
run(){ r=$(curl -s -m 15 -w "\n%{http_code}" -X POST "$B/auth/register" -H 'Content-Type: application/json' -d "$2")
  printf '%-5s HTTP %s  %s\n' "$1" "$(echo "$r" | tail -1)" "$(echo "$r" | sed "\$d" | sed -E 's/.*"message":"([^"]*)".*/\1/; s/.*"email":"([^"]*)".*/cuenta creada: \1/' | cut -c1-90)"; }
P='"Secreta123"'
# Validos
run CP1  "$(body '"mariaq"' '"maria.q@test.pe"' $P $P '"987654321"')"
run CP2  "$(body '"ana"' '"ana.l@test.pe"' '"Abcd1234"' '"Abcd1234"' '"900000000"')"
run CP3  "$(body "\"$N100\"" '"largo@test.pe"' $P $P '"999999999"')"
# nombreUsuario
run CP4  "$(body '"an"' '"cp4@test.pe"' $P $P '"987654321"')"
run CP5  "$(body "\"$N101\"" '"cp5@test.pe"' $P $P '"987654321"')"
run CP6  "$(body '"   "' '"cp6@test.pe"' $P $P '"987654321"')"
# email
run CP7  "$(body '"mariaq"' 'null' $P $P '"987654321"')"
run CP8  "$(body '"mariaq"' '"maria.test.pe"' $P $P '"987654321"')"
run CP9  "$(body '"mariaq"' '"cliente@bustracka.pe"' $P $P '"987654321"')"
# contrasena
run CP10 "$(body '"mariaq"' '"cp10@test.pe"' '"Abc1234"' '"Abc1234"' '"987654321"')"
run CP11 "$(body '"mariaq"' '"cp11@test.pe"' 'null' $P '"987654321"')"
# confirmarContrasena
run CP12 "$(body '"mariaq"' '"cp12@test.pe"' $P '"Secreta124"' '"987654321"')"
run CP13 "$(body '"mariaq"' '"cp13@test.pe"' $P 'null' '"987654321"')"
# celular
run CP14 "$(body '"mariaq"' '"cp14@test.pe"' $P $P '"98765432"')"
run CP15 "$(body '"mariaq"' '"cp15@test.pe"' $P $P '"9876543210"')"
run CP16 "$(body '"mariaq"' '"cp16@test.pe"' $P $P '"887654321"')"
run CP17 "$(body '"mariaq"' '"cp17@test.pe"' $P $P '"98765432a"')"
run CP18 "$(body '"mariaq"' '"cp18@test.pe"' $P $P 'null')"
