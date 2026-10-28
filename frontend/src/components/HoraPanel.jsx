export default function HoraPanel({ hora = "", tamano }) {
  const caracteres = String(hora).split("");

  return (
    <span className={`bt-hora ${tamano === "sm" ? "bt-hora--sm" : ""}`}>
      <span className="visually-hidden">{hora}</span>
      {caracteres.map((caracter, i) => (
        <span key={i} className={caracter === ":" ? "is-sep" : ""} aria-hidden="true">
          {caracter}
        </span>
      ))}
    </span>
  );
}
