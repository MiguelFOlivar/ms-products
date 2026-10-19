
package org.dev.app.msproducts.dto;

import java.util.ArrayList;
import java.util.List;

public class ImportResultDTO {

    private int procesados;
    private int aceptados;
    private int rechazados;

    private List<ImportErrorDTO> errores = new ArrayList<>();

    public int getProcesados() {
        return procesados;
    }

    public void setProcesados(int procesados) {
        this.procesados = procesados;
    }

    public int getAceptados() {
        return aceptados;
    }

    public void setAceptados(int aceptados) {
        this.aceptados = aceptados;
    }

    public int getRechazados() {
        return rechazados;
    }

    public void setRechazados(int rechazados) {
        this.rechazados = rechazados;
    }

    public List<ImportErrorDTO> getErrores() {
        return errores;
    }

    public void setErrores(List<ImportErrorDTO> errores) {
        this.errores = errores;
    }
}
