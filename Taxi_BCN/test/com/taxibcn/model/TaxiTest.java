package com.taxibcn.model;

import UI.Menu;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TaxiTest {

    @Test
    void testMenu(){
        String formatoEsperado =
                """
                        
                        --- Menú Gestió Serveis ---\
                        
                        1. Crear servei\
                        
                        2. Marcar arribada del taxi\
                        
                        3. Finalitzar servei\
                        
                        4. Mostrar serveis\
                        
                        5. Sortir\
                        
                        Opció:\s""";

        assertEquals(formatoEsperado, Menu.imprimirOpciones());    }

}
