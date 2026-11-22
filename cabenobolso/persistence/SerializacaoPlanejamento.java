package cabenobolso.persistence;

import cabenobolso.exception.PersistenciaException;
import cabenobolso.model.Planejamento;
import java.io.*;

public class SerializacaoPlanejamento implements IPersistenciaPlanejamento {

    @Override
    public void salvar(Planejamento planejamento, File arquivo) throws PersistenciaException {
        if (planejamento == null) {
            throw new PersistenciaException("Instância de planejamento inválida para persistência.", null);
        }
        try (FileOutputStream fos = new FileOutputStream(arquivo);
             BufferedOutputStream bos = new BufferedOutputStream(fos);
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(planejamento);
            oos.flush();
        } catch (IOException e) {
            throw new PersistenciaException("Falha ao gravar arquivo em disco: " + e.getMessage(), e);
        }
    }

    @Override
    public Planejamento carregar(File arquivo) throws PersistenciaException {
        if (arquivo == null || !arquivo.exists() || !arquivo.canRead()) {
            throw new PersistenciaException("Arquivo inexistente ou inacessível para leitura.", null);
        }
        try (FileInputStream fis = new FileInputStream(arquivo);
             BufferedInputStream bis = new BufferedInputStream(fis);
             ObjectInputStream ois = new ObjectInputStream(bis)) {
            Object obj = ois.readObject();
            if (obj instanceof Planejamento) {
                return (Planejamento) obj;
            } else {
                throw new PersistenciaException("Formato incompatível: objeto não é um Planejamento válido.", null);
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new PersistenciaException("Falha ao desserializar arquivo: dados corrompidos ou versão incompatível.", e);
        }
    }
}