export default function Alerta({ tipo = "error", children }) {
  if (!children) return null;
  const icono = tipo === "exito" ? "bi-check-square-fill" : "bi-exclamation-octagon-fill";

  return (
    <div className={`bt-alert bt-alert--${tipo} mb-3`} role={tipo === "error" ? "alert" : "status"}>
      <i className={`bi ${icono}`} aria-hidden="true"></i>
      <span>{children}</span>
    </div>
  );
}
