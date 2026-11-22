package cabenobolso.controller;

import cabenobolso.exception.PersistenciaException;
import cabenobolso.exception.ValidacaoException;
import cabenobolso.model.*;
import cabenobolso.persistence.IPersistenciaPlanejamento;
import cabenobolso.persistence.SerializacaoPlanejamento;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class PlanejamentoController {
    private Planejamento planejamento;
    private final IPersistenciaPlanejamento persistencia;

    public PlanejamentoController() {
        try {
            this.planejamento = new Planejamento("Passeio da Turma");
        } catch (ValidacaoException ignored) {}
        this.persistencia = new SerializacaoPlanejamento();
    }

    public Planejamento getPlanejamento() {
        return planejamento;
    }

    public void cadastrarParticipante(int codigo, String nome, BigDecimal orcamento) throws ValidacaoException {
        Participante p = new Participante(codigo, nome, orcamento);
        planejamento.cadastrarParticipante(p);
    }

    public void alterarParticipante(int codigo, String nome, BigDecimal orcamento) throws ValidacaoException {
        planejamento.alterarParticipante(codigo, nome, orcamento);
    }

    public void excluirParticipante(int codigo) throws ValidacaoException {
        planejamento.excluirParticipante(codigo);
    }

    public void adicionarDespesa(String id, String desc, BigDecimal valor, String tipo, 
                                 List<Participante> parts, boolean essencial, int prioridade) 
                                 throws ValidacaoException {
        Despesa d;
        if ("Fixa".equalsIgnoreCase(tipo)) {
            d = new DespesaFixa(id, desc, valor, parts, essencial, prioridade);
        } else if ("Por pessoa".equalsIgnoreCase(tipo)) {
            d = new DespesaPorPessoa(id, desc, valor, parts, essencial, prioridade);
        } else {
            throw new ValidacaoException("Tipo de despesa inválido: " + tipo);
        }
        planejamento.adicionarDespesa(d);
    }

    public void excluirDespesa(String id) throws ValidacaoException {
        planejamento.excluirDespesa(id);
    }

    public void salvarPlanejamento(File file) throws PersistenciaException {
        persistencia.salvar(this.planejamento, file);
    }

    public void carregarPlanejamento(File file) throws PersistenciaException {
        Planejamento carregado = persistencia.carregar(file);
        // Preserva o estado ativo caso a leitura seja corrompida
        this.planejamento = carregado;
    }

    public SimuladorOtimizacao.ResultadoSimulacao executarSimulador() {
        return SimuladorOtimizacao.simularMelhorCenario(this.planejamento);
    }
}