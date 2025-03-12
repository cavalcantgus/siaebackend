package com.siae.utils;

import lombok.NoArgsConstructor;

import java.text.NumberFormat;
import java.util.Locale;

@NoArgsConstructor
public class NumeroPorExtenso {
    private static final String[] UNIDADES = {"", "um", "dois", "três", "quatro", "cinco", "seis", "sete", "oito", "nove"};
    private static final String[] DEZENAS = {"", "dez", "vinte", "trinta", "quarenta", "cinquenta", "sessenta", "setenta", "oitenta", "noventa"};
    private static final String[] ESPECIAIS = {"dez", "onze", "doze", "treze", "quatorze", "quinze", "dezesseis", "dezessete", "dezoito", "dezenove"};
    private static final String[] CENTENAS = {"", "cento", "duzentos", "trezentos", "quatrocentos", "quinhentos", "seiscentos", "setecentos", "oitocentos", "novecentos"};
    private static final String[] MILHOES = {"", "mil", "milhão", "bilhão", "trilhão"};

    public static String converterNumeroPorExtenso(long numero) {
        if (numero == 0) {
            return "zero";
        }
        if (numero == 100) {
            return "cem";
        }

        StringBuilder extenso = new StringBuilder();

        int parte = 0;
        long divisor = 1;
        while (numero / divisor >= 1000) {
            divisor *= 1000;
            parte++;
        }

        while (divisor > 0) {
            long valor = numero / divisor;
            if (valor > 0) {
                if (extenso.length() > 0) {
                    extenso.append(" e ");
                }
                extenso.append(converterCentenas((int) valor));
                if (parte > 0) {
                    extenso.append(" ").append(MILHOES[parte]);
                    if (valor > 1 && parte > 1) {
                        extenso.append("s"); // "milhões" e "bilhões"
                    }
                }
            }
            numero %= divisor;
            divisor /= 1000;
            parte--;
        }

        return extenso.toString();
    }

    public static String converterCentenas(int numero) {
        if (numero == 100) {
            return "cem";
        }

        StringBuilder resultado = new StringBuilder();
        int centena = numero / 100;
        int dezena = (numero % 100) / 10;
        int unidade = numero % 10;

        if (centena > 0) {
            resultado.append(CENTENAS[centena]);
        }
        if (dezena == 1 && unidade > 0) {
            if (resultado.length() > 0) {
                resultado.append(" e ");
            }
            resultado.append(ESPECIAIS[unidade]);
        } else {
            if (dezena > 0) {
                if (resultado.length() > 0) {
                    resultado.append(" e ");
                }
                resultado.append(DEZENAS[dezena]);
            }
            if (unidade > 0) {
                if (resultado.length() > 0) {
                    resultado.append(" e ");
                }
                resultado.append(UNIDADES[unidade]);
            }
        }

        return resultado.toString();
    }

    public static String converterValorMonetarioPorExtenso(double valor) {
        long parteInteira = (long) valor;
        int centavos = (int) Math.round((valor - parteInteira) * 100);

        String resultado = converterNumeroPorExtenso(parteInteira) + " reais";

        if (centavos > 0) {
            resultado += " e " + converterNumeroPorExtenso(centavos) + " centavos";
        }

        return resultado;
    }
}
