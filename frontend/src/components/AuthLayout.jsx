import Logo from "./Logo";

const TABLERO = [
  ["Elige tu asiento", "Embarque"],
  ["Sigue tu bus", "En vivo"],
  ["Llega tranquilo", "A tiempo"],
];

/** Marco visual comun de Login, Registro y Recuperar contrasena. */
export default function AuthLayout({ anden = "07", eyebrow, titulo, subtitulo, children, pie }) {
  return (
    <div className="bt-auth">
      <aside className="bt-auth-anden">
        <div className="bt-auth-top">
          <Logo to="/login" />
          <span className="bt-tag bt-tag--ink">Terminal</span>
        </div>

        <div className="bt-anden" aria-hidden="true">
          <span className="bt-anden-label">Andén</span>
          <span className="bt-anden-num">{anden}</span>
        </div>

        <ul className="bt-board" aria-hidden="true">
          {TABLERO.map(([texto, estado]) => (
            <li key={texto}>
              <span>{texto}</span>
              <span>{estado}</span>
            </li>
          ))}
        </ul>
      </aside>

      <main className="bt-auth-panel">
        <div className="bt-auth-form">
          {eyebrow && <p className="bt-eyebrow mb-3">{eyebrow}</p>}
          <h1 className="bt-auth-title mb-2">{titulo}</h1>
          {subtitulo && <p className="bt-muted mb-4">{subtitulo}</p>}
          {children}
          {pie && <div className="bt-auth-pie">{pie}</div>}
        </div>

        <p className="bt-auth-legal">© BusTracka · Hecho en el Perú</p>
      </main>
    </div>
  );
}
