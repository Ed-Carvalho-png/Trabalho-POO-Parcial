package cabenobolso.persistence;

import cabenobolso.exception.PersistenciaException;
import cabenobolso.model.Planejamento;
import java.io.File;

public interface IPersistenciaPlanejamento {
    void salvar(Planejamento planejamento, File arquivo) throws PersistenciaException;
    Planejamento carregar(File arquivo) throws PersistenciaException;
}