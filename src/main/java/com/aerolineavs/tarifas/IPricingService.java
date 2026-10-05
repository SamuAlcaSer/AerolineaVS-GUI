package com.aerolineavs.tarifas;

/**
 * Contrato para evaluar la tarifa aplicable a un cliente.
 */
public interface IPricingService {

    /**
     * Evalúa una única tarifa aplicable en función de los datos del cliente.
     *
     * @param cliente datos del cliente
     * @return tarifa resultante junto con las suposiciones usadas
     */
    ResultadoTarifa evaluar(ClientePotencial cliente);
}
