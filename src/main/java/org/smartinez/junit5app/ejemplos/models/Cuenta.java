package org.smartinez.junit5app.ejemplos.models;

import org.smartinez.junit5app.ejemplos.exceptions.DineroInsuficienteException;

import java.math.BigDecimal;

public class Cuenta {
    private String persona;
    private BigDecimal saldo;
    private Banco banco;

    public Cuenta(String persona, BigDecimal saldo) {
        this.saldo = saldo;
        this.persona = persona;
    }

    public String getPersona() {
        return persona;
    }

    public void setPersona(String persona) {
        this.persona = persona;
    }

    public BigDecimal getSaldo() {
        return saldo;
    }

    public void setSaldo(BigDecimal saldo) {
        this.saldo = saldo;
    }
//          El manejo de la excepcion podria haber sido asi tambien
//            if(monto.intValue() < 0){
//        throw new IllegalArgumentException("El monto no puede ser negativo");
//    }
//        if(monto.doubleValue() > this.getSaldo().doubleValue()){
//        throw new IllegalArgumentException("No hay suficiente dinero en la cuenta como para mandar ese monto");
//    }
//

    public Banco getBanco() {
        return banco;
    }

    public void setBanco(Banco banco) {
        this.banco = banco;
    }

    public void debito(BigDecimal monto){//BigDecimal es inmutable
        //para modificarlo se crea una nueva instancia
        BigDecimal nuevoSaldo = this.saldo.subtract(monto);
        if(nuevoSaldo.compareTo(BigDecimal.ZERO) < 0){  //eg: -2 - 0 < 0 entonces tira la exception
            throw new DineroInsuficienteException("Dinero Insuficiente");
        }
        this.saldo = nuevoSaldo;
    }

    public void credito(BigDecimal monto){

        this.saldo = this.saldo.add(monto);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null || !(obj instanceof Cuenta)){
            return false;
        }
        Cuenta c = (Cuenta) obj;//castear
        if(this.persona == null || this.saldo == null){
            return false;
        }

        return this.persona.equals(c.getPersona()) && this.saldo.equals(c.getSaldo());
    }
}
