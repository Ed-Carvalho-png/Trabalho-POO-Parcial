package cabenobolso.view;

import cabenobolso.model.Despesa;
import cabenobolso.model.Participante;
import javax.swing.table.AbstractTableModel;
import java.util.List;
import java.util.stream.Collectors;

public class DespesaTableModel extends AbstractTableModel {
    private final String[] colunas = {"ID", "Descrição", "Tipo", "Valor Base (R$)", "Custo Total (R$)", "Envolvidos"};
    private List<Despesa> despesas;

    public DespesaTableModel(List<Despesa> despesas) {
        this.despesas = despesas;
    }

    public void setDespesas(List<Despesa> despesas) {
        this.despesas = despesas;
        fireTableDataChanged();
    }

    @Override public int getRowCount() { return despesas.size(); }
    @Override public int getColumnCount() { return colunas.length; }
    @Override public String getColumnName(int col) { return colunas[col]; }

    @Override
    public Object getValueAt(int row, int col) {
        Despesa d = despesas.get(row);
        switch (col) {
            case 0: return d.getId();
            case 1: return d.getDescricao();
            case 2: return d.getTipo();
            case 3: return String.format("%.2f", d.getValorInformado());
            case 4: return String.format("%.2f", d.getValorTotalCalculado());
            case 5: return d.getParticipantes().stream().map(Participante::getNome).collect(Collectors.joining(", "));
            default: return null;
        }
    }
}