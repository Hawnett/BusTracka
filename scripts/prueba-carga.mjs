// Prueba de carga del Definition of Done: 8 usuarios a la vez y el 90% de las
// respuestas en menos de 3 segundos. Uso (con el backend corriendo):
//   node scripts/prueba-carga.mjs [usuarios] [rondas]
// Variables opcionales: API_URL, EMAIL, CONTRASENA.

const API = process.env.API_URL || "http://localhost:8080/api";
const USUARIOS = Number(process.argv[2] || 8);
const RONDAS = Number(process.argv[3] || 20);
const LIMITE_MS = 3000;

async function iniciarSesion() {
  const respuesta = await fetch(`${API}/auth/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      email: process.env.EMAIL || "cliente@bustracka.pe",
      contrasena: process.env.CONTRASENA || "Cliente1234",
    }),
  });
  if (!respuesta.ok) throw new Error(`No se pudo iniciar sesion (${respuesta.status})`);
  return (await respuesta.json()).token;
}

async function medir(ruta, token, tiempos, errores) {
  const inicio = performance.now();
  try {
    const respuesta = await fetch(`${API}${ruta}`, { headers: { Authorization: `Bearer ${token}` } });
    await respuesta.arrayBuffer();
    if (!respuesta.ok) errores.push(`${ruta}: ${respuesta.status}`);
  } catch (e) {
    errores.push(`${ruta}: ${e.message}`);
  }
  tiempos.push(performance.now() - inicio);
}

const percentil = (ordenados, p) => ordenados[Math.min(ordenados.length - 1, Math.ceil((p / 100) * ordenados.length) - 1)];

const token = await iniciarSesion();
const enCurso = await (await fetch(`${API}/viajes/en-curso`, { headers: { Authorization: `Bearer ${token}` } })).json();
const rutas = ["/terminales", "/viajes", "/viajes/en-curso", "/ventas"];
if (enCurso.length > 0) rutas.push(`/seguimiento/${enCurso[0].idViaje}`);

const tiempos = [];
const errores = [];
const inicio = performance.now();
await Promise.all(Array.from({ length: USUARIOS }, async () => {
  for (let ronda = 0; ronda < RONDAS; ronda++) {
    for (const ruta of rutas) {
      await medir(ruta, token, tiempos, errores);
    }
  }
}));
const duracion = (performance.now() - inicio) / 1000;

const ordenados = [...tiempos].sort((a, b) => a - b);
const bajoLimite = ordenados.filter((t) => t < LIMITE_MS).length / ordenados.length;
console.log(`Usuarios concurrentes: ${USUARIOS} · peticiones: ${tiempos.length} en ${duracion.toFixed(1)} s`);
console.log(`p50 ${percentil(ordenados, 50).toFixed(0)} ms · p90 ${percentil(ordenados, 90).toFixed(0)} ms · `
  + `p99 ${percentil(ordenados, 99).toFixed(0)} ms · max ${ordenados[ordenados.length - 1].toFixed(0)} ms`);
console.log(`Respuestas bajo ${LIMITE_MS / 1000} s: ${(bajoLimite * 100).toFixed(1)}% · errores: ${errores.length}`);
if (errores.length > 0) console.log("Primeros errores:", errores.slice(0, 5));

const exito = bajoLimite >= 0.9 && errores.length / tiempos.length <= 0.1;
console.log(exito ? "CUMPLE el Definition of Done" : "NO CUMPLE el Definition of Done");
process.exit(exito ? 0 : 1);
