export default function Campo({
  id,
  etiqueta,
  icono,
  type = "text",
  name,
  value,
  onChange,
  placeholder,
  autoComplete,
  inputMode,
  maxLength,
  required,
  accionEtiqueta,
  className = "",
}) {
  return (
    <div className={className}>
      <div className="d-flex justify-content-between align-items-baseline gap-2">
        <label htmlFor={id} className="bt-label">
          {etiqueta}
        </label>
        {accionEtiqueta}
      </div>
      <div className="bt-field">
        {icono && <i className={`bi ${icono}`} aria-hidden="true"></i>}
        <input
          id={id}
          name={name}
          type={type}
          className="form-control"
          value={value}
          onChange={onChange}
          placeholder={placeholder}
          autoComplete={autoComplete}
          inputMode={inputMode}
          maxLength={maxLength}
          required={required}
        />
      </div>
    </div>
  );
}
