package org.smartinez.junit5app.ejemplos;


import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.smartinez.junit5app.ejemplos.exceptions.DineroInsuficienteException;
import org.smartinez.junit5app.ejemplos.models.Banco;
import org.smartinez.junit5app.ejemplos.models.Cuenta;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

//@TestInstance(TestInstance.Lifecycle.PER_CLASS) SI HAGO ESO SE CREA SOLO UNA INSTANCIA DE LA CLASE TEST
// Y PODEMOS ENTONCES TENER METODOS NO ESTATICOS, ATRIBUTOS, ETC. PERO NO SE DESEA PORQUE SE TRATA DE QUE SEA STYLELESS
class CuentaTest {
    Cuenta cuenta;

    private TestInfo testInfo;
    private TestReporter testReporter;


    @BeforeEach
    void initMetodoTest(TestInfo testInfo, TestReporter testReporter) {
        Cuenta cuenta = new Cuenta("Andres", new BigDecimal("1000.12345"));
        this.testInfo = testInfo;
        this.testReporter = testReporter;
        System.out.println("Iniciando el metodo");
        testReporter.publishEntry("Ejecutando: " + testInfo.getDisplayName() + " " + testInfo.getTestMethod().orElse(null).getName() +
                " con las etiquetas " + testInfo.getTags());
    }

    @AfterEach
    void tearDown() {
        System.out.println("Finalizando el metodo del programa");
    }

    @BeforeAll
    static void beforeAll() {
        System.out.println("Inicializando el test");

    }

    @AfterAll
    static void afterAll() {
        System.out.println("finalizando el test");
    }

    //test anidados como clases anidadas. Realizamos pruebas por contexto/lo que hacen
    @Nested
    @Tag("cuenta") //para ejecutar solo los que tienen cierto tag
    @DisplayName("Probando atributos de la cuenta corriente")
    class CuentaTestNombreSaldo {
        @Test
        @DisplayName("el nombre")
        void testNombreCuenta() {
            testReporter.publishEntry(testInfo.getTags().toString());
            if(testInfo.getTags().contains("cuenta")){
                System.out.println("Hacer algo con la etiqueta cuenta");
            }
            Cuenta cuenta = new Cuenta("Andres", new BigDecimal("1000.12345"));
//        cuenta.setPersona("Andres");
            String esperado = "Andres";
            String real = cuenta.getPersona();
            assertNotNull(real, () -> "La cuenta no puede ser nula");
            assertEquals(esperado, real, () -> "el nombre de la cuenta no es el que se esperaba: se esperaba " + esperado
                    + " sin embargo fue " + real);
            assertTrue(real.equals("Andres"), () -> "nombre cuenta esperado debe ser igual a la real");
        }

        @Test
        @DisplayName("el saldo, que no sea null, mayor que cero, valor esperado")
        void testSaldoCuenta() {
            Cuenta cuenta = new Cuenta("Andres", new BigDecimal("1000.12345"));
            assertNotNull(cuenta.getSaldo()); //chequear que el saldo no sea nulo
            assertEquals(1000.12345, cuenta.getSaldo().doubleValue());
            assertFalse(cuenta.getSaldo().compareTo(BigDecimal.ZERO) < 0);
            assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
        }

        @Test
        @DisplayName("testeando referencias que sean iguales con el metodo equals")
        void testReferenciaCuenta() {
            Cuenta cuenta = new Cuenta("John Doe", new BigDecimal("8900.9997"));
            Cuenta cuenta2 = new Cuenta("John Doe", new BigDecimal("8900.9997"));
            //assertNotEquals(cuenta2, cuenta); //comparando si son el mismo objeto, cosa que no lo son
            assertEquals(cuenta2, cuenta); //comparando si son iguales los valores de los atributos de ambos objetos
        }
    }

    @Nested
    class CuentaOperacionesTest {

        @Tag("cuenta")
        @Test
        @DisplayName("Probando el metodo debito de la clase Cuenta")
        void testDebitoCuenta() {
            Cuenta cuenta = new Cuenta("Andres", new BigDecimal("1000.12345"));
            cuenta.debito((new BigDecimal(100)));
            assertNotNull(cuenta.getSaldo()); //chequear que el saldo no sea nulo
            assertEquals(900, cuenta.getSaldo().intValue());
            assertEquals("900.12345", cuenta.getSaldo().toPlainString());
        }

        @Tag("cuenta")
        @Test
        @DisplayName("Probando el metodo credito de la clase Cuenta")
        void testCreditoCuenta() {
            Cuenta cuenta = new Cuenta("Andres", new BigDecimal("1000.12345"));
            //esta es la misma que la anterior por lo que se la puede borrar
            cuenta.credito((new BigDecimal(100)));
            assertNotNull(cuenta.getSaldo()); //chequear que el saldo no sea nulo
            assertEquals(1100, cuenta.getSaldo().intValue());
            assertEquals("1100.12345", cuenta.getSaldo().toPlainString());
        }

        @Tag("cuenta")
        @Tag("banco")
        @Test
        @DisplayName("Probando que el metodo transferir dinero de la clase Banco funcione")
        void testTransferirDineroCuentas() {
            Cuenta cuenta1 = new Cuenta("John Doe", new BigDecimal("2500"));
            Cuenta cuenta2 = new Cuenta("Andres", new BigDecimal("1500.8989"));

            Banco banco = new Banco();
            banco.setNombre("Banco del Estado");
            banco.transferir(cuenta2, cuenta1, new BigDecimal(500));
            assertEquals("1000.8989", cuenta2.getSaldo().toPlainString());
            assertEquals("3000", cuenta1.getSaldo().toPlainString());
        }
    }


    @Test
    @Tag("cuenta")
    @Tag("error")
    @DisplayName("Probando que la excepcion por dinero insuficiente a la hora de usar el metodo debito o credito funcione")
    void testDineroInsuficienteException() {
        Cuenta cuenta = new Cuenta("Andres", new BigDecimal("1000.12345"));

        Exception exception = assertThrows(DineroInsuficienteException.class, () -> {
            cuenta.debito((new BigDecimal(1500)));
        });
        String actual = exception.getMessage();
        String esperado = "Dinero Insuficiente";
        assertEquals(esperado, actual);
    }


    @Test
    @Tag("cuenta")
    @Tag("banco")
    //@Disabled   //esta bueno porque aparece en la prueba pero no lo evalua
    @DisplayName("Probando relaciones entre las cuentas y el banco con assertAll")
    void testRelacionBancoCuentas() {
        //fail();
        Cuenta cuenta1 = new Cuenta("John Doe", new BigDecimal("2500"));
        Cuenta cuenta2 = new Cuenta("Andres", new BigDecimal("1500.8989"));

        Banco banco = new Banco();
        banco.setNombre("Banco del Estado");
        banco.addCuenta(cuenta1);
        banco.addCuenta(cuenta2);

        banco.transferir(cuenta2, cuenta1, new BigDecimal(500));
        //Usando assertAll puedo verificar todos los assert
        assertAll(() -> assertEquals("1000.8989", cuenta2.getSaldo().toPlainString(),
                        () -> "el valor del saldo de la cuenta2 no es el esperado"),
                () -> assertEquals("3000", cuenta1.getSaldo().toPlainString(),
                        () -> "el valor del saldo de la cuenta1 no es el esperado"),
                () -> assertEquals(2, banco.getCuentas().size(), () -> "el banco no tiene las cuentas esperadas"),
                () -> assertEquals("Banco del Estado", cuenta1.getBanco().getNombre()),
                () -> assertEquals("Andres", banco.getCuentas().stream()
                        .filter(c -> c.getPersona().equals("Andres"))
                        .findFirst()
                        .get().getPersona()),
                () -> assertTrue(banco.getCuentas().stream()
                        .anyMatch(c -> c.getPersona().equals("Andres")))
        );
    }

    //test anidados como clases anidadas. Realizamos pruebas por contexto/lo que hacen
    class SistemaOperativosTest {

        @Test
        @EnabledOnOs(OS.WINDOWS)
        void testSoloWindows() {
        }

        @Test
        @EnabledOnOs({OS.LINUX, OS.MAC})
            //Se ejecuta solo en Linux o Mac
        void testSoloLinuxMac() {
        }

        @Test
        @DisabledOnOs(OS.WINDOWS)
        void testNoWindows() {
        }
    }

    class JavaVersionTest {
        @Test
        @EnabledOnJre(JRE.JAVA_8)
        void soloJDK8() {
        }

        @Test
        @EnabledOnJre(JRE.JAVA_18)
        void soloJDK18() {
        }

        @Test
        @DisabledOnJre(JRE.JAVA_18)
        void testNoJDK18() {
        }
    }

    @Nested
    class SystemPropertiesTest {
        @Test
        void imprimirSystemProperties() {
            Properties properties = System.getProperties();
            properties.forEach((k, v) -> System.out.println(k + ":" + v));
        }

        @Test
        @EnabledIfSystemProperty(named = "java.version", matches = "18.0.1")
            //o una expresion regular matches = ".*18.*"
        void testJavaVersion() {
        }

        @Test
        @DisabledIfSystemProperty(named = "os.arch", matches = ".*32.*")
        void testSolo64() {
        }

        @Test
        @EnabledIfSystemProperty(named = "os.arch", matches = ".*32.*")
        void testNo64() {
        }

        @Test
        @EnabledIfSystemProperty(named = "user.name", matches = "SebMartinez")
        void testUsername() {
        }

        @Test
        @EnabledIfSystemProperty(named = "ENV", matches = "dev")
        void testDev() {
        }
    }

    @Nested
    class VariableAmbienteTest {
        @Test
        void imprimirVariablesAmbiente() {
            Map<String, String> getenv = System.getenv();
            getenv.forEach((k, v) -> System.out.println(k + " = " + v));
        }

        @Test
        @EnabledIfEnvironmentVariable(named = "HOMEDRIVE", matches = "C:")
        void testJavaHome() {//No me aparece JAVAHOME asi que use HOMEDRIVE
        }

        @Test //Para pruebas unitarias muy pesadas
        @EnabledIfEnvironmentVariable(named = "NUMBER_OF_PROCESSORS", matches = "16")
        void testProcesadores() {
        }

        @Test
        @EnabledIfEnvironmentVariable(named = "ENVIRONMENT", matches = "dev")
        void testEnv() {
        }

        @Test
        @DisabledIfEnvironmentVariable(named = "ENVIRONMENT", matches = "prod")
        void testEnvProdDisabled() {
        }
    }


    //Usando Assumptions
    @Test
    @DisplayName("testSaldoCuentaDev")
    void testSaldoCuentaDev() {
        boolean esDev = "dev".equals(System.getProperty("ENV"));
        assumeTrue(esDev);
        Cuenta cuenta = new Cuenta("Andres", new BigDecimal("1000.12345"));
        assertNotNull(cuenta.getSaldo()); //chequear que el saldo no sea nulo
        assertEquals(1000.12345, cuenta.getSaldo().doubleValue());
        assertFalse(cuenta.getSaldo().compareTo(BigDecimal.ZERO) < 0);
        assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    @DisplayName("testSaldoCuentaDev")
    void testSaldoCuentaDev2() {
        boolean esDev = "dev".equals(System.getProperty("ENV"));
        assumingThat(esDev, () -> {
            Cuenta cuenta = new Cuenta("Andres", new BigDecimal("1000.12345"));
            assertNotNull(cuenta.getSaldo()); //chequear que el saldo no sea nulo
            assertEquals(1000.12345, cuenta.getSaldo().doubleValue());
            assertFalse(cuenta.getSaldo().compareTo(BigDecimal.ZERO) < 0);
            assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
        });
    }

    //se suele usar repeated test cuando trabajamos con Random
    //y el comportamiento de nuestro test podria variar segun algun parametro que varia cada vez que se ejecuta
    @DisplayName("Probando Debito Cuenta Repetir")
    @RepeatedTest(value=5, name = " {displayName} Repeticion numero {currentRepetition} de {totalRepetitions}")
    void testDebitoCuentaRepetir(RepetitionInfo info) {
        if(info.getCurrentRepetition() == 3){
            System.out.println("estamos en la repeticion " + info.getCurrentRepetition());
        }
        Cuenta cuenta = new Cuenta("Andres", new BigDecimal("1000.12345"));
        cuenta.debito((new BigDecimal(100)));
        assertNotNull(cuenta.getSaldo()); //chequear que el saldo no sea nulo
        assertEquals(900, cuenta.getSaldo().intValue());
        assertEquals("900.12345", cuenta.getSaldo().toPlainString());
    }

    @Tag("param")
    @Nested
    class PruebasParametrizadasTest{
        //se repite varias veces de forma parametrizada
        @ParameterizedTest(name = "numero {index} ejecutando con valor {0} {argumentsWithNames}")
        @ValueSource(strings = {"100", "200", "300", "500", "700", "1000"})
        void testDebitoCuentaValueSource(String monto) { //la precision con String en BigDecimal es mejor que con double
            Cuenta cuenta = new Cuenta("Andres", new BigDecimal("1000.12345"));
            cuenta.debito(new BigDecimal(monto));
            assertNotNull(cuenta.getSaldo()); //chequear que el saldo no sea nulo
            assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO)> 0);
        }

        @ParameterizedTest(name = "numero {index} ejecutando con valor {0} {argumentsWithNames}")
        @CsvSource({"1, 100", "2, 200", "3, 300", "4, 500", "5, 700", "6, 1000"})
        void testDebitoCuentaCsvSource(String index, String monto) { //la precision con String en BigDecimal es mejor que con double
            Cuenta cuenta = new Cuenta("Andres", new BigDecimal("1000.12345"));
            System.out.println(index + " -> " + monto);
            cuenta.debito(new BigDecimal(monto));
            assertNotNull(cuenta.getSaldo()); //chequear que el saldo no sea nulo
            assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO)> 0);
        }

        @ParameterizedTest(name = "numero {index} ejecutando con valor {0} {argumentsWithNames}")
        @CsvSource({"200, 100, John, Andres", "250, 200, Pepe, Pepe", "300, 300, maria, Maria", "510, 500, Pepa, Pepa", "750, 700, Lucas , Luca", "1000.12345, 1000.12345, Cata, Cata"})
        void testDebitoCuentaCsvSource2(String saldo, String monto, String esperado, String actual) { //la precision con String en BigDecimal es mejor que con double
            Cuenta cuenta = new Cuenta("Andres", new BigDecimal("1000.12345"));
            cuenta.setSaldo(new BigDecimal(saldo));
            cuenta.debito(new BigDecimal(monto));
            cuenta.setPersona(actual);

            assertNotNull(cuenta.getSaldo()); //chequear que el saldo no sea nulo
            assertNotNull(cuenta.getPersona());
            assertEquals(esperado, actual);
            assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO)> 0);
        }

        @ParameterizedTest(name = "numero {index} ejecutando con valor {0} {argumentsWithNames}")
        @CsvFileSource(resources = "/data.csv")
        void testDebitoCuentaCsvFileSource(String monto) { //la precision con String en BigDecimal es mejor que con double
            Cuenta cuenta = new Cuenta("Andres", new BigDecimal("1000.12345"));
            cuenta.debito(new BigDecimal(monto));
            assertNotNull(cuenta.getSaldo()); //chequear que el saldo no sea nulo
            assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO)> 0);
        }

        @ParameterizedTest(name = "numero {index} ejecutando con valor {0} {argumentsWithNames}")
        @CsvFileSource(resources = "/data2.csv")
        void testDebitoCuentaCsvFileSource2(String saldo, String monto, String esperado, String actual) { //la precision con String en BigDecimal es mejor que con double
            Cuenta cuenta = new Cuenta("Andres", new BigDecimal("1000.12345"));
            cuenta.setSaldo(new BigDecimal(saldo));
            cuenta.debito(new BigDecimal(monto));
            cuenta.setPersona(actual);

            assertNotNull(cuenta.getSaldo()); //chequear que el saldo no sea nulo
            assertNotNull(cuenta.getPersona());
            assertEquals(esperado, actual);
            assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO)> 0);
        }


    }
    @Tag("param")
    @ParameterizedTest(name = "numero {index} ejecutando con valor {0} {argumentsWithNames}")
    @MethodSource("montoList")
    void testDebitoCuentaMethodSource(String monto) { //la precision con String en BigDecimal es mejor que con double
        Cuenta cuenta = new Cuenta("Andres", new BigDecimal("1000.12345"));
        cuenta.debito(new BigDecimal(monto));
        assertNotNull(cuenta.getSaldo()); //chequear que el saldo no sea nulo
        assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO)> 0);
    }

    static List<String> montoList(){
        return Arrays.asList("100", "200", "300", "500", "700", "1000");
    }


    @Nested
    @Tag("timeout")
    class EjemploTimeOutTest{
        @Test
        @Timeout(1)
        void pruebaTimeout() throws InterruptedException {
            TimeUnit.MILLISECONDS.sleep(100);//hace una pausa o simula una carga pesada
        }

        @Test
        @Timeout(value = 1000, unit = TimeUnit.MILLISECONDS)
        void pruebaTimeout2() throws InterruptedException {
            TimeUnit.MILLISECONDS.sleep(900);
        }

        @Test
        void testTimeoutAssertions() {
            assertTimeout(Duration.ofSeconds(5), ()-> {
                TimeUnit.MILLISECONDS.sleep(4000);
            });
        }
    }


}