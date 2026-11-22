package cabenobolso.model;

import cabenobolso.exception.ValidacaoException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

public class Participante implements Serializable, Comparable<Participante> {
    private static final long serialVersionUID = 1L;

    private final int codigo;
    private String nome;
    private BigDecimal orcamento;

    public Participante(int codigo, String nome, BigDecimal orcamento) throws ValidacaoException {
        if (codigo <= 0) {
            throw new ValidacaoException("O código do participante deve ser um inteiro positivo.");
        }
        this.codigo = codigo;
        setNome(nome);
        setOrcamento(orcamento);
    }

    public int getCodigo() {
        return codigo;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) throws ValidacaoException {
        if (nome == null || nome.trim().isEmpty()) {
            throw new ValidacaoException("O nome do participante não pode ser vazio.");
        }
        this.nome = nome.trim();
    }

    public BigDecimal getOrcamento() {
        return orcamento;
    }

    public void setOrcamento(BigDecimal orcamento) throws ValidacaoException {
        if (orcamento == null || orcamento.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidacaoException("O orçamento disponível deve ser maior ou igual a zero.");
        }
        this.orcamento = orcamento;
    }

    @Override
    public int compareTo(Participante outro) {
        return Integer.compare(this.codigo, outro.codigo);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Participante that = (Participante) o;
        return codigo == that.codigo;
    }

    @Override
    public int hashCode() {
        return Objects.hash(codigo);
    }

    @Override
    public String toString() {
        return codigo + " - " + nome;
    }
}