package cabenobolso.model;

import cabenobolso.exception.ValidacaoException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class SimuladorOtimizacao {

    public static class ResultadoSimulacao {
        private final boolean solucaoEncontrada;
        private final String mensagemStatus;
        private final List<Despesa> despesasMantidas;
        private final List<Despesa> despesasRemovidas;
        private final Map<Participante, BigDecimal> novosTotaisIndividuais;
        private final BigDecimal novoCustoTotal;
        private final int pontuacaoPrioridadeTotal;

        public ResultadoSimulacao(boolean solucaoEncontrada, String mensagemStatus,
                                  List<Despesa> mantidas, List<Despesa> removidas,
                                  Map<Participante, BigDecimal> totais, BigDecimal total, int pontuacao) {
            this.solucaoEncontrada = solucaoEncontrada;
            this.mensagemStatus = mensagemStatus;
            this.despesasMantidas = mantidas;
            this.despesasRemovidas = removidas;
            this.novosTotaisIndividuais = totais;
            this.novoCustoTotal = total;
            this.pontuacaoPrioridadeTotal = pontuacao;
        }

        public boolean isSolucaoEncontrada() { return solucaoEncontrada; }
        public String getMensagemStatus() { return mensagemStatus; }
        public List<Despesa> getDespesasMantidas() { return despesasMantidas; }
        public List<Despesa> getDespesasRemovidas() { return despesasRemovidas; }
        public Map<Participante, BigDecimal> getNovosTotaisIndividuais() { return novosTotaisIndividuais; }
        public BigDecimal getNovoCustoTotal() { return novoCustoTotal; }
        public int getPontuacaoPrioridadeTotal() { return pontuacaoPrioridadeTotal; }
    }

    public static ResultadoSimulacao simularMelhorCenario(Planejamento original) {
        List<Despesa> essenciais = new ArrayList<>();
        List<Despesa> opcionais = new ArrayList<>();

        for (Despesa d : original.getDespesas()) {
            if (d.isEssencial()) essenciais.add(d);
            else opcionais.add(d);
        }

        if (opcionais.size() > 10) {
            return new ResultadoSimulacao(false, "Simulador suporta no máximo 10 despesas opcionais.", 
                    Collections.emptyList(), Collections.emptyList(), Collections.emptyMap(), BigDecimal.ZERO, 0);
        }

        // Testar viabilidade das despesas essenciais
        Map<Participante, BigDecimal> custoEssencial = new HashMap<>();
        for (Participante p : original.getParticipantes()) {
            BigDecimal soma = BigDecimal.ZERO;
            for (Despesa d : essenciais) {
                soma = soma.add(d.getValorIndividual(p));
            }
            custoEssencial.put(p, soma);
            if (soma.compareTo(p.getOrcamento()) > 0) {
                BigDecimal excesso = soma.subtract(p.getOrcamento());
                return new ResultadoSimulacao(false, 
                        "Sem solução: despesas essenciais excedem orçamento de " + p.getNome() + " em R$ " + excesso,
                        Collections.emptyList(), Collections.emptyList(), Collections.emptyMap(), BigDecimal.ZERO, 0);
            }
        }

        // Busca exaustiva de combinações de opcionais
        int maxPrioridade = -1;
        List<Despesa> melhorCombOpcionais = null;
        int totalSubconjuntos = 1 << opcionais.size();

        for (int mask = 0; mask < totalSubconjuntos; mask++) {
            List<Despesa> candidatas = new ArrayList<>();
            int prioridadeAtual = 0;

            for (int i = 0; i < opcionais.size(); i++) {
                if ((mask & (1 << i)) != 0) {
                    Despesa opc = opcionais.get(i);
                    candidatas.add(opc);
                    prioridadeAtual += opc.getPrioridade();
                }
            }

            // Validar viabilidade orçamentária de cada participante
            boolean viavel = true;
            for (Participante p : original.getParticipantes()) {
                BigDecimal totalP = custoEssencial.get(p);
                for (Despesa opc : candidatas) {
                    totalP = totalP.add(opc.getValorIndividual(p));
                }
                if (totalP.compareTo(p.getOrcamento()) > 0) {
                    viavel = false;
                    break;
                }
            }

            if (viavel && prioridadeAtual > maxPrioridade) {
                maxPrioridade = prioridadeAtual;
                melhorCombOpcionais = candidatas;
            }
        }

        if (melhorCombOpcionais == null) {
            return new ResultadoSimulacao(false, "Nenhuma combinação de opcionais respeita o orçamento.",
                    Collections.emptyList(), Collections.emptyList(), Collections.emptyMap(), BigDecimal.ZERO, 0);
        }

        // Montar resultado final
        List<Despesa> mantidas = new ArrayList<>(essenciais);
        mantidas.addAll(melhorCombOpcionais);

        List<Despesa> removidas = new ArrayList<>(opcionais);
        removidas.removeAll(melhorCombOpcionais);

        Map<Participante, BigDecimal> totaisFinais = new LinkedHashMap<>();
        BigDecimal custoTotalGeral = BigDecimal.ZERO;

        for (Participante p : original.getParticipantes()) {
            BigDecimal totalP = BigDecimal.ZERO;
            for (Despesa d : mantidas) {
                totalP = totalP.add(d.getValorIndividual(p));
            }
            totaisFinais.put(p, totalP.setScale(2, RoundingMode.HALF_UP));
        }

        for (Despesa d : mantidas) {
            custoTotalGeral = custoTotalGeral.add(d.getValorTotalCalculado());
        }

        return new ResultadoSimulacao(true, "Solução ótima encontrada com prioridade somada: " + maxPrioridade,
                mantidas, removidas, totaisFinais, custoTotalGeral.setScale(2, RoundingMode.HALF_UP), maxPrioridade);
    }
}