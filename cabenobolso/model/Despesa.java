package cabenobolso.model;

import cabenobolso.exception.ValidacaoException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;

public abstract class Despesa implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String id;
    private String descricao;
    private BigDecimal valorInformado;
    private final List<Participante> participantes;
    private boolean essencial;
    private int prioridade;

    public Despesa(String id, String descricao, BigDecimal valorInformado, 
                   List<Participante> participantes, boolean essencial, int prioridade) throws ValidacaoException {
        if (id == null || id.trim().isEmpty()) {
            throw new ValidacaoException("Identificador da despesa inválido.");
        }
        if (descricao == null || descricao.trim().isEmpty()) {
            throw new ValidacaoException("A descrição da despesa não pode estar vazia.");
        }
        if (valorInformado == null || valorInformado.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidacaoException("O valor informado da despesa deve ser estritamente maior que zero.");
        }
        if (participantes == null || participantes.isEmpty()) {
            throw new ValidacaoException("Cada despesa deve conter pelo menos um participante vinculado.");
        }

        // Garantir unicidade sem repetições de participantes
        Set<Participante> semDuplicados = new LinkedHashSet<>(participantes);
        if (semDuplicados.size() != participantes.size()) {
            throw new ValidacaoException("A despesa não pode conter participantes duplicados.");
        }

        if (!essencial && prioridade <= 0) {
            throw new ValidacaoException("Despesas opcionais exigem prioridade inteira positiva.");
        }

        this.id = id.trim();
        this.descricao = descricao.trim();
        this.valorInformado = valorInformado;
        // Ordena participantes pelo código para aplicação do algoritmo de rateio de centavos
        List<Participante> ordenados = new ArrayList<>(semDuplicados);
        Collections.sort(ordenados);
        this.participantes = Collections.unmodifiableList(ordenados);
        this.essencial = essencial;
        this.prioridade = essencial ? 0 : prioridade;
    }

    public String getId() { return id; }
    public String getDescricao() { return descricao; }
    public BigDecimal getValorInformado() { return valorInformado; }
    public List<Participante> getParticipantes() { return participantes; }
    public boolean isEssencial() { return essencial; }
    public int getPrioridade() { return prioridade; }

    public abstract BigDecimal getValorTotalCalculado();
    public abstract BigDecimal getValorIndividual(Participante p);
    public abstract String getTipo();
}