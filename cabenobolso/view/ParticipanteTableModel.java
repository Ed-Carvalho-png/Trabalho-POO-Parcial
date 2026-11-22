package cabenobolso.view;

import cabenobolso.model.Participante;
import javax.swing.table.AbstractTableModel;
import java.util.List;

public class ParticipanteTableModel extends AbstractTableModel {
    private final String[] colunas = {"Código", "Nome", "Orçamento (R$)"};
    private List<Participante> participantes;

    public ParticipanteTableModel(List<Participante> participantes) {
        this.participantes = participantes;
    }

    public void setParticipantes(List<Participante> participantes) {
        this.participantes = participantes;
        fireTableDataChanged();
    }

    @Override public int getRowCount() { return participantes.size(); }
    @Override public int getColumnCount() { return colunas.length; }
    @Override public String getColumnName(int col) { return colunas[col]; }

    @Override
    public Object getValueAt(int row, int col) {
        Participante p = participantes.get(row);
        switch (col) {
            case 0: return p.getCodigo();
            case 1: return p.getNome();
            case 2: return String.format("%.2f", p.getOrcamento());
            default: return null;
        }
    }
}