package cabenobolso.view;

import cabenobolso.controller.PlanejamentoController;
import cabenobolso.exception.PersistenciaException;
import cabenobolso.exception.ValidacaoException;
import cabenobolso.model.Despesa;
import cabenobolso.model.Participante;
import cabenobolso.model.SimuladorOtimizacao;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainFrame extends JFrame {
    private final PlanejamentoController controller;

    private ParticipanteTableModel partTableModel;
    private DespesaTableModel despTableModel;
    private JTable tabResumo;
    private DefaultTableModel resumoTableModel;
    private JLabel lblCustoTotal;

    // Componentes da aba do simulador
    private JTextArea txtResultadoSimulador;

    public MainFrame() {
        this.controller = new PlanejamentoController();
        inicializarUI();
    }

    private void inicializarUI() {
        setTitle("Cabe no bolso - Planejador de Despesas Compartilhadas");
        setSize(950, 680);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Participantes", criarPainelParticipantes());
        tabbedPane.addTab("Despesas", criarPainelDespesas());
        tabbedPane.addTab("Resumo & Rateio", criarPainelResumo());
        tabbedPane.addTab("Simulador (Bônus)", criarPainelSimulador());

        setJMenuBar(criarBarraMenu());
        getContentPane().add(tabbedPane, BorderLayout.CENTER);
    }

    private JMenuBar criarBarraMenu() {
        JMenuBar menuBar = new JMenuBar();
        JMenu menuArquivo = new JMenu("Arquivo");

        JMenuItem itemSalvar = new JMenuItem("Salvar Planejamento...");
        itemSalvar.addActionListener(e -> acaoSalvar());

        JMenuItem itemCarregar = new JMenuItem("Carregar Planejamento...");
        itemCarregar.addActionListener(e -> acaoCarregar());

        JMenuItem itemNovo = new JMenuItem("Novo Planejamento");
        itemNovo.addActionListener(e -> acaoNovo());

        menuArquivo.add(itemNovo);
        menuArquivo.addSeparator();
        menuArquivo.add(itemSalvar);
        menuArquivo.add(itemCarregar);
        menuBar.add(menuArquivo);
        return menuBar;
    }

    private JPanel criarPainelParticipantes() {
        JPanel painel = new JPanel(new BorderLayout(10, 10));
        painel.setBorder(new EmptyBorder(10, 10, 10, 10));

        partTableModel = new ParticipanteTableModel(controller.getPlanejamento().getParticipantes());
        JTable table = new JTable(partTableModel);
        painel.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel pnlBotoes = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnAdicionar = new JButton("Cadastrar");
        JButton btnEditar = new JButton("Editar");
        JButton btnExcluir = new JButton("Excluir");

        btnAdicionar.addActionListener(e -> {
            try {
                JTextField txtCodigo = new JTextField();
                JTextField txtNome = new JTextField();
                JTextField txtOrcamento = new JTextField();

                Object[] form = {
                        "Código Único:", txtCodigo,
                        "Nome do Participante:", txtNome,
                        "Orçamento Disponível (R$):", txtOrcamento
                };

                int op = JOptionPane.showConfirmDialog(this, form, "Cadastrar Participante", JOptionPane.OK_CANCEL_OPTION);
                if (op == JOptionPane.OK_OPTION) {
                    int cod = Integer.parseInt(txtCodigo.getText().trim());
                    String nome = txtNome.getText().trim();
                    BigDecimal orc = new BigDecimal(txtOrcamento.getText().trim().replace(",", "."));
                    controller.cadastrarParticipante(cod, nome, orc);
                    atualizarTodasTabelas();
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Formato numérico inválido.", "Erro de Validação", JOptionPane.ERROR_MESSAGE);
            } catch (ValidacaoException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Regra Violada", JOptionPane.WARNING_MESSAGE);
            }
        });

        btnEditar.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Selecione um participante para alterar.");
                return;
            }
            Participante p = controller.getPlanejamento().getParticipantes().get(row);
            try {
                JTextField txtNome = new JTextField(p.getNome());
                JTextField txtOrc = new JTextField(p.getOrcamento().toString());
                Object[] form = {"Nome:", txtNome, "Orçamento (R$):", txtOrc};
                int op = JOptionPane.showConfirmDialog(this, form, "Editar Participante", JOptionPane.OK_CANCEL_OPTION);
                if (op == JOptionPane.OK_OPTION) {
                    controller.alterarParticipante(p.getCodigo(), txtNome.getText(), new BigDecimal(txtOrc.getText().replace(",", ".")));
                    atualizarTodasTabelas();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnExcluir.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Selecione um participante para excluir.");
                return;
            }
            Participante p = controller.getPlanejamento().getParticipantes().get(row);
            try {
                controller.excluirParticipante(p.getCodigo());
                atualizarTodasTabelas();
            } catch (ValidacaoException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Exclusão Bloqueada", JOptionPane.WARNING_MESSAGE);
            }
        });

        pnlBotoes.add(btnAdicionar);
        pnlBotoes.add(btnEditar);
        pnlBotoes.add(btnExcluir);
        painel.add(pnlBotoes, BorderLayout.SOUTH);

        return painel;
    }

    private JPanel criarPainelDespesas() {
        JPanel painel = new JPanel(new BorderLayout(10, 10));
        painel.setBorder(new EmptyBorder(10, 10, 10, 10));

        despTableModel = new DespesaTableModel(controller.getPlanejamento().getDespesas());
        JTable table = new JTable(despTableModel);
        painel.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel pnlBotoes = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnAdicionar = new JButton("Adicionar Despesa");
        JButton btnExcluir = new JButton("Excluir Despesa");

        btnAdicionar.addActionListener(e -> abrirDialogNovaDespesa());
        btnExcluir.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Selecione uma despesa para excluir.");
                return;
            }
            Despesa d = controller.getPlanejamento().getDespesas().get(row);
            try {
                controller.excluirDespesa(d.getId());
                atualizarTodasTabelas();
            } catch (ValidacaoException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });

        pnlBotoes.add(btnAdicionar);
        pnlBotoes.add(btnExcluir);
        painel.add(pnlBotoes, BorderLayout.SOUTH);

        return painel;
    }

    private void abrirDialogNovaDespesa() {
        if (controller.getPlanejamento().getParticipantes().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Cadastre ao menos um participante antes de criar despesas.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JTextField txtId = new JTextField();
        JTextField txtDesc = new JTextField();
        JTextField txtValor = new JTextField();
        JComboBox<String> cmbTipo = new JComboBox<>(new String[]{"Fixa", "Por pessoa"});
        JCheckBox chkEssencial = new JCheckBox("Despesa Essencial?", true);
        JTextField txtPrioridade = new JTextField("0");
        txtPrioridade.setEnabled(false);

        chkEssencial.addActionListener(e -> {
            txtPrioridade.setEnabled(!chkEssencial.isSelected());
            if (chkEssencial.isSelected()) txtPrioridade.setText("0");
        });

        JPanel pnlCheckboxes = new JPanel(new GridLayout(0, 1));
        List<JCheckBox> checks = new ArrayList<>();
        for (Participante p : controller.getPlanejamento().getParticipantes()) {
            JCheckBox chk = new JCheckBox(p.toString());
            checks.add(chk);
            pnlCheckboxes.add(chk);
        }

        Object[] form = {
                "Identificador:", txtId,
                "Descrição:", txtDesc,
                "Tipo:", cmbTipo,
                "Valor Informado (R$):", txtValor,
                "Classificação:", chkEssencial,
                "Prioridade Opcional (1-10):", txtPrioridade,
                "Participantes Envolvidos:", new JScrollPane(pnlCheckboxes)
        };

        int op = JOptionPane.showConfirmDialog(this, form, "Incluir Nova Despesa", JOptionPane.OK_CANCEL_OPTION);
        if (op == JOptionPane.OK_OPTION) {
            try {
                List<Participante> selecionados = new ArrayList<>();
                for (int i = 0; i < checks.size(); i++) {
                    if (checks.get(i).isSelected()) {
                        selecionados.add(controller.getPlanejamento().getParticipantes().get(i));
                    }
                }
                BigDecimal valor = new BigDecimal(txtValor.getText().trim().replace(",", "."));
                boolean ess = chkEssencial.isSelected();
                int prio = Integer.parseInt(txtPrioridade.getText().trim());

                controller.adicionarDespesa(txtId.getText(), txtDesc.getText(), valor,
                        (String) cmbTipo.getSelectedItem(), selecionados, ess, prio);

                atualizarTodasTabelas();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Validação Falhou", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private JPanel criarPainelResumo() {
        JPanel painel = new JPanel(new BorderLayout(10, 10));
        painel.setBorder(new EmptyBorder(10, 10, 10, 10));

        resumoTableModel = new DefaultTableModel(
                new Object[]{"Código", "Participante", "Orçamento (R$)", "Total Gastos (R$)", "Situação", "Saldo/Excesso (R$)"}, 0
        ) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        tabResumo = new JTable(resumoTableModel);

        // Renderizador de cores para a coluna de Situação
        tabResumo.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                String situacao = (String) table.getValueAt(row, 4);
                if ("EXCEDIDO".equals(situacao)) {
                    c.setForeground(Color.RED);
                    c.setFont(c.getFont().deriveFont(Font.BOLD));
                } else {
                    c.setForeground(new Color(0, 128, 0));
                    c.setFont(c.getFont().deriveFont(Font.PLAIN));
                }
                return c;
            }
        });

        painel.add(new JScrollPane(tabResumo), BorderLayout.CENTER);

        JPanel pnlRodape = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        lblCustoTotal = new JLabel("Custo Total Geral: R$ 0,00");
        lblCustoTotal.setFont(new Font("SansSerif", Font.BOLD, 16));
        pnlRodape.add(lblCustoTotal);
        painel.add(pnlRodape, BorderLayout.SOUTH);

        return painel;
    }

    private JPanel criarPainelSimulador() {
        JPanel painel = new JPanel(new BorderLayout(10, 10));
        painel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JButton btnExecutar = new JButton("Executar Otimização de Orçamento");
        btnExecutar.setFont(new Font("SansSerif", Font.BOLD, 14));
        painel.add(btnExecutar, BorderLayout.NORTH);

        txtResultadoSimulador = new JTextArea();
        txtResultadoSimulador.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtResultadoSimulador.setEditable(false);
        painel.add(new JScrollPane(txtResultadoSimulador), BorderLayout.CENTER);

        btnExecutar.addActionListener(e -> {
            SimuladorOtimizacao.ResultadoSimulacao res = controller.executarSimulador();
            StringBuilder sb = new StringBuilder();
            sb.append("=================================================================\n");
            sb.append("   RESULTADO DA SIMULAÇÃO DE ALTERNATIVAS (OBJETIVO EXTRA)\n");
            sb.append("=================================================================\n\n");
            sb.append("Status: ").append(res.getMensagemStatus()).append("\n\n");

            if (res.isSolucaoEncontrada()) {
                sb.append("Prioridade Total Opcional Obtida: ").append(res.getPontuacaoPrioridadeTotal()).append("\n");
                sb.append("Novo Custo Total do Passeio: R$ ").append(res.getNovoCustoTotal()).append("\n\n");

                sb.append("DESPESAS MANTIDAS:\n");
                for (Despesa d : res.getDespesasMantidas()) {
                    sb.append(String.format(" - [%s] %s (%s) | Custo: R$ %.2f\n",
                            d.isEssencial() ? "ESSENCIAL" : "OPCIONAL (P=" + d.getPrioridade() + ")",
                            d.getDescricao(), d.getTipo(), d.getValorTotalCalculado()));
                }

                sb.append("\nDESPESAS RETIRADAS:\n");
                if (res.getDespesasRemovidas().isEmpty()) {
                    sb.append(" (Nenhuma despesa opcional precisou ser removida)\n");
                } else {
                    for (Despesa d : res.getDespesasRemovidas()) {
                        sb.append(String.format(" - [OPCIONAL P=%d] %s | Economia gerada: R$ %.2f\n",
                                d.getPrioridade(), d.getDescricao(), d.getValorTotalCalculado()));
                    }
                }

                sb.append("\nNOVOS TOTAIS INDIVIDUAIS POR PARTICIPANTE:\n");
                for (Map.Entry<Participante, BigDecimal> entry : res.getNovosTotaisIndividuais().entrySet()) {
                    Participante p = entry.getKey();
                    BigDecimal novoGasto = entry.getValue();
                    sb.append(String.format(" * %s: R$ %.2f (Orçamento: R$ %.2f) -> DENTRO DO LIMITE\n",
                            p.getNome(), novoGasto, p.getOrcamento()));
                }
            } else {
                sb.append("Nenhuma alternativa viável pôde ser encontrada respeitando as premissas.\n");
            }

            txtResultadoSimulador.setText(sb.toString());
        });

        return painel;
    }

    private void atualizarTodasTabelas() {
        partTableModel.setParticipantes(controller.getPlanejamento().getParticipantes());
        despTableModel.setDespesas(controller.getPlanejamento().getDespesas());

        resumoTableModel.setRowCount(0);
        for (Participante p : controller.getPlanejamento().getParticipantes()) {
            BigDecimal totalGasto = controller.getPlanejamento().calcularGastoIndividual(p);
            BigDecimal orcamento = p.getOrcamento();
            String situacao = totalGasto.compareTo(orcamento) <= 0 ? "DENTRO DO LIMITE" : "EXCEDIDO";
            BigDecimal diferenca = totalGasto.subtract(orcamento).abs();

            resumoTableModel.addRow(new Object[]{
                    p.getCodigo(),
                    p.getNome(),
                    String.format("%.2f", orcamento),
                    String.format("%.2f", totalGasto),
                    situacao,
                    String.format("%.2f", diferenca)
            });
        }

        lblCustoTotal.setText("Custo Total Geral: R$ " + String.format("%.2f", controller.getPlanejamento().calcularCustoTotal()));
    }

    private void acaoSalvar() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File arquivo = fileChooser.getSelectedFile();
            try {
                controller.salvarPlanejamento(arquivo);
                JOptionPane.showMessageDialog(this, "Planejamento salvo com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            } catch (PersistenciaException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Falha ao Salvar", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void acaoCarregar() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File arquivo = fileChooser.getSelectedFile();
            try {
                controller.carregarPlanejamento(arquivo);
                atualizarTodasTabelas();
                JOptionPane.showMessageDialog(this, "Planejamento carregado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            } catch (PersistenciaException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Falha ao Carregar", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void acaoNovo() {
        int confirm = JOptionPane.showConfirmDialog(this, "Deseja iniciar um novo planejamento em branco?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                controller.carregarPlanejamento(null); // falha intencional se arquivo for nulo
            } catch (PersistenciaException e) {
                try {
                    controller.salvarPlanejamento(null);
                } catch (Exception ignored) {}
            }
            // Inicializa novo planejamento
            try {
                java.lang.reflect.Field field = PlanejamentoController.class.getDeclaredField("planejamento");
                field.setAccessible(true);
                field.set(controller, new cabenobolso.model.Planejamento("Novo Passeio"));
                atualizarTodasTabelas();
            } catch (Exception ignored) {}
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}