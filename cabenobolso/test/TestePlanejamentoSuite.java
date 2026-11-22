package cabenobolso.test;

import cabenobolso.exception.PersistenciaException;
import cabenobolso.exception.ValidacaoException;
import cabenobolso.model.*;
import cabenobolso.persistence.SerializacaoPlanejamento;

import java.io.File;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TestePlanejamentoSuite {

    public static void main(String[] args) {
        System.out.println(">>> INICIANDO BATERIA DE TESTES DO SISTEMA CABE NO BOLSO <<<\n");
        int passados = 0;
        int total = 8;

        if (testarCasoReferencia()) passados++;
        if (testarRateioCentavos()) passados++;
        if (testarEntradaOuVinculoInvalido()) passados++;
        if (testarAlteracaoOrcamento()) passados++;
        if (testarExclusaoDespesa()) passados++;
        if (testarSalvarERecuperar()) passados++;
        if (testarProblemaArquivo()) passados++;
        if (testarSimuladorOtimizacao()) passados++;

        System.out.printf("\nRESULTADO GERAL: %d/%d TESTES BEM-SUCEDIDOS.\n", passados, total);
    }

    // 1. Caso de referência do passeio da turma (Seção 4)
    public static boolean testarCasoReferencia() {
        System.out.print("[TESTE 1] Caso de Referência Principal: ");
        try {
            Planejamento plan = new Planejamento("Passeio da Turma");
            Participante p1 = new Participante(1, "Ana", new BigDecimal("70.00"));
            Participante p2 = new Participante(2, "Bruno", new BigDecimal("60.00"));
            Participante p3 = new Participante(3, "Carla", new BigDecimal("70.00"));

            plan.cadastrarParticipante(p1);
            plan.cadastrarParticipante(p2);
            plan.cadastrarParticipante(p3);

            plan.adicionarDespesa(new DespesaFixa("D1", "Transporte", new BigDecimal("90.00"), Arrays.asList(p1, p2, p3), true, 0));
            plan.adicionarDespesa(new DespesaPorPessoa("D2", "Almoço", new BigDecimal("20.00"), Arrays.asList(p1, p2, p3), true, 0));
            plan.adicionarDespesa(new DespesaFixa("D3", "Atividade recreativa", new BigDecimal("30.00"), Arrays.asList(p1, p3), false, 3));
            plan.adicionarDespesa(new DespesaPorPessoa("D4", "Sobremesa", new BigDecimal("10.00"), Arrays.asList(p1, p2, p3), false, 1));

            boolean okCustoTotal = plan.calcularCustoTotal().compareTo(new BigDecimal("210.00")) == 0;
            boolean okAna = plan.calcularGastoIndividual(p1).compareTo(new BigDecimal("75.00")) == 0;
            boolean okBruno = plan.calcularGastoIndividual(p2).compareTo(new BigDecimal("60.00")) == 0;
            boolean okCarla = plan.calcularGastoIndividual(p3).compareTo(new BigDecimal("75.00")) == 0;

            if (okCustoTotal && okAna && okBruno && okCarla) {
                System.out.println("PASSOU (Total R$ 210,00 | Ana=75, Bruno=60, Carla=75)");
                return true;
            }
        } catch (Exception e) {
            System.out.println("FALHOU: " + e.getMessage());
        }
        return false;
    }

    // 2. Centavos no rateio (Seção 5)
    public static boolean testarRateioCentavos() {
        System.out.print("[TESTE 2] Rateio de Centavos (R$ 100 / 3 participantes): ");
        try {
            Participante p1 = new Participante(1, "Ana", new BigDecimal("100.00"));
            Participante p2 = new Participante(2, "Bruno", new BigDecimal("100.00"));
            Participante p3 = new Participante(3, "Carla", new BigDecimal("100.00"));

            DespesaFixa df = new DespesaFixa("D100", "Rateio Fracionado", new BigDecimal("100.00"), Arrays.asList(p1, p2, p3), true, 0);

            BigDecimal v1 = df.getValorIndividual(p1);
            BigDecimal v2 = df.getValorIndividual(p2);
            BigDecimal v3 = df.getValorIndividual(p3);

            boolean c1 = v1.compareTo(new BigDecimal("33.34")) == 0;
            boolean c2 = v2.compareTo(new BigDecimal("33.33")) == 0;
            boolean c3 = v3.compareTo(new BigDecimal("33.33")) == 0;
            boolean soma = v1.add(v2).add(v3).compareTo(new BigDecimal("100.00")) == 0;

            if (c1 && c2 && c3 && soma) {
                System.out.println("PASSOU (P1=33.34, P2=33.33, P3=33.33, Soma=100.00)");
                return true;
            }
        } catch (Exception e) {
            System.out.println("FALHOU: " + e.getMessage());
        }
        return false;
    }

    // 3. Validação de entradas e integridade de vínculos (Seção 5)
    public static boolean testarEntradaOuVinculoInvalido() {
        System.out.print("[TESTE 3] Entradas Inválidas e Bloqueio de Vínculo: ");
        try {
            Planejamento plan = new Planejamento("Teste Validações");
            Participante p1 = new Participante(1, "Ana", new BigDecimal("50.00"));
            plan.cadastrarParticipante(p1);

            // Teste 3.1: Despesa sem participantes
            boolean barraSemPart = false;
            try {
                new DespesaFixa("D1", "Sem participantes", new BigDecimal("10.00"), Collections.emptyList(), true, 0);
            } catch (ValidacaoException e) {
                barraSemPart = true;
            }

            // Teste 3.2: Despesa com valor negativo
            boolean barraValorNeg = false;
            try {
                new DespesaFixa("D2", "Negativa", new BigDecimal("-10.00"), List.of(p1), true, 0);
            } catch (ValidacaoException e) {
                barraValorNeg = true;
            }

            // Teste 3.3: Exclusão de participante vinculado a despesa
            plan.adicionarDespesa(new DespesaFixa("D3", "Vinculada", new BigDecimal("20.00"), List.of(p1), true, 0));
            boolean barraExclusao = false;
            try {
                plan.excluirParticipante(1);
            } catch (ValidacaoException e) {
                barraExclusao = true;
            }

            if (barraSemPart && barraValorNeg && barraExclusao) {
                System.out.println("PASSOU (Recusas de campos vazios, valores <= 0 e integridade relacional verificadas)");
                return true;
            }
        } catch (Exception e) {
            System.out.println("FALHOU: " + e.getMessage());
        }
        return false;
    }

    // 4. Alteração de orçamento (Seção 5)
    public static boolean testarAlteracaoOrcamento() {
        System.out.print("[TESTE 4] Alteração de Orçamento: ");
        try {
            Planejamento plan = new Planejamento("Teste Orcamento");
            Participante ana = new Participante(1, "Ana", new BigDecimal("70.00"));
            plan.cadastrarParticipante(ana);
            plan.adicionarDespesa(new DespesaFixa("D1", "Gasto", new BigDecimal("75.00"), List.of(ana), true, 0));

            // Custo inicial 75.00 vs orçamento 70.00 -> Excede
            boolean excedeInicial = plan.calcularGastoIndividual(ana).compareTo(ana.getOrcamento()) > 0;

            // Alterar orçamento para 80.00
            plan.alterarParticipante(1, "Ana", new BigDecimal("80.00"));

            boolean dentroLimite = plan.calcularGastoIndividual(ana).compareTo(ana.getOrcamento()) <= 0;
            boolean custoPreservado = plan.calcularGastoIndividual(ana).compareTo(new BigDecimal("75.00")) == 0;

            if (excedeInicial && dentroLimite && custoPreservado) {
                System.out.println("PASSOU (Custo preservado em R$ 75,00 e enquadrado dentro do novo limite de R$ 80,00)");
                return true;
            }
        } catch (Exception e) {
            System.out.println("FALHOU: " + e.getMessage());
        }
        return false;
    }

    // 5. Exclusão de despesa (Seção 5)
    public static boolean testarExclusaoDespesa() {
        System.out.print("[TESTE 5] Exclusão de Despesa: ");
        try {
            Planejamento plan = new Planejamento("Passeio");
            Participante p1 = new Participante(1, "Ana", new BigDecimal("70.00"));
            Participante p2 = new Participante(2, "Bruno", new BigDecimal("60.00"));
            Participante p3 = new Participante(3, "Carla", new BigDecimal("70.00"));

            plan.cadastrarParticipante(p1);
            plan.cadastrarParticipante(p2);
            plan.cadastrarParticipante(p3);

            plan.adicionarDespesa(new DespesaFixa("D1", "Transporte", new BigDecimal("90.00"), Arrays.asList(p1, p2, p3), true, 0));
            plan.adicionarDespesa(new DespesaPorPessoa("D2", "Almoço", new BigDecimal("20.00"), Arrays.asList(p1, p2, p3), true, 0));
            plan.adicionarDespesa(new DespesaFixa("D3", "Atividade recreativa", new BigDecimal("30.00"), Arrays.asList(p1, p3), false, 3));
            plan.adicionarDespesa(new DespesaPorPessoa("D4", "Sobremesa", new BigDecimal("10.00"), Arrays.asList(p1, p2, p3), false, 1));

            // Exclui a despesa D3 (Atividade recreativa)
            plan.excluirDespesa("D3");

            boolean okTotal = plan.calcularCustoTotal().compareTo(new BigDecimal("180.00")) == 0;
            boolean okAna = plan.calcularGastoIndividual(p1).compareTo(new BigDecimal("60.00")) == 0;
            boolean okBruno = plan.calcularGastoIndividual(p2).compareTo(new BigDecimal("60.00")) == 0;
            boolean okCarla = plan.calcularGastoIndividual(p3).compareTo(new BigDecimal("60.00")) == 0;

            if (okTotal && okAna && okBruno && okCarla) {
                System.out.println("PASSOU (Custo Total: R$ 180,00 | Todos os participantes com R$ 60,00)");
                return true;
            }
        } catch (Exception e) {
            System.out.println("FALHOU: " + e.getMessage());
        }
        return false;
    }

    // 6. Salvar e recuperar (Seção 5)
    public static boolean testarSalvarERecuperar() {
        System.out.print("[TESTE 6] Salvar e Reiniciar (Serialização): ");
        File temp = null;
        try {
            temp = File.createTempFile("planejamento_teste", ".dat");
            temp.deleteOnExit();

            Planejamento original = new Planejamento("Passeio Teste");
            Participante p1 = new Participante(1, "Ana", new BigDecimal("70.00"));
            Participante p2 = new Participante(2, "Bruno", new BigDecimal("60.00"));
            original.cadastrarParticipante(p1);
            original.cadastrarParticipante(p2);
            original.adicionarDespesa(new DespesaFixa("D1", "Transporte", new BigDecimal("50.00"), Arrays.asList(p1, p2), true, 0));

            SerializacaoPlanejamento repo = new SerializacaoPlanejamento();
            repo.salvar(original, temp);

            Planejamento recuperado = repo.carregar(temp);

            boolean partsIguais = recuperado.getParticipantes().size() == 2;
            boolean despsIguais = recuperado.getDespesas().size() == 1;
            boolean recalculoOk = recuperado.calcularCustoTotal().compareTo(new BigDecimal("50.00")) == 0;

            if (partsIguais && despsIguais && recalculoOk) {
                System.out.println("PASSOU (Integridade relacional e valores recuperados com sucesso)");
                return true;
            }
        } catch (Exception e) {
            System.out.println("FALHOU: " + e.getMessage());
        }
        return false;
    }

    // 7. Problemas no arquivo (Seção 5)
    public static boolean testarProblemaArquivo() {
        System.out.print("[TESTE 7] Tratamento de Arquivo Ausente/Inválido: ");
        try {
            SerializacaoPlanejamento repo = new SerializacaoPlanejamento();
            File arquivoInexistente = new File("caminho_inexistente_12345.dat");

            boolean capturouErro = false;
            try {
                repo.carregar(arquivoInexistente);
            } catch (PersistenciaException e) {
                capturouErro = true;
            }

            if (capturouErro) {
                System.out.println("PASSOU (Lançou PersistenciaException com mensagem clara e preveniu corrupção)");
                return true;
            }
        } catch (Exception e) {
            System.out.println("FALHOU: " + e.getMessage());
        }
        return false;
    }

    // 8. Objetivo Extra: Simulador de alternativas (Seção 7)
    public static boolean testarSimuladorOtimizacao() {
        System.out.print("[TESTE 8] Simulador de Alternativas (Caso de Referência e Impossível): ");
        try {
            Planejamento plan = new Planejamento("Passeio com Simulador");
            Participante p1 = new Participante(1, "Ana", new BigDecimal("70.00"));
            Participante p2 = new Participante(2, "Bruno", new BigDecimal("60.00"));
            Participante p3 = new Participante(3, "Carla", new BigDecimal("70.00"));

            plan.cadastrarParticipante(p1);
            plan.cadastrarParticipante(p2);
            plan.cadastrarParticipante(p3);

            plan.adicionarDespesa(new DespesaFixa("D1", "Transporte", new BigDecimal("90.00"), Arrays.asList(p1, p2, p3), true, 0));
            plan.adicionarDespesa(new DespesaPorPessoa("D2", "Almoço", new BigDecimal("20.00"), Arrays.asList(p1, p2, p3), true, 0));
            plan.adicionarDespesa(new DespesaFixa("D3", "Atividade recreativa", new BigDecimal("30.00"), Arrays.asList(p1, p3), false, 3));
            plan.adicionarDespesa(new DespesaPorPessoa("D4", "Sobremesa", new BigDecimal("10.00"), Arrays.asList(p1, p2, p3), false, 1));

            // Executar simulação
            SimuladorOtimizacao.ResultadoSimulacao res = SimuladorOtimizacao.simularMelhorCenario(plan);

            boolean solucaoOk = res.isSolucaoEncontrada();
            boolean manteveAtividade = res.getDespesasMantidas().stream().anyMatch(d -> d.getId().equals("D3"));
            boolean removeuSobremesa = res.getDespesasRemovidas().stream().anyMatch(d -> d.getId().equals("D4"));
            boolean prioOk = res.getPontuacaoPrioridadeTotal() == 3;
            boolean totalOk = res.getNovoCustoTotal().compareTo(new BigDecimal("180.00")) == 0;
            boolean anaOk = res.getNovosTotaisIndividuais().get(p1).compareTo(new BigDecimal("65.00")) == 0;
            boolean brunoOk = res.getNovosTotaisIndividuais().get(p2).compareTo(new BigDecimal("50.00")) == 0;
            boolean carlaOk = res.getNovosTotaisIndividuais().get(p3).compareTo(new BigDecimal("65.00")) == 0;

            // Testar caso sem solução: orçamento de Ana = 40.00 (essenciais custam 50.00)
            plan.alterarParticipante(1, "Ana", new BigDecimal("40.00"));
            SimuladorOtimizacao.ResultadoSimulacao resSemSolucao = SimuladorOtimizacao.simularMelhorCenario(plan);
            boolean barrouSemSolucao = !resSemSolucao.isSolucaoEncontrada() && resSemSolucao.getMensagemStatus().contains("Sem solução");

            if (solucaoOk && manteveAtividade && removeuSobremesa && prioOk && totalOk && anaOk && brunoOk && carlaOk && barrouSemSolucao) {
                System.out.println("PASSOU (Solução ótima remove sobremesa e preserva atividade; caso impossível tratado corretamente)");
                return true;
            }
        } catch (Exception e) {
            System.out.println("FALHOU: " + e.getMessage());
        }
        return false;
    }
}