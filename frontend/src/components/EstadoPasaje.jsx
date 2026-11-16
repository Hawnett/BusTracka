import { ESTADOS_PASAJE } from "../utils/formato";

export default function EstadoPasaje({ estado }) {
  const { texto, clase } = ESTADOS_PASAJE[estado] || { texto: estado, clase: "" };
  return <span className={`bt-estado ${clase}`}>{texto}</span>;
}
