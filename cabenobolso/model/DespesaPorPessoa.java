package cabenobolso.model;

import cabenobolso.exception.ValidacaoException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class DespesaPorPessoa extends Despesa {
    private static final long serialVersionUID = 1L;

    public DespesaPorPessoa(String id, String descricao, BigDecimal valorUnitario,
                            List<Participante> participantes, boolean essencial, int prioridade) 
                            throws ValidacaoException {
        super(id, descricao, valorUnitario, participantes, essencial, prioridade);
    }

    @Override
    public BigDecimal getValorTotalCalculado() {
        return getValorInformado()
                .multiply(BigDecimal.valueOf(getParticipantes().size()))
                .setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal getValorIndividual(Participante p) {
        if (!getParticipantes().contains(p)) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return getValorInformado().setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String getTipo() {
        return "Por pessoa";
    }
}