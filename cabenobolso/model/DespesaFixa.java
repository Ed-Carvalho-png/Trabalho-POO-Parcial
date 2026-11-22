package cabenobolso.model;

import cabenobolso.exception.ValidacaoException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class DespesaFixa extends Despesa {
    private static final long serialVersionUID = 1L;

    public DespesaFixa(String id, String descricao, BigDecimal valorTotal,
                       List<Participante> participantes, boolean essencial, int prioridade) 
                       throws ValidacaoException {
        super(id, descricao, valorTotal, participantes, essencial, prioridade);
    }

    @Override
    public BigDecimal getValorTotalCalculado() {
        return getValorInformado();
    }

    @Override
    public BigDecimal getValorIndividual(Participante p) {
        int indice = getParticipantes().indexOf(p);
        if (indice < 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        // Conversão do valor total em centavos inteiros
        long centavosTotais = getValorInformado()
                .multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP)
                .longValue();

        int n = getParticipantes().size();
        long quocienteCentavos = centavosTotais / n;
        long restoCentavos = centavosTotais % n;

        // Distribuição dos centavos excedentes conforme ordem crescente dos códigos
        long centavosPessoa = quocienteCentavos + (indice < restoCentavos ? 1 : 0);

        return BigDecimal.valueOf(centavosPessoa, 2);
    }

    @Override
    public String getTipo() {
        return "Fixa";
    }
}